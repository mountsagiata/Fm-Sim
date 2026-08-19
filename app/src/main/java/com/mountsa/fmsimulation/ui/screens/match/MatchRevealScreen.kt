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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FM_DARK_BG),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR - ANIMASI DARI ATAS
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(800))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = session.competitionName.uppercase(),
                        color = FM_GREEN,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = session.stadiumName,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            // MIDDLE SECTION (HOME - VS - AWAY)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // HOME - ANIMASI DARI KIRI
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(1000)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ClubLogo(clubId = session.match.homeClubId, size = 70.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            session.homeClubName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                // VS - TETAP
                Text(
                    "VS",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // AWAY - ANIMASI DARI KANAN
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(1000)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ClubLogo(clubId = session.match.awayClubId, size = 70.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            session.awayClubName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }

            // BUTTON - TETAP
            Button(
                onClick = { viewModel.nextMatchFlowStep() },
                colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                modifier = Modifier
                    .width(180.dp)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("PREPARE LINEUP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
