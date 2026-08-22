package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.DashboardUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun MatchResultScreen(viewModel: DashboardViewModel) {
    val uiState: DashboardUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return
    val match = session.match
    val userClubId = uiState.club?.id
    val userIsHome = userClubId == match.homeClubId

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val isHomeWin = match.homeScore > match.awayScore
    val isAwayWin = match.awayScore > match.homeScore
    val isDraw = match.homeScore == match.awayScore

    val userWon = if (userIsHome) isHomeWin else isAwayWin
    val userLost = if (userIsHome) isAwayWin else isHomeWin
    val resultColor = when { userWon -> FM_GREEN; userLost -> Color(0xFFFF5252); else -> Color(0xFFFFD740) }
    val resultText = when { userWon -> "VICTORY"; userLost -> "DEFEAT"; else -> "DRAW" }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FM_DARK_BG)
            .padding(16.dp)
    ) {
        val compactHeight = maxHeight < 480.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (compactHeight) Arrangement.Top else Arrangement.Center
        ) {
            // HEADER
            AnimatedVisibility(
                visible = visible && !isDraw,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -it / 2 })
            ) {
                Surface(
                    color = resultColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        resultText,
                        color = resultColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(color = FM_GREEN.copy(.16f), shape = RoundedCornerShape(50), modifier = Modifier.size(28.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(session.competitionName.take(2).uppercase(), color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Black) }
                }
                Text(session.competitionName.uppercase(), color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            // TEXT "FULL TIME"
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -it / 2 })
            ) {
                Text(
                    "FULL TIME",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp
                )
            }

            Spacer(Modifier.height(if (compactHeight) 4.dp else 12.dp))

            // SCORE SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = isHomeWin,
                    transitionSpec = {
                        fadeIn(tween(400)) + scaleIn(initialScale = 0.5f) togetherWith
                        fadeOut(tween(200)) + scaleOut(targetScale = 0.5f)
                    }
                ) { homeWon ->
                    ResultTeamModern(
                        clubId = match.homeClubId,
                        name = session.homeClubName,
                        score = match.homeScore,
                        isWinner = homeWon,
                        compact = compactHeight,
                        modifier = Modifier.weight(1f)
                    )
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(tween(500)) + fadeIn(tween(500))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp)) {
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "VS",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = isAwayWin,
                    transitionSpec = {
                        fadeIn(tween(400)) + scaleIn(initialScale = 0.5f) togetherWith
                        fadeOut(tween(200)) + scaleOut(targetScale = 0.5f)
                    }
                ) { awayWon ->
                    ResultTeamModern(
                        clubId = match.awayClubId,
                        name = session.awayClubName,
                        score = match.awayScore,
                        isWinner = awayWon,
                        compact = compactHeight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(if (compactHeight) 8.dp else 20.dp))

            // STATS CARD
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it })
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .scale(
                            animateFloatAsState(
                                targetValue = if (visible) 1f else 0.9f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ).value
                        )
                ) {
                    Column(modifier = Modifier.padding(if (compactHeight) 8.dp else 14.dp)) {
                        Text(
                            "MATCH STATS",
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        StatRowSmall("Shots", "${match.shotsHome}", "${match.shotsAway}")
                        StatRowSmall("On Target", "${match.shotsOnTargetHome}", "${match.shotsOnTargetAway}")
                        StatRowSmall("Possession", "${match.possessionHome}%", "${match.possessionAway}%")
                        StatRowSmall("Corners", "${match.cornersHome}", "${match.cornersAway}")
                        StatRowSmall("Fouls", "${match.foulsHome}", "${match.foulsAway}")
                    }
                }
            }

            Spacer(Modifier.height(if (compactHeight) 8.dp else 24.dp))

            // CONTINUE BUTTON
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it })
            ) {
                Button(
                    onClick = { viewModel.nextMatchFlowStep() },
                    colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                    modifier = Modifier
                        .width(220.dp)
                        .height(if (compactHeight) 36.dp else 42.dp)
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
                    Text(com.mountsa.fmsimulation.ui.localization.localized("CONTINUE"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun ResultTeamModern(clubId: Long, name: String, score: Int, isWinner: Boolean, compact: Boolean = false, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        ClubLogo(clubId = clubId, size = if (compact) 36.dp else 50.dp)
        Spacer(Modifier.height(if (compact) 2.dp else 6.dp))
        Text(
            name,
            color = if (isWinner) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = if (compact) 11.sp else 14.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
        AnimatedContent(
            targetState = score,
            transitionSpec = {
                fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -it / 2 }) togetherWith
                fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
            }
        ) { s ->
            Text(
                s.toString(),
                color = if (isWinner) FM_GREEN else Color.White.copy(alpha = 0.4f),
                fontSize = if (compact) 38.sp else 52.sp,
                fontWeight = if (isWinner) FontWeight.Black else FontWeight.Light
            )
        }
        if (isWinner) {
            Surface(
                color = FM_GREEN.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "WINNER",
                    color = FM_GREEN,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StatRowSmall(label: String, home: String, away: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(home, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(
            label,
            color = Color.Gray.copy(alpha = 0.4f),
            fontSize = 10.sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            away,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
