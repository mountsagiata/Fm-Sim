package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.data.local.entities.ObjectiveEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN

@Composable
fun ObjectiveCard(
    modifier: Modifier = Modifier,
    objectives: List<ObjectiveEntity> = emptyList()
) {
    AppColumn(
        modifier = modifier,
        title = "OBJECTIVES"
    ) {
        if (objectives.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
                Text("No active objectives", color = Color.Gray, fontSize = 10.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show only the first 2 for brevity in the card
                objectives.take(2).forEach { objective ->
                    ObjectiveItem(objective)
                }
            }
        }
    }
}

@Composable
private fun ObjectiveItem(objective: ObjectiveEntity) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = objective.title,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = objective.priority.name,
                fontSize = 8.sp,
                color = when (objective.priority.name) {
                    "CRITICAL" -> Color.Red
                    "HIGH" -> Color(0xFFFFA500)
                    else -> Color.Gray
                },
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        val progress = if (objective.targetValue > 0) {
            objective.currentProgress.toFloat() / objective.targetValue.toFloat()
        } else 0f
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = FM_GREEN,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}
