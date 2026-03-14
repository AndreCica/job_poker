package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import models.GeminiResponse
import models.SkillCard
import kotlin.random.Random

class GeminiClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                isLenient = true
            })
        }
    }

    private val apiKey = "AIzaSyAk6MCVdGIkZ0G8Q55uoexhCcY-U3Z27J0"
    private val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

    suspend fun generateGameData(jobDescription: String, resumes: List<String>): GeminiResponse {
        val prompt = buildPrompt(jobDescription, resumes)
        
        try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("contents", buildJsonArray {
                            add(buildJsonObject {
                                put("parts", buildJsonArray {
                                    add(buildJsonObject {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                    }
                )
            }

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = response.bodyAsText()
                val parsed = Json.parseToJsonElement(jsonResponse)
                
                // Extract text from Gemini response format
                val candidateText = parsed.jsonObject["candidates"]?.jsonArray?.get(0)?.jsonObject
                    ?.get("content")?.jsonObject?.get("parts")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""

                val cleanJson = candidateText.replace("```json", "").replace("```", "").trim()
                return Json { ignoreUnknownKeys = true }.decodeFromString(GeminiResponse.serializer(), cleanJson)
            }
        } catch (e: Exception) {
            println("Gemini API Error: ${e.message}")
        }
        
        return generateFallbackData()
    }

    private fun buildPrompt(jobDescription: String, resumes: List<String>): String {
        return """
            Given this Job Description: $jobDescription
            and these candidate resumes: 
            ${resumes.mapIndexed { i, r -> "RESUME_${i + 1}: $r" }.joinToString("\n")}
            
            Respond ONLY with a JSON object (no markdown) in this exact schema:
            {
              "suits": ["SuitName1","SuitName2","SuitName3","SuitName4"],
              "deck": [{"suit":"SuitName1","rank":1,"title":"Card Title","description":"One sentence skill"}],  
              "holeCards": {
                "player0": [0, 1],
                "player1": [2, 3],
                "player2": [4, 5],
                "player3": [6, 7]
              }
            }
            Ensure the deck has exactly 20 cards: 5 ranks (1 to 5) × 4 suits.
            Ensure holeCards contains arrays of 2 distinct indices (0-19) for player0 through player3.
        """.trimIndent()
    }

    private fun generateFallbackData(): GeminiResponse {
        val suits = listOf("Engineering", "Design", "Management", "Operations")
        val deck = mutableListOf<SkillCard>()
        val titles = listOf("Intern Task", "Junior Fix", "Mid Feature", "Senior Arch", "Lead Vision")
        var index = 0
        for (suit in suits) {
            for (rank in 1..5) {
                deck.add(SkillCard(suit, rank, "$suit ${titles[rank-1]}", "A standard skill in $suit."))
                index++
            }
        }
        
        val remainingIndices = (0..19).toMutableList()
        remainingIndices.shuffle(Random(42)) // Deterministic fallback
        
        val holeCards = mapOf(
            "player0" to listOf(remainingIndices[0], remainingIndices[1]),
            "player1" to listOf(remainingIndices[2], remainingIndices[3]),
            "player2" to listOf(remainingIndices[4], remainingIndices[5]),
            "player3" to listOf(remainingIndices[6], remainingIndices[7])
        )

        return GeminiResponse(suits, deck, holeCards)
    }
}
