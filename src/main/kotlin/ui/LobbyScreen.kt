package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LobbyScreen(onStartGame: (String, String, List<String>) -> Unit) {
    var jobDescription by remember { mutableStateOf("") }
    var userResume by remember { mutableStateOf("") }
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
                    onValueChange = { jobDescription = it },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = userResume,
                    onValueChange = { userResume = it },
                    label = { Text("Your Resume") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
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

        Button(
            onClick = {
                isLoading = true
                onStartGame(jobDescription, userResume, listOf(npc1Resume, npc2Resume, npc3Resume))
            },
            enabled = !isLoading,
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
