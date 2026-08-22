package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
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
            val active = objectives.filterNot { it.completed }
            val priority = active.maxByOrNull { it.priority.ordinal } ?: objectives.first()
            Column(
                Modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = FM_GREEN,
                    modifier = Modifier.size(30.dp).alpha(.75f)
                )
                Text("${active.size} ACTIVE", color = FM_GREEN, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(
                    priority.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("OPEN OBJECTIVES", color = Color.Gray, fontSize = 8.sp)
            }
        }
    }
}
