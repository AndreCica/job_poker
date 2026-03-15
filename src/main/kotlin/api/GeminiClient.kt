package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import models.GeminiResponse
import models.SkillCard

class GeminiClient {
    private val client =
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                            }
                    )
                }
            }

    private val apiKey = System.getProperty("GEMINI_API_KEY") ?: error("API key not set")
    private val url =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

    suspend fun generateGameData(jobDescription: String, resumes: List<String>): GeminiResponse {
        val prompt = buildPrompt(jobDescription, resumes)

        try {
            val response: HttpResponse =
                    client.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(
                                buildJsonObject {
                                    put(
                                            "contents",
                                            buildJsonArray {
                                                add(
                                                        buildJsonObject {
                                                            put(
                                                                    "parts",
                                                                    buildJsonArray {
                                                                        add(
                                                                                buildJsonObject {
                                                                                    put(
                                                                                            "text",
                                                                                            prompt
                                                                                    )
                                                                                }
                                                                        )
                                                                    }
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                }
                        )
                    }

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = response.bodyAsText()
                val parsed = Json.parseToJsonElement(jsonResponse)

                // Extract text from Gemini response format
                val candidateText =
                        parsed.jsonObject["candidates"]
                                ?.jsonArray
                                ?.get(0)
                                ?.jsonObject
                                ?.get("content")
                                ?.jsonObject
                                ?.get("parts")
                                ?.jsonArray
                                ?.get(0)
                                ?.jsonObject
                                ?.get("text")
                                ?.jsonPrimitive
                                ?.content
                                ?: ""

                val cleanJson = candidateText.replace("```json", "").replace("```", "").trim()
                return Json { ignoreUnknownKeys = true }.decodeFromString<GeminiResponse>(cleanJson)
            }
        } catch (e: Exception) {
            println("Gemini API Error: ${e.message}")
        }

        return generateFallbackData()
    }

    private fun buildPrompt(jobDescription: String, resumes: List<String>): String {
        return """
            You are a game designer for a poker-like card game based on job interviews.
            
            Given this Job Description: $jobDescription
            and these candidate resumes: 
            ${resumes.mapIndexed { i, r -> "RESUME_${i + 1}: $r" }.joinToString("\n")}
            
            Convert the bullet points of the job requirements into 4 thematic suits (Engineering, Design, Management, Operations) and 5 levels of their ranks (1-5).
            The description and title of the 20 cards in the deck should be named and described strictly based on these job requirements.
            If the resume is not an appropriate format, then randomly generate 20 cards with random suits and ranks.
            
            Then, convert the skills and bullet points from each candidate's resume into cards.
            Generate exactly 5 cards based on RESUME_1 and store them in resumeCards under "player0".
            Generate exactly 5 cards based on RESUME_2 and store them in resumeCards under "player1".
            Generate exactly 5 cards based on RESUME_3 and store them in resumeCards under "player2".
            Generate exactly 5 cards based on RESUME_4 and store them in resumeCards under "player3".

            If the resume is not an appropriate format, then randomly generate 20 cards with random suits and ranks.
            
            Respond ONLY with a JSON object (no markdown) in this exact schema:
            {
              "jobCards": [{"suit":"SuitName1","rank":1,"title":"Card Title","description":"One sentence skill"}],  
              "resumeCards": {
                "player0": [{"suit":"SuitName1","rank":1,"title":"Card Title","description":"One sentence skill"}],
                "player1": [],
                "player2": [],
                "player3": []
              }
            }
            Ensure jobCards has exactly 20 cards.
            Ensure each array in resumeCards has exactly 5 cards.
        """.trimIndent()
    }

    private fun generateFallbackData(): GeminiResponse {
        val suits = listOf("Engineering", "Design", "Management", "Operations")
        val deck = mutableListOf<SkillCard>()
        val titles =
                listOf("Intern Task", "Junior Fix", "Mid Feature", "Senior Arch", "Lead Vision")
        var index = 0
        for (suit in suits) {
            for (rank in 1..5) {
                deck.add(
                        SkillCard(
                                suit,
                                rank,
                                "$suit ${titles[rank-1]}",
                                "A standard skill in $suit."
                        )
                )
                index++
            }
        }

        val resumeCards =
                mapOf(
                        "player0" to
                                listOf(
                                        SkillCard("Engineering", 2, "Test0", "Test"),
                                        SkillCard("Design", 3, "Test0", "Test")
                                ),
                        "player1" to
                                listOf(
                                        SkillCard("Engineering", 2, "Test1", "Test"),
                                        SkillCard("Design", 3, "Test1", "Test")
                                ),
                        "player2" to
                                listOf(
                                        SkillCard("Engineering", 2, "Test2", "Test"),
                                        SkillCard("Design", 3, "Test2", "Test")
                                ),
                        "player3" to
                                listOf(
                                        SkillCard("Engineering", 2, "Test3", "Test"),
                                        SkillCard("Design", 3, "Test3", "Test")
                                )
                )

        return GeminiResponse(deck, resumeCards)
    }
}
