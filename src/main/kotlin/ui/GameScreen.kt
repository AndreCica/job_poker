package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import game.PlayerAction
import models.GamePhase
import models.GameState
import models.SkillCard

@Composable
fun GameScreen(gameState: GameState, onHumanAction: (PlayerAction) -> Unit) {
    val phaseLabel = when (gameState.phase) {
        GamePhase.PRE_FLOP -> "Pre-Interview: Check your skills"
        GamePhase.FLOP -> "Tech Screen: The Flop"
        GamePhase.TURN -> "Culture Fit: The Turn"
        GamePhase.RIVER -> "Final Interview: The River"
        else -> ""
    }

    // Card sizing (can be tweaked for your pixel art assets)
    val npcCardW = 60.dp
    val npcCardH = 90.dp
    val communityCardW = 80.dp
    val communityCardH = 120.dp
    val communityRowH = 140.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image with fallback
        val backgroundPainter = runCatching {
            painterResource("background_room_test.png")
        }.getOrNull()

        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF35654D)))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Phase and Pot Info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(phaseLabel, style = MaterialTheme.typography.h5, color = Color.White)
                // TextButton(onClick = onShowLog) {
                //     Text("Log", color = Color.White)
                // }
            }
            Text("Pot: ${gameState.pot} | Round: ${gameState.round}/10", color = Color.White, style = MaterialTheme.typography.h6)

            // NPCs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gameState.players.forEachIndexed { index, player ->
                    if (!player.isHuman) {
                        PlayerSection(
                            name = player.name,
                            chips = player.chips,
                            currentBet = player.currentBet,
                            hasFolded = player.hasFolded,
                            cards = if (player.hasFolded) emptyList() else listOf(null, null),
                            isActive = index == gameState.currentUserIndex,
                            cardWidth = npcCardW,
                            cardHeight = npcCardH
                        )
                    }
                }
            }

            // Community Cards
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().height(communityRowH)
            ) {
                gameState.communityCards.forEach { card ->
                    PlayingCard(card, modifier = Modifier.size(communityCardW, communityCardH))
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // Human Player
            val humanIndex = gameState.players.indexOfFirst { it.isHuman }
            val human = gameState.players[humanIndex]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerSection(
                    name = "You",
                    chips = human.chips,
                    currentBet = human.currentBet,
                    hasFolded = human.hasFolded,
                    cards = human.holeCards,
                    isActive = humanIndex == gameState.currentUserIndex,
                    cardWidth = npcCardW,
                    cardHeight = npcCardH
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = { onHumanAction(PlayerAction.FOLD) }, enabled = !human.hasFolded) {
                        Text("Fold")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onHumanAction(PlayerAction.CHECK_CALL) }, enabled = !human.hasFolded) {
                        Text("Check")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onHumanAction(PlayerAction.BET) }, enabled = !human.hasFolded && human.chips >= 10000) {
                        Text("Bet 10k")
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerSection(
    name: String,
    chips: Int,
    currentBet: Int,
    hasFolded: Boolean,
    cards: List<SkillCard?>,
    isActive: Boolean = false,
    cardWidth: Dp = 60.dp,
    cardHeight: Dp = 90.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(name, color = if (isActive) Color(0xFFFFD700) else Color.White, style = MaterialTheme.typography.subtitle1)
        Text("Chips: $chips", color = Color.LightGray)
        Text("Bet: $currentBet", color = if (currentBet > 0) Color.Yellow else Color.Gray, style = MaterialTheme.typography.caption)
        if (hasFolded) {
            Text("FOLDED", color = Color.Red, style = MaterialTheme.typography.h6)
        } else {
            Row {
                cards.forEach { card ->
                    if (card == null) {
                        val cardBorder = if (isActive) Modifier.border(4.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                         else Modifier.border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        Surface(
                            modifier = Modifier.size(cardWidth, cardHeight).padding(2.dp).then(cardBorder),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E3A8A)
                        ) {}
                    } else {
                        PlayingCard(card, modifier = Modifier.size(cardWidth, cardHeight), isActive = isActive)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayingCard(card: SkillCard, modifier: Modifier = Modifier.size(80.dp, 120.dp), isActive: Boolean = false) {
    val borderColor = if (isActive) Color(0xFFFFD700) else Color.Black
    val borderWidth = if (isActive) 4.dp else 1.dp

    Surface(
        modifier = modifier.padding(2.dp).border(borderWidth, borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(4.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${card.rank}", style = MaterialTheme.typography.subtitle2)
            Text(card.suit.take(3).uppercase(), style = MaterialTheme.typography.overline)
            Text(card.title, fontSize = MaterialTheme.typography.caption.fontSize, textAlign = TextAlign.Center)
        }
    }
}
