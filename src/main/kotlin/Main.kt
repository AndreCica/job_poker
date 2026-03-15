import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import api.GeminiClient
import game.GameEngine
import kotlinx.coroutines.launch
import models.GamePhase
import models.Player
import ui.GameScreen
import ui.LobbyScreen
import ui.LogScreen
import ui.ShowdownScreen
import ui.TitleScreen

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1280.dp, 720.dp))
    var isPaused by remember { mutableStateOf(false) }

    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        title = "The Interview Table",
        resizable = false,
        onKeyEvent = {
            if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                isPaused = !isPaused
                true
            } else false
        }
    ) {
        MaterialTheme {
            val scope = rememberCoroutineScope()
            val gameEngine = remember { GameEngine(scope) }
            val gameState by gameEngine.gameState.collectAsState()
            val apiClient = remember { GeminiClient() }
            var showLogWindow by remember { mutableStateOf(false) }

            when (gameState.phase) {
                GamePhase.TITLE -> {
                    TitleScreen(onStart = { gameEngine.startFromTitle() })
                }
                GamePhase.LOBBY -> {
                    LobbyScreen(
                        onStartGame = { jobDescription, userResume, npcResumes ->
                            scope.launch {
                                val resumes = listOf(userResume) + npcResumes
                                val response = apiClient.generateGameData(jobDescription, resumes)
                                val currentPlayers = gameState.players.filter { it.chips > 0 }
                                val playersToUse = if (currentPlayers.isNotEmpty() && currentPlayers.any { it.isHuman }) {
                                    currentPlayers.map { it.copy(hasFolded = false, currentBet = 0, holeCards = emptyList()) }
                                } else {
                                    listOf(
                                        Player(name = "You", isHuman = true),
                                        Player(name = "Chad (Overconfident)", isHuman = false),
                                        Player(name = "Priya (Methodical)", isHuman = false),
                                        Player(name = "Kevin (Desperate)", isHuman = false)
                                    )
                                }
                                gameEngine.startGame(playersToUse, response)
                            }
                        }
                    )
                }
                GamePhase.SHOWDOWN -> {
                    ShowdownScreen(
                        gameState = gameState,
                        onNextRound = { gameEngine.nextRound() },
                        onShowLog = { showLogWindow = true }
                    )
                }
                GamePhase.GAME_OVER -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val didWin = gameState.players.find { it.isHuman }?.let { it.chips > 0 } ?: false
                            Text(
                                if (didWin) "Game Over - You survived the interview process!" else "Game Over - You were rejected.",
                                style = MaterialTheme.typography.h3, 
                                color = if(didWin) Color.Green else Color.Red
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(onClick = { gameEngine.startFromTitle() }) {
                                Text("Return to Title")
                            }
                        }
                    }
                }
                else -> {
                    GameScreen(
                        gameState = gameState,
                        onHumanAction = { action, betAmount -> gameEngine.humanAction(action, betAmount) },
                        onShowLog = { showLogWindow = true }
                    )
                }
            }
            if (showLogWindow) {
                LogScreen(logs = gameState.logs, onClose = { showLogWindow = false })
            }

            if (isPaused) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Paused", color = Color.White, style = MaterialTheme.typography.h3)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { isPaused = false }) {
                            Text("Resume")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = ::exitApplication) {
                            Text("Quit")
                        }
                    }
                }
            }
        }
    }
}
