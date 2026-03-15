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
    private var fullResponse: GeminiResponse? = null

    private fun logAction(message: String) {
        val currentLogs = gameState.value.logs.toMutableList()
        currentLogs.add(message)
        gameState.value = gameState.value.copy(logs = currentLogs)
    }

    fun startGame(players: List<Player>, response: GeminiResponse) {
        fullResponse = response
        val jobDeck = response.jobCards.toMutableList()
        jobDeck.shuffle()
        
        val dealtPlayers = players.mapIndexed { index, player ->
            val pool = when (index) {
                0 -> response.resumeCards["player0"]
                1 -> response.resumeCards["player1"]
                2 -> response.resumeCards["player2"]
                3 -> response.resumeCards["player3"]
                else -> emptyList()
            } ?: emptyList()
            
            val shuffledPool = pool.shuffled()
            val cards = shuffledPool.take(2)
            player.copy(holeCards = cards, hasFolded = false, currentBet = 0)
        }
        
        deck = jobDeck

        // Deal 2 community cards face-up at game start
        val initialCommunity = deck.take(2)
        deck = deck.drop(2)

        gameState.value = GameState(
            players = dealtPlayers,
            communityCards = initialCommunity,
            pot = 0,
            round = 1,
            phase = GamePhase.PRE_FLOP,
            communityCardsRevealed = 2,
            currentUserIndex = 0,
            logs = listOf("Game started! Dealing hole cards.", "Community cards: ${initialCommunity.joinToString { it.title }}")
        )
    }

    fun humanAction(action: PlayerAction, betAmount: Int = 0) {
        val state = gameState.value
        if (state.currentUserIndex != 0) return

        val currentPlayers = state.players.toMutableList()
        val human = currentPlayers[0]

        when (action) {
            PlayerAction.FOLD -> {
                currentPlayers[0] = human.copy(hasFolded = true)
                logAction("You folded.")
            }
            PlayerAction.CHECK_CALL -> {
                logAction("You checked/called.")
            }
            PlayerAction.BET -> {
                if (human.chips >= betAmount && betAmount > 0) {
                    currentPlayers[0] = human.copy(
                        chips = human.chips - betAmount,
                        currentBet = human.currentBet + betAmount
                    )
                    gameState.value = state.copy(pot = state.pot + betAmount, players = currentPlayers)
                    logAction("You bet $betAmount chips.")
                } else {
                    return // Invalid bet, don't advance the turn
                }
            }
        }

        gameState.value = gameState.value.copy(players = currentPlayers, currentUserIndex = -1)
        
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
                    logAction("${npc.name} folded.")
                }
                1 -> { // Check/Call
                    logAction("${npc.name} checked/called.")
                }
                2 -> { // Bet
                    val maxBet = npc.chips
                    val betAmount = if (maxBet > 100) Random.nextInt(100, maxBet + 1) else if (maxBet > 0) maxBet else 0
                    if (betAmount > 0) {
                        currentPlayers[i] = npc.copy(
                            chips = npc.chips - betAmount,
                            currentBet = npc.currentBet + betAmount
                        )
                        currentPot += betAmount
                        logAction("${npc.name} bet $betAmount chips.")
                    } else {
                        logAction("${npc.name} checked/called.")
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
                val card = deck.take(1).first()
                newCommunityCards.add(card)
                deck = deck.drop(1)
                cardsRevealed = 3
                logAction("Dealing Flop: ${card.title}")
            }
            GamePhase.TURN -> {
                val card = deck.take(1).first()
                newCommunityCards.add(card)
                deck = deck.drop(1)
                cardsRevealed = 4
                logAction("Dealing Turn: ${card.title}")
            }
            GamePhase.RIVER -> {
                val card = deck.take(1).first()
                newCommunityCards.add(card)
                deck = deck.drop(1)
                cardsRevealed = 5
                logAction("Dealing River: ${card.title}")
            }
            GamePhase.SHOWDOWN -> {
                logAction("Entering Showdown.")
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

        // If the human player has folded, auto-skip their turn
        if (currentPlayers.isNotEmpty() && currentPlayers[0].hasFolded) {
            gameState.value = gameState.value.copy(currentUserIndex = -1)
            coroutineScope.launch {
                processNpcTurns()
            }
        }
    }

    private fun endRound(defaultWinner: Player? = null) {
        val state = gameState.value
        val players = state.players.toMutableList()
        
        val winner = defaultWinner ?: evaluateWinner(players.filter { !it.hasFolded }, state.communityCards)
        
        val winnerIndex = players.indexOfFirst { it.name == winner.name }
        if (winnerIndex >= 0) {
            val winnerName = players[winnerIndex].name
            players[winnerIndex] = players[winnerIndex].copy(chips = players[winnerIndex].chips + state.pot)
            logAction("$winnerName wins the pot of ${state.pot} chips!")
        }

        gameState.value = state.copy(
            phase = GamePhase.SHOWDOWN,
            players = players
        )
    }

    fun nextRound() {
        val state = gameState.value
        val currentPlayers = state.players.filter { it.chips > 0 }
        
        // Check for 10-round limit or not enough players
        if (state.round >= 10 || currentPlayers.count { it.isHuman } == 0 || currentPlayers.size <= 1) {
            val response = fullResponse ?: return // ensure we have our original response to potentially grab data, but transition to game over
            gameState.value = state.copy(phase = GamePhase.GAME_OVER)
            return
        }
        
        val response = fullResponse ?: return
        
        // Reset for next round
        val resetPlayers = currentPlayers.mapIndexed { index, player -> 
            val pool = when (index) {
                0 -> response.resumeCards["player0"]
                1 -> response.resumeCards["player1"]
                2 -> response.resumeCards["player2"]
                3 -> response.resumeCards["player3"]
                else -> emptyList()
            } ?: emptyList()
            
            val shuffledPool = pool.shuffled()
            val cards = shuffledPool.take(2)
            // Need to match exactly by Original Player Name mapped to static index, 
            // but the original indices might drift if players get eliminated! 
            // So we instead look up by the name to pull the correct resume from the dict.
            val assignedPool = when {
                player.name == "You" -> response.resumeCards["player0"]
                player.name.contains("Chad") -> response.resumeCards["player1"]
                player.name.contains("Priya") -> response.resumeCards["player2"]
                player.name.contains("Kevin") -> response.resumeCards["player3"]
                else -> emptyList()
            } ?: emptyList()
            val specificShuffled = assignedPool.shuffled()
            val specificCards = specificShuffled.take(2)
            player.copy(hasFolded = false, currentBet = 0, holeCards = specificCards)
        }

        // Reshuffle the full job deck afresh
        val freshJobDeck = response.jobCards.toMutableList()
        freshJobDeck.shuffle()
        deck = freshJobDeck

        val initialCommunity = deck.take(2)
        deck = deck.drop(2)

        gameState.value = state.copy(
            players = resetPlayers,
            communityCards = initialCommunity,
            communityCardsRevealed = 2,
            pot = 0,
            round = state.round + 1,
            phase = GamePhase.PRE_FLOP,
            currentUserIndex = 0,
            logs = state.logs + listOf(
                "Starting Round ${state.round + 1}", 
                "Dealing hole cards.", 
                "Community cards: ${initialCommunity.joinToString { it.title }}"
            )
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
