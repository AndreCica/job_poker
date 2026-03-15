package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

@Composable
fun LobbyScreen(onStartGame: (String, String, List<String>) -> Unit) {
    var jobDescription by remember {
        mutableStateOf(
                """
Role: Senior Project Manager

Location: Remote / Hybrid

Reports to: Director of Operations

Position Summary

We are seeking a disciplined and strategic Senior Project Manager to oversee complex, high-impact projects from inception to completion. You will be responsible for coordinating cross-functional teams, managing budgets, and ensuring that all project deliverables meet our high standards of quality and efficiency.

Key Responsibilities

Planning: Define project scope, goals, and deliverables that support business goals in collaboration with senior management.

Execution: Lead the planning and implementation of projects; facilitate the definition of service levels and customer requirements.

Monitoring: Track project milestones and deliverables using appropriate tools (e.g., Jira, Asana, or MS Project).

Communication: Present reports defining project progress, problems, and solutions to stakeholders.

Qualifications

Bachelor’s degree in Business, Engineering, or a related field.

5+ years of experience in project management.

PMP or PRINCE2 certification is highly preferred.

Strong familiarity with Agile and Waterfall methodologies.
"""
        )
    }
    var userResume by remember { mutableStateOf("") }
    var npc1Resume by remember {
        mutableStateOf("Overconfident backend dev who thinks they know it all.")
    }
    var npc2Resume by remember {
        mutableStateOf("Methodical architect who over-engineers everything.")
    }
    var npc3Resume by remember {
        mutableStateOf("Desperate junior developer willing to do anything.")
    }
    var isLoading by remember { mutableStateOf(false) }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .paint(
                                    painter = painterResource("lobby_background_1.png"),
                                    contentScale = ContentScale.Crop
                            )
                            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("The Interview Table", color = Color.White, style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                StyledTextField(
                        value = jobDescription,
                        onValueChange = { jobDescription = it },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Job Description")
                                Text(" *", color = MaterialTheme.colors.error)
                            }
                        },
                        isError = jobDescription.isBlank(),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                StyledTextField(
                        value = userResume,
                        onValueChange = { userResume = it },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Your Resume")
                                Text(" *", color = MaterialTheme.colors.error)
                            }
                        },
                        isError = userResume.isBlank(),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
                Button(
                        onClick = {
                            try {
                                val chooser = JFileChooser()
                                chooser.dialogTitle = "Select Resume PDF"
                                chooser.fileFilter = FileNameExtensionFilter("PDF Documents", "pdf")
                                chooser.isAcceptAllFileFilterUsed = false

                                val result = chooser.showOpenDialog(null)
                                val selectedFile =
                                        if (result == JFileChooser.APPROVE_OPTION)
                                                chooser.selectedFile
                                        else null
                                println("Selected file: ${selectedFile?.absolutePath}")

                                if (selectedFile != null) {
                                    if (!selectedFile.exists() || !selectedFile.canRead()) {
                                        println(
                                                "File exists: ${selectedFile.exists()}, Can read: ${selectedFile.canRead()}"
                                        )
                                        return@Button
                                    }

                                    PDDocument.load(selectedFile).use { doc ->
                                        val text = PDFTextStripper().getText(doc)
                                        if (text.isNullOrBlank()) {
                                            println("Warning: Extracted text is empty or null")
                                        }
                                        userResume = text.trim()
                                        println(
                                                "Successfully extracted ${userResume.length} characters"
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error during PDF upload: ${e.message}")
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                ) { Text("Upload PDF for Your Resume") }
                if (userResume.isBlank()) {
                    Text(
                            "Required field",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                StyledTextField(
                        value = npc1Resume,
                        onValueChange = { npc1Resume = it },
                        label = { Text("Chad's Resume") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
                StyledTextField(
                        value = npc2Resume,
                        onValueChange = { npc2Resume = it },
                        label = { Text("Priya's Resume") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
                StyledTextField(
                        value = npc3Resume,
                        onValueChange = { npc3Resume = it },
                        label = { Text("Kevin's Resume") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val resumeIsValid = isResumeValid(userResume)
        val canStart = jobDescription.isNotBlank() && resumeIsValid && !isLoading
        if (userResume.isNotBlank() && !resumeIsValid) {
            Text(
                    "Please enter a valid resume (not random letters).",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Button(
                onClick = {
                    isLoading = true
                    onStartGame(
                            jobDescription,
                            userResume,
                            listOf(npc1Resume, npc2Resume, npc3Resume)
                    )
                },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colors.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating Deck & Dealing...")
            } else {
                Text("Start Interview")
            }
        }
    }
}

fun isResumeValid(resume: String): Boolean {
    val cleaned = resume.trim()
    if (cleaned.length < 40) return false
    val words = cleaned.split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (words.size < 7) return false
    val letters = cleaned.count { it.isLetter() }
    if (letters < cleaned.length * 0.5) return false
    val uniqueLetters = cleaned.filter { it.isLetterOrDigit() }.toSet().size
    if (uniqueLetters < 10) return false
    if (Regex("([a-zA-Z])\\1{4,}").containsMatchIn(cleaned)) return false
    if (Regex("[A-Z][a-z]+ [A-Z][a-z]+ [A-Z][a-z]+").containsMatchIn(cleaned)) return true
    return words.any { it.length > 4 }
}

@Composable
fun StyledTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        isError: Boolean = false
) {
    Box(
            modifier =
                    modifier.paint(
                            painter = painterResource("square.png"),
                            contentScale = ContentScale.FillBounds
                    )
    ) {
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                isError = isError,
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
                colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent
                        )
        )
    }
}
