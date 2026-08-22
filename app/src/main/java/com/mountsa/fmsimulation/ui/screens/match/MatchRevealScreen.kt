package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.LeagueLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun MatchRevealScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Pulse animation untuk VS
    val pulse by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    MatchStageBackground {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        session.match.leagueId?.takeIf { it > 0L }?.let { LeagueLogo(it, 28.dp) }
                        Text(
                            text = session.competitionName.uppercase(),
                            color = FM_GREEN,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        text = session.stadiumName,
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = matchDayInfo(session.match.matchDate, session.stadiumName),
                        color = Color.White.copy(alpha = .62f),
                        fontSize = 9.sp
                    )
                }
            }

            // MIDDLE SECTION (HOME - VS - AWAY)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // HOME
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(800)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ClubLogo(clubId = session.match.homeClubId, size = 80.dp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            session.homeShortName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Text("HOME", color = Color(0xFF42A5F5), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                }

                // VS
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .scale(pulse)
                ) {
                    Surface(
                        color = FM_GREEN.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "VS",
                                color = FM_GREEN,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // AWAY
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(800)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ClubLogo(clubId = session.match.awayClubId, size = 80.dp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            session.awayShortName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Text("AWAY", color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                }
            }

            // BUTTON
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it })
            ) {
                Button(
                    onClick = { viewModel.nextMatchFlowStep() },
                    colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                    modifier = Modifier
                        .width(180.dp)
                        .height(40.dp)
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
                        "PREPARE LINEUP",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
    }
}
