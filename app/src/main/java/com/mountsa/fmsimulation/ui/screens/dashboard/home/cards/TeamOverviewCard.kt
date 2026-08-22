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
import com.mountsa.fmsimulation.ui.screens.dashboard.home.StatRowGreen

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
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatRowGreen(label = "ATT", value = club?.attack ?: 0)
                    StatRowGreen(label = "MID", value = club?.midfield ?: 0)
                    StatRowGreen(label = "DEF", value = club?.defense ?: 0)
                }

                Spacer(Modifier.width(12.dp))

                // Overall Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("OVR"), fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = (club?.overall ?: 0).toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FM_GREEN
                    )
                }
            }

            // Item 29 & 33: Youth & AI Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("ACADEMY"), fontSize = 9.sp, color = Color.Gray)
                    Text(
                        text = "${club?.academyQuality ?: 0}/100",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("AI PERSONALITY"), fontSize = 9.sp, color = Color.Gray)
                    Text(
                        text = club?.managerPersonality?.name?.replace("_", " ") ?: "BALANCED",
                        fontSize = 11.sp,
                        color = FM_GREEN,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Item 34: Satisfaction Levels (Dynamic World)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("BOARD SATISFACTION"), fontSize = 8.sp, color = Color.Gray)
                    LinearProgressIndicator(
                        progress = { (club?.boardSatisfaction ?: 100) / 100f },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = if ((club?.boardSatisfaction ?: 100) > 50) FM_GREEN else Color.Red,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("FAN SATISFACTION"), fontSize = 8.sp, color = Color.Gray)
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
