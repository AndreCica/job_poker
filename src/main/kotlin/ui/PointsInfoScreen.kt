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
fun PointsInfoScreen(onClose: () -> Unit) {
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
                    Text("Skill Levels (Points)", style = MaterialTheme.typography.h4, color = Color.White)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Each card represents a candidate's skill and is assigned a rank from 1 to 5 based on their proficiency:",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SkillLevelRow("Level 1:", "Novice / Basic familiarity")
                SkillLevelRow("Level 2:", "Beginner / Some practical experience")
                SkillLevelRow("Level 3:", "Intermediate / Solid working knowledge")
                SkillLevelRow("Level 4:", "Advanced / Strong expertise")
                SkillLevelRow("Level 5:", "Expert / Industry-leading mastery")
                
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Higher level cards give you a stronger hand during the interview phases!",
                    color = Color.Yellow,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun SkillLevelRow(level: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Text(
            text = level,
            color = Color(0xFFFFD700),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = description,
            color = Color.White,
            style = MaterialTheme.typography.body1
        )
    }
}
