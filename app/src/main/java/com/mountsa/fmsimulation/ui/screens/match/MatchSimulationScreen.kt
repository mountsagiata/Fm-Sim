package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mountsa.fmsimulation.core.match.event.EventType
import com.mountsa.fmsimulation.core.match.event.MatchEvent
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun MatchSimulationScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return
    val match = session.match

    val events = remember(match.matchEvents) {
        try {
            Gson().fromJson<List<MatchEvent>>(
                match.matchEvents,
                object : TypeToken<List<MatchEvent>>() {}.type
            ) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    var currentMinute by remember { mutableIntStateOf(0) }
    var halfTimePaused by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var pulseScale by remember { mutableStateOf(1f) }

    val scale by animateFloatAsState(
        targetValue = pulseScale,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    val audioManager = viewModel.audioManager
    DisposableEffect(Unit) {
        audioManager.playCrowdAmbience()
        onDispose { audioManager.stopCrowdAmbience() }
    }

    LaunchedEffect(Unit) {
        while (currentMinute < 90) {
            if (currentMinute == 45) {
                halfTimePaused = true
                while (halfTimePaused) delay(100)
            }
            delay(150)
            currentMinute += 1
            pulseScale = 1f + (0.02f * (currentMinute % 10) / 10f)
        }
    }

    val visibleEvents = events.filter { it.minute <= currentMinute }
    val latestEvent = visibleEvents.lastOrNull()
    val latestCommentary = latestEvent?.commentary?.takeIf { it.isNotBlank() }
        ?: latestEvent?.let(::fallbackCommentary)
        ?: "The teams are ready. Live commentary will appear here."

    val currentHomeScore = visibleEvents.lastOrNull()?.scoreHome ?: 0
    val currentAwayScore = visibleEvents.lastOrNull()?.scoreAway ?: 0

    LaunchedEffect(visibleEvents.size) {
        if (visibleEvents.isNotEmpty()) {
            listState.animateScrollToItem(visibleEvents.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(FM_DARK_BG).padding(12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClubLogo(clubId = match.homeClubId, size = 22.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(session.homeShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(
                    color = FM_GREEN.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(session.competitionName.uppercase(), color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(session.awayShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    ClubLogo(clubId = match.awayClubId, size = 22.dp)
                }
            }

            // SCORE & TIME dengan animasi
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                AnimatedContent(
                    targetState = currentHomeScore,
                    transitionSpec = {
                        fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -it / 2 }) togetherWith
                        fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
                    }
                ) { score ->
                    Text(score.toString(), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(if (currentMinute >= 90) "FT" else "$currentMinute'", color = FM_GREEN, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(if (currentMinute >= 90) "FULL TIME" else "MENIT", color = Color.Gray, fontSize = 7.sp, letterSpacing = 1.sp)
                }
                AnimatedContent(
                    targetState = currentAwayScore,
                    transitionSpec = {
                        fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -it / 2 }) togetherWith
                        fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
                    }
                ) { score ->
                    Text(score.toString(), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                }
            }

            // PROGRESS BAR
            Box(modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.CenterHorizontally).height(4.dp).clip(RoundedCornerShape(2.dp))) {
                LinearProgressIndicator(
                    progress = { currentMinute / 90f },
                    modifier = Modifier.fillMaxSize(),
                    color = FM_GREEN,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(visible = halfTimePaused) {
                Surface(
                    color = Color(0xFF17231B),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(com.mountsa.fmsimulation.ui.localization.localized("HALF-TIME"), color = FM_GREEN, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text(com.mountsa.fmsimulation.ui.localization.localized("Review events, statistics and make tactical changes."), color = Color.Gray, fontSize = 10.sp)
                        }
                        Button(onClick = { halfTimePaused = false }, colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN)) {
                            Text(com.mountsa.fmsimulation.ui.localization.localized("START SECOND HALF"), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3 COLUMNS SECTION
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // LEFT: STATISTIK
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { -it / 4 })
                ) {
                    SimCol(title = "STATISTIK", modifier = Modifier.weight(1f)) {
                        val p = currentMinute / 90f
                        StatRowModern("Shots", (match.shotsHome * p).toInt(), (match.shotsAway * p).toInt())
                        StatRowModern("Target", (match.shotsOnTargetHome * p).toInt(), (match.shotsOnTargetAway * p).toInt())
                        StatRowModern("Poss %", match.possessionHome, match.possessionAway)
                        StatRowModern("Corners", (match.cornersHome * p).toInt(), (match.cornersAway * p).toInt())
                    }
                }

                // CENTER: EVENT
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.95f)
                ) {
                    SimCol(title = "EVENT", modifier = Modifier.weight(1.1f)) {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            items(visibleEvents) { event ->
                                EventText(
                                    min = event.minute,
                                    text = event.type.toString().replace("_", " "),
                                    type = event.type,
                                    playerName = event.playerName
                                )
                            }
                        }
                    }
                }

                // RIGHT: LIVE COMMENTARY
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { it / 4 })
                ) {
                    SimCol(title = "KOMENTAR", modifier = Modifier.weight(1.2f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = latestCommentary,
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            // Live indicator
                            if (currentMinute < 90) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(6.dp)
                                        .background(
                                            color = FM_GREEN,
                                            shape = RoundedCornerShape(50)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // FOOTER BUTTON
            if (currentMinute >= 90) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it })
                ) {
                    Button(
                        onClick = { viewModel.nextMatchFlowStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(com.mountsa.fmsimulation.ui.localization.localized("VIEW RESULT"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SimCol(title: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp)).padding(8.dp)) {
        Text(title, color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun StatRowModern(label: String, h: Int, a: Int) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(h.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray.copy(alpha = 0.6f), fontSize = 8.sp)
            Text(a.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        val progress = if (h + a > 0) h.toFloat() / (h + a) else 0.5f
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.05f))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(FM_GREEN)
            )
        }
    }
}

@Composable
fun EventText(min: Int, text: String, type: EventType, playerName: String = "") {
    val color = when (type) {
        EventType.GOAL, EventType.PENALTY_GOAL, EventType.INJURY_TIME_GOAL -> FM_GREEN
        EventType.RED_CARD -> Color.Red
        EventType.YELLOW_CARD -> Color.Yellow
        EventType.GOAL_OWN -> Color(0xFFFF9800)
        EventType.SUBSTITUTION -> Color(0xFF2196F3)
        EventType.HALFTIME, EventType.MATCH_END -> Color(0xFF9C27B0)
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (type == EventType.GOAL || type == EventType.PENALTY_GOAL)
                    FM_GREEN.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(com.mountsa.fmsimulation.ui.localization.localized("$min'"), color = FM_GREEN, fontSize = 10.sp, modifier = Modifier.width(22.dp), fontWeight = FontWeight.Bold)
            Text(text, color = color, fontSize = 10.sp, fontWeight = if (type == EventType.GOAL || type == EventType.PENALTY_GOAL) FontWeight.Bold else FontWeight.Normal)
        }
        if (playerName.isNotEmpty()) {
            Text(playerName, color = Color.Gray.copy(alpha = 0.6f), fontSize = 9.sp)
        }
    }
}

private fun fallbackCommentary(event: MatchEvent): String = when (event.type) {
    EventType.MATCH_START, EventType.KICKOFF_FIRST -> "Kick-off. Both teams settle into their shape."
    EventType.HALFTIME -> "The referee signals half-time. Time for the managers to adjust."
    EventType.SECOND_HALF -> "The second half is underway."
    EventType.GOAL, EventType.PENALTY_GOAL -> "GOAL! ${event.playerName.ifBlank { event.teamName }} finds the net."
    EventType.SHOT, EventType.SHOT_ON_TARGET -> "${event.playerName.ifBlank { event.teamName }} gets a shot away."
    EventType.SAVE, EventType.BIG_SAVE, EventType.GOAL_KEEPER_SAVE -> "A strong save keeps the score unchanged."
    EventType.CORNER -> "Corner to ${event.teamName.ifBlank { "the attacking side" }}."
    EventType.FOUL -> "The referee stops play for a foul."
    EventType.YELLOW_CARD -> "Yellow card for ${event.playerName.ifBlank { "the offender" }}."
    EventType.RED_CARD -> "Red card! ${event.playerName.ifBlank { "A player" }} is sent off."
    EventType.SUBSTITUTION -> "A substitution changes the shape of ${event.teamName.ifBlank { "the team" }}."
    EventType.INJURY -> "Play is stopped while ${event.playerName.ifBlank { "the player" }} receives treatment."
    EventType.MATCH_END -> "Full-time. The referee brings the match to an end."
    else -> "${event.minute}' ${event.type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}."
}
