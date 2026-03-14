package game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import models.*
import kotlin.random.Random

enum class PlayerAction {
    CHECK_CALL, BET, FOLD
}

class GameEngine(private val coroutineScope: CoroutineScope) {
        fun startFromTitle() {
            gameState.value = gameState.value.copy(phase = GamePhase.LOBBY)
        }
    val gameState = MutableStateFlow(GameState(players = emptyList()))
    private var deck: List<SkillCard> = emptyList()

    fun startGame(players: List<Player>, generatedDeck: List<SkillCard>, holeCardsIndices: Map<String, List<Int>>) {
        deck = generatedDeck.toMutableList()
        val dealtPlayers = players.mapIndexed { index, player ->
            val indices = holeCardsIndices["player$index"] ?: emptyList()
            val cards = indices.mapNotNull { i -> deck.getOrNull(i) }
            player.copy(holeCards = cards, chips = 100_000, hasFolded = false, currentBet = 0)
        }
        
        // Remove dealt cards from deck to form community cards later
        val dealtCards = dealtPlayers.flatMap { it.holeCards }.toSet()
        deck = deck.filterNot { it in dealtCards }

        gameState.value = GameState(
            players = dealtPlayers,
            communityCards = emptyList(),
            pot = 0,
            round = 1,
            phase = GamePhase.PRE_FLOP,
            communityCardsRevealed = 0,
            currentUserIndex = 0
        )
    }

    fun humanAction(action: PlayerAction) {
        val state = gameState.value
        val currentPlayers = state.players.toMutableList()
        val human = currentPlayers[0]

        when (action) {
            PlayerAction.FOLD -> {
                currentPlayers[0] = human.copy(hasFolded = true)
            }
            PlayerAction.CHECK_CALL -> {
                // simple check/call, no additional chips subtracted for simplicity unless needed
            }
            PlayerAction.BET -> {
                val betAmount = 10_000
                if (human.chips >= betAmount) {
                    currentPlayers[0] = human.copy(
                        chips = human.chips - betAmount,
                        currentBet = human.currentBet + betAmount
                    )
                    gameState.value = state.copy(pot = state.pot + betAmount, players = currentPlayers)
                }
            }
        }

        gameState.value = gameState.value.copy(players = currentPlayers)
        
        // Trigger NPC turns
        coroutineScope.launch {
            processNpcTurns()
        }
    }

    private suspend fun processNpcTurns() {
        val state = gameState.value
        val currentPlayers = state.players.toMutableList()
        var currentPot = state.pot

        for (i in 1 until currentPlayers.size) {
            val npc = currentPlayers[i]
            if (npc.hasFolded) continue

            // Set active player and simulate thinking
            gameState.value = gameState.value.copy(currentUserIndex = i)
            delay(800)

            val actionChoice = Random.nextInt(3)
            when (actionChoice) {
                0 -> { // Fold
                    currentPlayers[i] = npc.copy(hasFolded = true)
                }
                1 -> { // Check/Call
                    // Do nothing to chips
                }
                2 -> { // Bet
                    val betAmount = 10_000
                    if (npc.chips >= betAmount) {
                        currentPlayers[i] = npc.copy(
                            chips = npc.chips - betAmount,
                            currentBet = npc.currentBet + betAmount
                        )
                        currentPot += betAmount
                    }
                }
            }
            gameState.value = gameState.value.copy(players = currentPlayers, pot = currentPot)
        }

        advancePhase()
    }

    private suspend fun advancePhase() {
        delay(1000)
        val state = gameState.value
        val currentPlayers = state.players
        
        // Check if everyone else folded
        val activePlayers = currentPlayers.filter { !it.hasFolded }
        if (activePlayers.size == 1) {
            endRound(activePlayers.first())
            return
        }

        val nextPhase = when (state.phase) {
            GamePhase.PRE_FLOP -> GamePhase.FLOP
            GamePhase.FLOP -> GamePhase.TURN
            GamePhase.TURN -> GamePhase.RIVER
            GamePhase.RIVER -> GamePhase.SHOWDOWN
            else -> return
        }
        
        val newCommunityCards = state.communityCards.toMutableList()
        var cardsRevealed = state.communityCardsRevealed
        
        when (nextPhase) {
            GamePhase.FLOP -> {
                newCommunityCards.addAll(deck.take(3))
                deck = deck.drop(3)
                cardsRevealed = 3
            }
            GamePhase.TURN -> {
                newCommunityCards.addAll(deck.take(1))
                deck = deck.drop(1)
                cardsRevealed = 4
            }
            GamePhase.RIVER -> {
                newCommunityCards.addAll(deck.take(1))
                deck = deck.drop(1)
                cardsRevealed = 5
            }
            GamePhase.SHOWDOWN -> {
                // Showdown logic handled by UI mostly, but we set winners here
                endRound()
                return
            }
            else -> {}
        }

        gameState.value = state.copy(
            phase = nextPhase,
            communityCards = newCommunityCards,
            communityCardsRevealed = cardsRevealed,
            currentUserIndex = 0
        )
    }

    private fun endRound(defaultWinner: Player? = null) {
        val state = gameState.value
        val players = state.players.toMutableList()
        
        val winner = defaultWinner ?: evaluateWinner(players.filter { !it.hasFolded }, state.communityCards)
        
        val winnerIndex = players.indexOfFirst { it.name == winner.name }
        if (winnerIndex >= 0) {
            players[winnerIndex] = players[winnerIndex].copy(chips = players[winnerIndex].chips + state.pot)
        }

        gameState.value = state.copy(
            phase = GamePhase.SHOWDOWN,
            players = players
        )
    }

    fun nextRound() {
        val state = gameState.value
        if (state.round >= 10) {
            gameState.value = state.copy(phase = GamePhase.GAME_OVER)
            return
        }
        
        // Reset for next round
        val resetPlayers = state.players.map { it.copy(hasFolded = false, currentBet = 0, holeCards = emptyList()) }
        gameState.value = state.copy(
            players = resetPlayers,
            communityCards = emptyList(),
            communityCardsRevealed = 0,
            pot = 0,
            round = state.round + 1,
            phase = GamePhase.LOBBY // Should probably trigger another card deal, but to save API calls in a real game we might just shuffle existing deck.
        )
    }

    private fun evaluateWinner(activePlayers: List<Player>, communityCards: List<SkillCard>): Player {
        val evaluated = activePlayers.associateWith { HandEvaluator.evaluate(it.holeCards, communityCards) }
        val bestHand = evaluated.values.maxOrNull() ?: HandRank.HighCard
        val bestPlayers = evaluated.filter { it.value == bestHand }.keys.toList()
        return bestPlayers.first() // If tie, first player wins for simplicity
    }
}

object HandEvaluator {
    fun evaluate(holeCards: List<SkillCard>, communityCards: List<SkillCard>): HandRank {
        val allCards = holeCards + communityCards
        if (allCards.isEmpty()) return HandRank.HighCard

        val suitCounts = allCards.groupBy { it.suit }.mapValues { it.value.size }
        val isFlush = suitCounts.values.any { it >= 5 }
        if (isFlush) return HandRank.Flush

        val rankCounts = allCards.groupBy { it.rank }.mapValues { it.value.size }
        val distinctRanks = rankCounts.keys
        val isStraight = distinctRanks.containsAll(listOf(1, 2, 3, 4, 5))
        if (isStraight) return HandRank.Straight

        val maxSameRank = rankCounts.values.maxOrNull() ?: 1
        if (maxSameRank >= 3) return HandRank.ThreeOfAKind
        if (maxSameRank == 2) return HandRank.OnePair

        return HandRank.HighCard
    }
}
