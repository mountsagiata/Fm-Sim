package com.mountsa.fmsimulation.ui.screens.dashboard.home.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.home.TopPlayerStat
import java.util.Locale

@Composable
fun LeagueStatsSection(
    modifier: Modifier = Modifier,
    topScorer: PlayerEntity?,
    topAssister: PlayerEntity?,
    bestPlayer: PlayerEntity?
) {

    AppColumn(
        modifier = modifier,
        title = "LEAGUE STATS",
        headerAction = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "View All",
                    fontSize = 10.sp,
                    color = FM_GREEN
                )
                Icon(
                    Icons.AutoMirrored.Filled.NavigateNext,
                    null,
                    tint = FM_GREEN,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopPlayerStat(
                category = "TOP SCORER",
                name = topScorer?.shortName ?: "-",
                value = "${topScorer?.goals ?: 0}",
                avatarAsset = topScorer?.avatarAsset
            )

            TopPlayerStat(
                category = "TOP ASSIST",
                name = topAssister?.shortName ?: "-",
                value = "${topAssister?.assists ?: 0}",
                avatarAsset = topAssister?.avatarAsset
            )

            TopPlayerStat(
                category = "TOP RATE",
                name = bestPlayer?.shortName ?: "-",
                value = String.format(
                    Locale.getDefault(),
                    "%.2f",
                    bestPlayer?.averageRating ?: 0f
                ),
                avatarAsset = bestPlayer?.avatarAsset
            )
        }
    }
}
