package models

import kotlinx.serialization.Serializable

@Serializable
data class SkillCard(val suit: String, val rank: Int, val title: String, val description: String)
// rank 1=Intern, 2=Junior, 3=Mid, 4=Senior, 5=Lead

@Serializable
data class Player(
    val name: String,
    val isHuman: Boolean,
    var chips: Int = 100_000,
    var holeCards: List<SkillCard> = emptyList(),
    var hasFolded: Boolean = false,
    var currentBet: Int = 0 
)

data class GameState(
    val players: List<Player>,
    var communityCards: List<SkillCard> = emptyList(),
    var pot: Int = 0,
    var round: Int = 0,
    var phase: GamePhase = GamePhase.TITLE,
    var communityCardsRevealed: Int = 0,
    var currentUserIndex: Int = 0
)

enum class GamePhase { TITLE, LOBBY, PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN, GAME_OVER }

sealed class HandRank(val displayName: String, val value: Int) : Comparable<HandRank> {
    object HighCard : HandRank("The Nepo Baby", 1)
    object OnePair : HandRank("Transferable Skills", 2)
    object ThreeOfAKind : HandRank("The Strong Fit", 3)
    object Straight : HandRank("The Generalist", 4)
    object Flush : HandRank("The Department Dream", 5)

    override fun compareTo(other: HandRank): Int {
        return this.value.compareTo(other.value)
    }
}

// Needed for the Gemini Response mapping
@Serializable
data class GeminiResponse(
    val suits: List<String>,
    val deck: List<SkillCard>,
    val holeCards: Map<String, List<Int>>
)
dd
