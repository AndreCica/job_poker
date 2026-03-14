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
import androidx.compose.ui.unit.dp
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource("background_room_test.png"),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Phase and Pot Info
            Text(phaseLabel, style = MaterialTheme.typography.h5, color = Color.White)
            Text("Pot: ${gameState.pot} | Round: ${gameState.round}/10", color = Color.White, style = MaterialTheme.typography.h6)

            // NPCs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gameState.players.filter { !it.isHuman }.forEach { npc ->
                    PlayerSection(
                        name = npc.name,
                        chips = npc.chips,
                        hasFolded = npc.hasFolded,
                        cards = if (npc.hasFolded) emptyList() else listOf(null, null) // Face down cards
                    )
                }
            }

            // Community Cards
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) {
                gameState.communityCards.forEach { card ->
                    PlayingCard(card)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // Human Player
            val human = gameState.players.first { it.isHuman }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerSection(
                    name = "You",
                    chips = human.chips,
                    hasFolded = human.hasFolded,
                    cards = human.holeCards
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
fun PlayerSection(name: String, chips: Int, hasFolded: Boolean, cards: List<SkillCard?>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Text(name, color = Color.White, style = MaterialTheme.typography.subtitle1)
        Text("Chips: $chips", color = Color.LightGray)
        if (hasFolded) {
            Text("FOLDED", color = Color.Red, style = MaterialTheme.typography.h6)
        } else {
            Row {
                cards.forEach { card ->
                    if (card == null) {
                        Surface(
                            modifier = Modifier.size(60.dp, 90.dp).padding(2.dp).border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E3A8A) // back pattern color
                        ) {}
                    } else {
                        PlayingCard(card, modifier = Modifier.size(60.dp, 90.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PlayingCard(card: SkillCard, modifier: Modifier = Modifier.size(80.dp, 120.dp)) {
    Surface(
        modifier = modifier.padding(2.dp).border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
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
