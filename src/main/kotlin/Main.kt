import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.GeminiClient
import game.GameEngine
import kotlinx.coroutines.launch
import models.GamePhase
import models.Player
import ui.GameScreen
import ui.LobbyScreen
import ui.ShowdownScreen
import ui.TitleScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "The Interview Table") {
        MaterialTheme {
            val scope = rememberCoroutineScope()
            val gameEngine = remember { GameEngine(scope) }
            val gameState by gameEngine.gameState.collectAsState()
            val apiClient = remember { GeminiClient() }

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
                                val players = listOf(
                                    Player(name = "You", isHuman = true),
                                    Player(name = "Chad (Overconfident)", isHuman = false),
                                    Player(name = "Priya (Methodical)", isHuman = false),
                                    Player(name = "Kevin (Desperate)", isHuman = false)
                                )
                                gameEngine.startGame(players, response.deck, response.holeCards)
                            }
                        }
                    )
                }
                GamePhase.SHOWDOWN -> {
                    ShowdownScreen(
                        gameState = gameState,
                        onNextRound = { gameEngine.nextRound() }
                    )
                }
                GamePhase.GAME_OVER -> {
                    LobbyScreen(
                        onStartGame = { jobDescription, userResume, npcResumes ->
                            scope.launch {
                                val resumes = listOf(userResume) + npcResumes
                                val response = apiClient.generateGameData(jobDescription, resumes)
                                val players = listOf(
                                    Player(name = "You", isHuman = true),
                                    Player(name = "Chad (Overconfident)", isHuman = false),
                                    Player(name = "Priya (Methodical)", isHuman = false),
                                    Player(name = "Kevin (Desperate)", isHuman = false)
                                )
                                gameEngine.startGame(players, response.deck, response.holeCards)
                            }
                        }
                    )
                }
                else -> {
                    GameScreen(
                        gameState = gameState,
                        onHumanAction = { action -> gameEngine.humanAction(action) }
                    )
                }
            }
        }
    }
}
