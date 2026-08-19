package com.mountsa.fmsimulation.ui.screens.dashboard.scouting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.mountsa.fmsimulation.data.local.entities.ScoutEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun ScoutingHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Scout List
        AppColumn(
            modifier = Modifier.weight(1f),
            title = "SCOUTING NETWORK"
        ) {
            if (uiState.scouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No scouts hired", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.scouts) { scout ->
                        ScoutRowItem(scout)
                    }
                }
            }
        }

        // Right Column: Assignments & Search
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppColumn(
                modifier = Modifier.weight(1f),
                title = "ACTIVE ASSIGNMENTS"
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active assignments", color = Color.Gray, fontSize = 12.sp)
                }
            }

            AppColumn(
                modifier = Modifier.height(120.dp),
                title = "PLAYER SEARCH"
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = FM_GREEN,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text("FIND PLAYERS", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Search the database for specific targets", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoutRowItem(scout: ScoutEntity) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(scout.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Ability: ${scout.judgmentAbility} | Potential: ${scout.judgmentPotential}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            if (scout.assignmentId != null) {
                Box(
                    modifier = Modifier
                        .background(FM_GREEN.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ASSIGNED", color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("IDLE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
