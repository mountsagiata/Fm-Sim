package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.DashboardUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun StartingLineupScreen(viewModel: DashboardViewModel) {
    val uiState: DashboardUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return
    val userIsHome = uiState.club?.id == session.match.homeClubId
    val userLineup = if (userIsHome) session.homeLineup else session.awayLineup
    val userBench = if (userIsHome) session.homeBench else session.awayBench

    var visible by remember { mutableStateOf(false) }
    var selectedStarterId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FM_DARK_BG)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER dengan animasi
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { -it / 2 })
            ) {
                Column {
                    Text(
                        text = "STARTING XI",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Finalize tactics & prepare for battle",
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = FM_GREEN.copy(alpha = 0.3f), thickness = 2.dp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Player List dengan animasi per item
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp)
            ) {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(userLineup, key = { it.id }) { player ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInHorizontally(
                                initialOffsetX = { -it / 2 },
                                animationSpec = tween(400, delayMillis = player.startingIndex * 50)
                            ) + fadeIn(
                                animationSpec = tween(400, delayMillis = player.startingIndex * 50)
                            )
                        ) {
                            PlayerRow(
                                player.name,
                                player.position,
                                userLineup.indexOfFirst { it.id == player.id },
                                selected = selectedStarterId == player.id,
                                onClick = { selectedStarterId = if (selectedStarterId == player.id) null else player.id }
                            )
                        }
                    }
                    item {
                        Text("SUBSTITUTES", color = FM_GREEN, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                    }
                    items(userBench, key = { "bench-${it.id}" }) { player ->
                        PlayerRow(player.name, player.position, player.shirtNumber, onClick = {
                            selectedStarterId?.let { starterId ->
                                viewModel.swapMatchPlayer(starterId, player.id)
                                selectedStarterId = null
                            }
                        })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // KICK OFF BUTTON dengan pulse animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it })
            ) {
                Button(
                    onClick = { viewModel.nextMatchFlowStep() },
                    colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .scale(
                            animateFloatAsState(
                                targetValue = if (visible) 1f else 0.95f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            ).value
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "KICK OFF",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerRow(name: String, position: String, index: Int, selected: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) FM_GREEN.copy(alpha = 0.18f) else if (index % 2 == 0) Color.White.copy(alpha = 0.03f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${index + 1}.",
                color = FM_GREEN,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            Text(
                name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Surface(
            color = FM_GREEN.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                position,
                color = FM_GREEN,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}
