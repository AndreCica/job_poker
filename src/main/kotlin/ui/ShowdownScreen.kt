package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import game.HandEvaluator
import models.GameState

@Composable
fun ShowdownScreen(gameState: GameState, onNextRound: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2E4053)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("The Offer: Showdown", style = MaterialTheme.typography.h4, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Community Cards:", color = Color.White)
        Row(modifier = Modifier.padding(8.dp)) {
            gameState.communityCards.forEach { card ->
                PlayingCard(card, modifier = Modifier.size(60.dp, 90.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Evaluate all hands
        val evaluated = gameState.players.filter { !it.hasFolded }.map { player ->
            player to HandEvaluator.evaluate(player.holeCards, gameState.communityCards)
        }
        
        val bestHand = evaluated.maxByOrNull { it.second }?.second
        val winners = evaluated.filter { it.second == bestHand }.map { it.first }

        evaluated.forEach { (player, rank) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(player.name, color = if (player in winners) Color.Yellow else Color.White, style = MaterialTheme.typography.h6)
                    Text("Hand: ${rank.displayName}", color = Color.LightGray)
                }
                Row {
                    player.holeCards.forEach { card ->
                        PlayingCard(card, modifier = Modifier.size(50.dp, 75.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (winners.isNotEmpty()) {
            if (it.name == "You") {Text("CONGRATS! You win ${gameState.pot} chips!", color = Color.Green, style = MaterialTheme.typography.h5)}
            else {
                Text("WINNER: ${winners.joinToString { it.name }} wins ${gameState.pot} chips!", color = Color.Yellow, style = MaterialTheme.typography.h5)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNextRound) {
            Text(if (gameState.round >= 10) "Game Over" else "Next Round")
        }
    }
}
