package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun StartingLineupScreen(viewModel: DashboardViewModel) {
    val uiState: DashboardUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FM_DARK_BG)
            .padding(16.dp)
    ) {
        val maxHeight = maxHeight
        
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "STARTING XI",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Finalize tactics",
                color = Color.Gray,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(12.dp))

            // Player List with smaller font and padding
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(session.homeLineup.filter { it.startingIndex in 0..10 }) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                player.name, 
                                color = Color.White, 
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                player.position, 
                                color = FM_GREEN,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.nextMatchFlowStep() },
                colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("KICK OFF", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
