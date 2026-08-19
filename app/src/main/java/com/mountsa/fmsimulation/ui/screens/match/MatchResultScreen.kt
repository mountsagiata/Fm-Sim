package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.DashboardUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MatchResultScreen(viewModel: DashboardViewModel) {
    val uiState: DashboardUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return
    val match = session.match

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FM_DARK_BG)
            .padding(16.dp)
    ) {
        val screenHeight = maxHeight
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "FULL TIME", 
                color = FM_GREEN, 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(16.dp))

            // SCORE SECTION - Compact
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                ResultTeamCompact(name = session.homeClubName, score = match.homeScore, modifier = Modifier.weight(1f))
                Text(
                    "-", 
                    color = Color.White, 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                ResultTeamCompact(name = session.awayClubName, score = match.awayScore, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // STATS CARD - Compact
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    StatRowSmall("Shots", "${match.shotsHome}", "${match.shotsAway}")
                    StatRowSmall("On Target", "${match.shotsOnTargetHome}", "${match.shotsOnTargetAway}")
                    StatRowSmall("Possession", "${match.possessionHome}%", "${match.possessionAway}%")
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.nextMatchFlowStep() },
                colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ResultTeamCompact(name: String, score: Int, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            name, 
            color = Color.White, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
        Text(
            score.toString(), 
            color = Color.White, 
            fontSize = 48.sp, 
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun StatRowSmall(label: String, home: String, away: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(home, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(
            label, 
            color = Color.Gray, 
            fontSize = 11.sp, 
            modifier = Modifier.weight(1f), 
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            away, 
            color = Color.White, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier.weight(1f), 
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
