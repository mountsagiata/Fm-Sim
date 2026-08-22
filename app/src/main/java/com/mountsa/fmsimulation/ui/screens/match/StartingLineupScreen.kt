package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.LeagueLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay

private val HOME_BLUE = Color(0xFF42A5F5)

@Composable
fun StartingLineupScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val session = state.matchSession ?: return
    val userIsHome = state.club?.id == session.match.homeClubId
    var visible by remember { mutableStateOf(false) }
    var selectedStarterId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) { delay(160); visible = true }

    MatchStageBackground {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            TeamHeading(session.match.homeClubId, session.homeShortName, "HOME", HOME_BLUE, Modifier.weight(1f), Alignment.Start)
            Column(Modifier.widthIn(min = 100.dp, max = 150.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                session.match.leagueId?.takeIf { it > 0L }?.let { leagueId ->
                    LeagueLogo(leagueId = leagueId, size = 26.dp)
                } ?: Text(
                    session.competitionName.uppercase(),
                    color = FM_GREEN,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    matchDayInfo(session.match.matchDate, session.stadiumName),
                    color = Color.Gray,
                    fontSize = 7.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TeamHeading(session.match.awayClubId, session.awayShortName, "AWAY", FM_GREEN, Modifier.weight(1f), Alignment.End)
        }
        HorizontalDivider(color = Color.White.copy(.1f))
        Row(Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            LineupColumn(session.homeLineup, HOME_BLUE, true, visible, if (userIsHome) selectedStarterId else null, userIsHome, { selectedStarterId = if (selectedStarterId == it) null else it }, Modifier.weight(1f))
            Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(.08f)))
            LineupColumn(session.awayLineup, FM_GREEN, false, visible, if (!userIsHome) selectedStarterId else null, !userIsHome, { selectedStarterId = if (selectedStarterId == it) null else it }, Modifier.weight(1f))
        }
        Button(
            onClick = viewModel::nextMatchFlowStep,
            modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(min = 150.dp, max = 190.dp).height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN), shape = RoundedCornerShape(7.dp)
        ) { Text("KICK OFF", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
    }
    }
}

@Composable
private fun TeamHeading(clubId: Long, name: String, label: String, color: Color, modifier: Modifier, alignment: Alignment.Horizontal) {
    Column(modifier, horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (alignment == Alignment.End) Text(name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, modifier = Modifier.padding(end = 8.dp))
            ClubLogo(clubId, 34.dp)
            if (alignment == Alignment.Start) Text(name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LineupColumn(players: List<PlayerEntity>, color: Color, fromLeft: Boolean, visible: Boolean, selectedId: Long?, enabled: Boolean, onSelect: (Long) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        itemsIndexed(players, key = { _, p -> p.id }) { index, player ->
            AnimatedVisibility(visible = visible, enter = slideInHorizontally(initialOffsetX = { if (fromLeft) -it else it }) + fadeIn()) {
                Row(
                    Modifier.fillMaxWidth().height(38.dp)
                        .background(if (selectedId == player.id) color.copy(.16f) else Color.White.copy(if (index % 2 == 0) .025f else 0f), RoundedCornerShape(5.dp))
                        .clickable(enabled = enabled) { onSelect(player.id) }.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        (player.shirtNumber.takeIf { it > 0 } ?: (index + 1)).toString(),
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )
                    LineupAvatar(player)
                    Spacer(Modifier.width(8.dp))
                    Text(player.shortName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(player.position, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun LineupAvatar(player: PlayerEntity) {
    Box(
        modifier = Modifier.size(27.dp).clip(CircleShape).background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        if (player.avatarAsset.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/${player.avatarAsset}")
                    .crossfade(true)
                    .build(),
                contentDescription = player.shortName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(player.shirtNumber.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
