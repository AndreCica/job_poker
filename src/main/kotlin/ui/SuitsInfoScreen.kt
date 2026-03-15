package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SuitsInfoScreen(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight(0.6f),
            elevation = 8.dp,
            backgroundColor = Color(0xFF1C2833)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thematic Departments (Suits)", style = MaterialTheme.typography.h4, color = Color.White)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Skills are categorized into four thematic suits based on the job requirements:",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SuitRow("Clubs ♣", "Engineering & Technical Skills", Color(0xFF81C784))
                SuitRow("Diamonds ♦", "Design & Creative Skills", Color(0xFF64B5F6))
                SuitRow("Hearts ♥", "Management & Leadership", Color(0xFFE57373))
                SuitRow("Spades ♠", "Operations & Strategy", Color(0xFFFFD54F))
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SuitRow(suit: String, description: String, suitColor: Color) {
    Row(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Text(
            text = suit,
            color = suitColor,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = description,
            color = Color.White,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
