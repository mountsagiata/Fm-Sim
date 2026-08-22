package com.mountsa.fmsimulation.ui.screens.dashboard.fixtures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.LeagueLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompetitionFixturesHub(viewModel: DashboardViewModel) {
    val matches by viewModel.worldMatches.collectAsStateWithLifecycle()
    val clubs by viewModel.allClubs.collectAsStateWithLifecycle()
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    var selectedLeagueId by remember { mutableStateOf<Long?>(null) }
    val filtered = remember(matches, selectedLeagueId) {
        matches.filter { selectedLeagueId == null || it.leagueId == selectedLeagueId }.sortedBy { it.matchDate }
    }

    AppColumn(Modifier.fillMaxSize(), title = "ALL FIXTURES & COMPETITIONS") {
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                item {
                    FilterChip(selectedLeagueId == null, { selectedLeagueId = null }, { Text("ALL", fontSize = 9.sp) })
                }
                items(leagues, key = { it.id }) { league ->
                    FilterChip(
                        selectedLeagueId == league.id,
                        { selectedLeagueId = league.id },
                        { Text(league.name, fontSize = 9.sp, maxLines = 1) },
                        leadingIcon = { LeagueLogo(league.id, 18.dp) }
                    )
                }
            }
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(filtered, key = { it.id }) { match ->
                    val home = clubs.firstOrNull { it.id == match.homeClubId }
                    val away = clubs.firstOrNull { it.id == match.awayClubId }
                    FixtureWorldRow(match, home?.shortName ?: "HOME", away?.shortName ?: "AWAY")
                }
            }
        }
    }
}

@Composable
private fun FixtureWorldRow(match: MatchEntity, homeName: String, awayName: String) {
    Surface(Modifier.fillMaxWidth(), color = Color.White.copy(.035f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(SimpleDateFormat("dd MMM\nHH:mm", Locale.getDefault()).format(Date(match.matchDate)), color = Color.Gray, fontSize = 9.sp, modifier = Modifier.width(58.dp))
            if (match.leagueId != null) LeagueLogo(match.leagueId, 24.dp)
            else Icon(Icons.Default.EmojiEvents, null, tint = FM_GREEN, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(homeName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(7.dp)); ClubLogo(match.homeClubId, 24.dp)
                Text(if (match.isPlayed) "  ${match.homeScore} - ${match.awayScore}  " else "  VS  ", color = FM_GREEN, fontSize = 11.sp, fontWeight = FontWeight.Black)
                ClubLogo(match.awayClubId, 24.dp); Spacer(Modifier.width(7.dp))
                Text(awayName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(match.stage, color = Color.Gray, fontSize = 8.sp, modifier = Modifier.width(70.dp))
        }
    }
}
