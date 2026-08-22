package com.mountsa.fmsimulation.ui.screens.dashboard.league

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.data.local.entities.StandingEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun LeagueHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val standings = uiState.leagueStandings
    val userClubId = uiState.club?.id ?: -1L

    AppColumn(
        modifier = Modifier.fillMaxSize(),
        title = uiState.club?.let { "LEAGUE TABLE" } ?: "LEAGUE"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StandingHeader()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(standings) { index, entry ->
                    StandingRow(
                        position = index + 1,
                        entry = entry,
                        isUserClub = entry.clubId == userClubId,
                        allClubs = uiState.allClubs
                    )
                }
            }
        }
    }
}

@Composable
fun StandingHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(com.mountsa.fmsimulation.ui.localization.localized("#"), modifier = Modifier.width(24.dp), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(com.mountsa.fmsimulation.ui.localization.localized("CLUB"), modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.width(160.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            HeaderText(com.mountsa.fmsimulation.ui.localization.localized("P"))
            HeaderText(com.mountsa.fmsimulation.ui.localization.localized("W"))
            HeaderText(com.mountsa.fmsimulation.ui.localization.localized("D"))
            HeaderText(com.mountsa.fmsimulation.ui.localization.localized("L"))
            HeaderText(com.mountsa.fmsimulation.ui.localization.localized("GD"))
            HeaderText(com.mountsa.fmsimulation.ui.localization.localized("PTS"))
        }
    }
}

@Composable
private fun HeaderText(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(26.dp),
        fontSize = 10.sp,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun StandingRow(
    position: Int,
    entry: StandingEntity,
    isUserClub: Boolean,
    allClubs: List<com.mountsa.fmsimulation.data.local.entities.ClubEntity>
) {
    val club = allClubs.find { it.id == entry.clubId }
    val bgColor = if (isUserClub) FM_GREEN.copy(alpha = 0.15f) else Color.Transparent

    Surface(
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position
            Text(
                text = position.toString(),
                modifier = Modifier.width(24.dp),
                fontSize = 12.sp,
                fontWeight = if (isUserClub) FontWeight.Bold else FontWeight.Normal,
                color = if (position <= 4) FM_GREEN else if (position >= 18) Color.Red else Color.White
            )

            // Club Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClubLogo(clubId = entry.clubId, size = 20.dp)
                Text(
                    text = club?.name ?: "Unknown",
                    fontSize = 12.sp,
                    fontWeight = if (isUserClub) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Stats
            Row(modifier = Modifier.width(160.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatText(entry.played.toString())
                StatText(entry.wins.toString())
                StatText(entry.draws.toString())
                StatText(entry.losses.toString())
                StatText(entry.goalDifference.toString(), color = if (entry.goalDifference >= 0) Color.White else Color.Red)
                Text(
                    text = entry.points.toString(),
                    modifier = Modifier.width(26.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUserClub) FM_GREEN else Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}

@Composable
private fun StatText(text: String, color: Color = Color.White) {
    Text(
        text = text,
        modifier = Modifier.width(26.dp),
        fontSize = 12.sp,
        color = color,
        textAlign = TextAlign.Center
    )
}
