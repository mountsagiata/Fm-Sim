package com.mountsa.fmsimulation.ui.screens.match

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mountsa.fmsimulation.core.match.event.MatchEvent
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.utils.AudioManager
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
    val listState = rememberLazyListState()

    // Crowd ambience: starts when the live match screen appears, stops when it's left.
    val audioManager = viewModel.audioManager
    DisposableEffect(Unit) {
        audioManager.playCrowdAmbience()
        onDispose { audioManager.stopCrowdAmbience() }
    }

    LaunchedEffect(Unit) {
        while (currentMinute < 90) {
            delay(150) // Adjust speed as needed
            currentMinute += 1
        }
    }

    val visibleEvents = events.filter { it.minute <= currentMinute }
    val latestCommentary = visibleEvents.lastOrNull { it.commentary.isNotEmpty() }?.commentary 
        ?: "Pertandingan sedang berlangsung..."

    val currentHomeScore = visibleEvents.lastOrNull()?.scoreHome ?: 0
    val currentAwayScore = visibleEvents.lastOrNull()?.scoreAway ?: 0

    // Auto-scroll to bottom of events
    LaunchedEffect(visibleEvents.size) {
        if (visibleEvents.isNotEmpty()) {
            listState.animateScrollToItem(visibleEvents.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(FM_DARK_BG).padding(12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(session.homeShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(session.competitionName.uppercase(), color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(session.awayShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // SCORE & TIME
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(currentHomeScore.toString(), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(if (currentMinute >= 90) "FT" else "$currentMinute'", color = FM_GREEN, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("MENIT", color = Color.Gray, fontSize = 8.sp)
                }
                Text(currentAwayScore.toString(), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
            }

            // PROGRESS BAR
            LinearProgressIndicator(
                progress = { currentMinute / 90f },
                modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally).height(3.dp).clip(RoundedCornerShape(2.dp)),
                color = FM_GREEN,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(Modifier.height(16.dp))

            // 3 COLUMNS SECTION
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // LEFT: STATISTIK (Mocked progress based on minute)
                SimCol(title = "STATISTIK", modifier = Modifier.weight(1f)) {
                    val p = currentMinute / 90f
                    StatRowSim("Shots", (match.shotsHome * p).toInt(), (match.shotsAway * p).toInt())
                    StatRowSim("Target", (match.shotsOnTargetHome * p).toInt(), (match.shotsOnTargetAway * p).toInt())
                    StatRowSim("Poss %", match.possessionHome, match.possessionAway)
                }

                // CENTER: EVENT PERTANDINGAN
                SimCol(title = "EVENT", modifier = Modifier.weight(1.1f)) {
                    LazyColumn(state = listState) {
                        items(visibleEvents) { event ->
                            EventText(event.minute, event.type.name.replace("_", " "))
                        }
                    }
                }

                // RIGHT: LIVE COMMENTARY
                SimCol(title = "KOMENTAR", modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = latestCommentary,
                        color = Color.White, fontSize = 11.sp, lineHeight = 15.sp
                    )
                }
            }

            // FOOTER BUTTON
            if (currentMinute >= 90) {
                Button(
                    onClick = { viewModel.nextMatchFlowStep() },
                    colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(40.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("VIEW RESULT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SimCol(title: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxHeight().background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp)).padding(8.dp)) {
        Text(title, color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun StatRowSim(label: String, h: Int, a: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(h.toString(), color = Color.White, fontSize = 10.sp)
            Text(label, color = Color.Gray, fontSize = 9.sp)
            Text(a.toString(), color = Color.White, fontSize = 10.sp)
        }
        val progress = if (h + a > 0) h.toFloat() / (h + a) else 0.5f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color = FM_GREEN,
            trackColor = Color.Red.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun EventText(min: Int, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text("$min'", color = FM_GREEN, fontSize = 10.sp, modifier = Modifier.width(22.dp), fontWeight = FontWeight.Bold)
        Text(text, color = Color.White, fontSize = 10.sp)
    }
}
