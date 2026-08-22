package com.mountsa.fmsimulation.ui.screens.dashboard.league

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mountsa.fmsimulation.data.local.entities.StandingEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.LeagueLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.PlayerAvatar
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun LeagueHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val standings by viewModel.selectedLeagueStandings.collectAsStateWithLifecycle()
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    val selectedLeagueId by viewModel.selectedLeagueId.collectAsStateWithLifecycle()
    val selectedLeague = leagues.firstOrNull { it.id == selectedLeagueId }
    val userClubId = uiState.club?.id ?: -1L
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()
    val leagueClubIds = remember(uiState.allClubs, selectedLeagueId) {
        uiState.allClubs.filter { it.leagueId == selectedLeagueId }.map { it.id }.toSet()
    }
    val leaguePlayers = remember(allPlayers, leagueClubIds) { allPlayers.filter { it.clubId in leagueClubIds } }
    var selectedTab by remember { mutableIntStateOf(0) }

    AppColumn(
        modifier = Modifier.fillMaxSize(),
        title = ""
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(42.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentIndex = leagues.indexOfFirst { it.id == selectedLeagueId }.coerceAtLeast(0)
                IconButton(onClick = { leagues.getOrNull((currentIndex - 1).coerceAtLeast(0))?.let { viewModel.selectLeague(it.id) } }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous league", tint = FM_GREEN)
                }
                selectedLeague?.let { LeagueLogo(it.id, 26.dp) }
                Text(
                    selectedLeague?.name?.uppercase() ?: "LEAGUE TABLE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 160.dp).padding(horizontal = 8.dp)
                )
                IconButton(onClick = { leagues.getOrNull((currentIndex + 1).coerceAtMost(leagues.lastIndex))?.let { viewModel.selectLeague(it.id) } }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next league", tint = FM_GREEN)
                }
            }
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                Tab(selectedTab == 0, { selectedTab = 0 }, text = { Text("TABLE", fontSize = 10.sp) })
                Tab(selectedTab == 1, { selectedTab = 1 }, text = { Text("TOP PLAYERS & AWARDS", fontSize = 10.sp) })
            }

            if (selectedTab == 0) {
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
            } else {
                PlayerLeaderboards(leaguePlayers, uiState.allClubs)
            }
        }
    }
}

@Composable
private fun PlayerLeaderboards(players: List<PlayerEntity>, clubs: List<ClubEntity>) {
    val scorers = players.sortedWith(compareByDescending<PlayerEntity> { it.goals }.thenByDescending { it.averageRating }).take(10)
    val assisters = players.sortedWith(compareByDescending<PlayerEntity> { it.assists }.thenByDescending { it.averageRating }).take(10)
    val rated = players.sortedWith(compareByDescending<PlayerEntity> { it.averageRating }.thenByDescending { it.appearances }).take(10)
    val awards = rated.take(5)

    BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp)) {
        val compact = maxWidth < 720.dp
        if (compact) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlayerLeaderColumn("TOP GOALS", scorers, clubs, { it.goals.toString() }, Modifier.weight(1f))
                    PlayerLeaderColumn("TOP ASSISTS", assisters, clubs, { it.assists.toString() }, Modifier.weight(1f))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlayerLeaderColumn("BEST RATING", rated, clubs, { String.format("%.2f", it.averageRating) }, Modifier.weight(1f))
                    PlayerLeaderColumn("PLAYER AWARDS", awards, clubs, { "★" }, Modifier.weight(1f))
                }
            }
        } else {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerLeaderColumn("TOP GOALS", scorers, clubs, { it.goals.toString() }, Modifier.weight(1f))
                PlayerLeaderColumn("TOP ASSISTS", assisters, clubs, { it.assists.toString() }, Modifier.weight(1f))
                PlayerLeaderColumn("BEST RATING", rated, clubs, { String.format("%.2f", it.averageRating) }, Modifier.weight(1f))
                PlayerLeaderColumn("PLAYER AWARDS", awards, clubs, { "★" }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PlayerLeaderColumn(
    title: String,
    players: List<PlayerEntity>,
    clubs: List<ClubEntity>,
    value: (PlayerEntity) -> String,
    modifier: Modifier = Modifier
) {
    Surface(modifier, color = Color.White.copy(.025f), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxSize()) {
            Text(title, color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(10.dp))
            LazyColumn(Modifier.fillMaxSize()) {
                items(players, key = { it.id }) { player ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${players.indexOf(player) + 1}", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.width(20.dp))
                        PlayerAvatar(player, 26.dp)
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(player.shortName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                ClubLogo(player.clubId, 12.dp)
                                if (player.flagAsset.isNotBlank()) {
                                    AsyncImage(
                                        model = "file:///android_asset/${player.flagAsset}",
                                        contentDescription = "Country",
                                        modifier = Modifier.size(width = 14.dp, height = 9.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text(
                                    "${clubs.firstOrNull { it.id == player.clubId }?.shortName ?: "-"} • ${player.position}",
                                    color = Color.Gray,
                                    fontSize = 7.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        Text(value(player), color = FM_GREEN, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    HorizontalDivider(color = Color.White.copy(.045f))
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
