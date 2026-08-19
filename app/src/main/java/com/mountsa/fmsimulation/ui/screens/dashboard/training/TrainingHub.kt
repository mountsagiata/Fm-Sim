package com.mountsa.fmsimulation.ui.screens.dashboard.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun TrainingHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val players = uiState.squadPlayers

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Squad Training Status
        AppColumn(
            modifier = Modifier.weight(1.5f),
            title = "SQUAD TRAINING PERFORMANCE"
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(players.sortedByDescending { it.sharpness }) { player ->
                    TrainingPlayerRow(player)
                }
            }
        }

        // Right Column: Training Focus & Intensity
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppColumn(
                modifier = Modifier.weight(1f),
                title = "UNIT FOCUS"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainingFocusItem("Attacking", 0.8f, Icons.Default.FitnessCenter)
                    TrainingFocusItem("Tactical", 0.6f, Icons.Default.Psychology)
                    TrainingFocusItem("Physical", 0.7f, Icons.Default.History)
                }
            }

            AppColumn(
                modifier = Modifier.height(140.dp),
                title = "ACADEMY FOCUS"
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Youth Development", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Quality: ${uiState.club?.academyQuality ?: 0}/100", color = FM_GREEN, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (uiState.club?.academyQuality ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = FM_GREEN,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TrainingPlayerRow(player: PlayerEntity) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(player.position, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
            Text(player.shortName, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
            
            Column(horizontalAlignment = Alignment.End) {
                Text("SHARPNESS", color = Color.Gray, fontSize = 8.sp)
                Text("${player.sharpness}%", color = if (player.sharpness > 70) FM_GREEN else Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text("FATIGUE", color = Color.Gray, fontSize = 8.sp)
                Text("${player.fatigue}%", color = if (player.fatigue > 50) Color.Red else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TrainingFocusItem(label: String, progress: Float, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}%", color = Color.Gray, fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = FM_GREEN,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}
