package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.alpha
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
                    tint = FM_GREEN,
                    modifier = Modifier.size(32.dp).alpha(.5f)
                )
                Text("0", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(com.mountsa.fmsimulation.ui.localization.localized("No active objectives"), color = Color.Gray, fontSize = 9.sp)
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(objectives, key = { it.id }) { objective ->
                    Column(
                        Modifier.width(150.dp).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = objective.title,
                            tint = FM_GREEN,
                            modifier = Modifier.size(27.dp).alpha(if (objective.completed) 1f else .5f)
                        )
                        Text(
                            if (objective.completed) "1" else "0",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        ObjectiveItem(objective)
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectiveItem(objective: ObjectiveEntity) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
