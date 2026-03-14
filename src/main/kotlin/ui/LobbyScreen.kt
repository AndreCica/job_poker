package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.FileDialog
import java.awt.Frame
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

@Composable
fun LobbyScreen(onStartGame: (String, String, List<String>) -> Unit) {
    var jobDescription by remember { mutableStateOf("""
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
""" ) }
    var userResume by remember { mutableStateOf("") }
    val frame = Frame() // Needed for FileDialog
    var npc1Resume by remember { mutableStateOf("Overconfident backend dev who thinks they know it all.") }
    var npc2Resume by remember { mutableStateOf("Methodical architect who over-engineers everything.") }
    var npc3Resume by remember { mutableStateOf("Desperate junior developer willing to do anything.") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("The Interview Table", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                OutlinedTextField(
                    value = jobDescription,
                    onValueChange = {},
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Job Description")
                            Text(" *", color = MaterialTheme.colors.error)
                        }
                    },
                    isError = jobDescription.isBlank(),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
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
                Button(onClick = {
                    try {
                        val dialog = FileDialog(frame, "Select Resume PDF", FileDialog.LOAD)
                        dialog.file = "*.pdf" // Hint for some systems
                        dialog.setFilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }
                        dialog.isVisible = true
                        
                        val selectedFile = dialog.files.firstOrNull()
                        println("Selected file: ${selectedFile?.absolutePath}")
                        
                        if (selectedFile != null) {
                            if (!selectedFile.exists() || !selectedFile.canRead()) {
                                println("File exists: ${selectedFile.exists()}, Can read: ${selectedFile.canRead()}")
                                return@Button
                            }
                            
                            PDDocument.load(selectedFile).use { doc ->
                                val text = PDFTextStripper().getText(doc)
                                if (text.isNullOrBlank()) {
                                    println("Warning: Extracted text is empty or null")
                                }
                                userResume = text.trim()
                                println("Successfully extracted ${userResume.length} characters")
                            }
                        }
                    } catch (e: Exception) {
                        println("Error during PDF upload: ${e.message}")
                        e.printStackTrace()
                    }
                }, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Upload PDF for Your Resume")
                }
                if (userResume.isBlank()) {
                    Text("Required field", color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                OutlinedTextField(
                    value = npc1Resume,
                    onValueChange = { npc1Resume = it },
                    label = { Text("Chad's Resume") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = npc2Resume,
                    onValueChange = { npc2Resume = it },
                    label = { Text("Priya's Resume") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = npc3Resume,
                    onValueChange = { npc3Resume = it },
                    label = { Text("Kevin's Resume") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val canStart = jobDescription.isNotBlank() && userResume.isNotBlank() && !isLoading
        Button(
            onClick = {
                isLoading = true
                onStartGame(jobDescription, userResume, listOf(npc1Resume, npc2Resume, npc3Resume))
            },
            enabled = canStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colors.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating Deck & Dealing...")
            } else {
                Text("Start Interview")
            }
        }
    }
}
