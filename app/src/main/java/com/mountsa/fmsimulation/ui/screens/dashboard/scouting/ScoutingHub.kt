package com.mountsa.fmsimulation.ui.screens.dashboard.scouting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var selectedScout by remember { mutableStateOf<ScoutEntity?>(null) }
    var query by remember { mutableStateOf("") }

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
                    Text(com.mountsa.fmsimulation.ui.localization.localized("No scouts hired"), color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.scouts) { scout ->
                        ScoutRowItem(scout, selectedScout?.id == scout.id) { selectedScout = scout }
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
                    Text(
                        selectedScout?.let { "${it.name} selected • choose a player below" } ?: "Select a scout to start an assignment",
                        color = if (selectedScout != null) FM_GREEN else Color.Gray, fontSize = 12.sp
                    )
                }
            }

            AppColumn(
                modifier = Modifier.weight(1.25f),
                title = "PLAYER SEARCH"
            ) {
                Column(Modifier.fillMaxSize().padding(8.dp)) {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        placeholder = { Text("Name or position") }, modifier = Modifier.fillMaxWidth()
                    )
                    if (query.isNotBlank()) LazyColumn(Modifier.fillMaxWidth()) {
                        items(uiState.squadPlayers.filter { it.name.contains(query, true) || it.position.contains(query, true) }.take(8)) { player ->
                            Row(Modifier.fillMaxWidth().clickable(enabled = selectedScout != null) { query = "Assigned: ${player.shortName}" }.padding(7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(player.shortName, color = Color.White, fontSize = 10.sp)
                                Text("${player.position} • ${player.overall}", color = FM_GREEN, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoutRowItem(scout: ScoutEntity, selected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(
        color = if (selected) FM_GREEN.copy(alpha = .13f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(com.mountsa.fmsimulation.ui.localization.localized("👤"), fontSize = 18.sp)
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
                    Text(com.mountsa.fmsimulation.ui.localization.localized("ASSIGNED"), color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(com.mountsa.fmsimulation.ui.localization.localized("IDLE"), color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
