package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitleScreen(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF212121))) {
        Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cool graphic placeholder (replace with your own image asset)
            Box(
                    modifier =
                            Modifier.size(180.dp)
                                    .background(
                                            Color(0xFF3F51B5),
                                            shape = MaterialTheme.shapes.medium
                                    ),
                    contentAlignment = Alignment.Center
            ) { Text("🃏", fontSize = 96.sp) }
            Spacer(modifier = Modifier.height(24.dp))
            Text("The Interview Table", style = MaterialTheme.typography.h3, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                    "Poker meets job interviews!",
                    style = MaterialTheme.typography.subtitle1,
                    color = Color(0xFFB0BEC5)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth(0.5f)) {
                Text("Start Game", fontSize = 20.sp)
            }
        }
    }
}
