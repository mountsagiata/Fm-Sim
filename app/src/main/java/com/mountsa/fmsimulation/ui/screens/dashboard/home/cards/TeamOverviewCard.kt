package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN

@Composable
fun TeamOverviewCard(
    modifier: Modifier = Modifier,
    club: ClubEntity?
) {
    AppColumn(
        modifier = modifier,
        title = "TEAM OVERVIEW"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    CompactRatingRow(label = "ATT", value = club?.attack ?: 0)
                    CompactRatingRow(label = "MID", value = club?.midfield ?: 0)
                    CompactRatingRow(label = "DEF", value = club?.defense ?: 0)
                }

                Spacer(Modifier.width(12.dp))

                // Overall Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("OVR"), fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = (club?.overall ?: 0).toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FM_GREEN
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${com.mountsa.fmsimulation.ui.localization.localized("ACADEMY")}  ${club?.academyQuality ?: 0}/100", fontSize = 8.sp, color = Color.LightGray)
                Text(
                    text = club?.managerPersonality?.name?.replace("_", " ") ?: "BALANCED",
                    fontSize = 8.sp,
                    color = FM_GREEN,
                    fontWeight = FontWeight.Bold
                )
            }

            // Item 34: Satisfaction Levels (Dynamic World)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("BOARD SATISFACTION"), fontSize = 7.sp, color = Color.Gray, maxLines = 1)
                    LinearProgressIndicator(
                        progress = { (club?.boardSatisfaction ?: 100) / 100f },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = if ((club?.boardSatisfaction ?: 100) > 50) FM_GREEN else Color.Red,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("FAN SATISFACTION"), fontSize = 7.sp, color = Color.Gray, maxLines = 1)
                    LinearProgressIndicator(
                        progress = { (club?.fanSatisfaction ?: 100) / 100f },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = if ((club?.fanSatisfaction ?: 100) > 50) FM_GREEN else Color.Red,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactRatingRow(label: String, value: Int) {
    val progress = (value.coerceIn(0, 99) / 99f)
    Row(
        modifier = Modifier.fillMaxWidth().height(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(28.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(6.dp),
            color = Color(0xFFB7D83D),
            trackColor = Color.White.copy(alpha = .1f)
        )
        Text(value.toString(), color = FM_GREEN, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp).padding(start = 7.dp))
    }
}
