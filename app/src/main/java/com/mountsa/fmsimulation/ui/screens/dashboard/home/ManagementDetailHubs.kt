package com.mountsa.fmsimulation.ui.screens.dashboard.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import java.util.Locale

@Composable
fun FinanceDetailHub(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val club = state.club
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppColumn(modifier = Modifier.weight(.8f), title = "CLUB FINANCE") {
            Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FinanceLine("Available budget", club?.budget ?: 0)
                FinanceLine("Weekly wage commitment", state.squadPlayers.sumOf { it.wage })
                FinanceLine("Squad market value", state.squadPlayers.sumOf { it.marketValue })
                HorizontalDivider(color = Color.White.copy(.08f))
                Text("Financial health", color = Color.Gray, fontSize = 11.sp)
                Text(if ((club?.budget ?: 0) > 0) "STABLE" else "ATTENTION", color = FM_GREEN, fontWeight = FontWeight.Black)
            }
        }
        AppColumn(modifier = Modifier.weight(1.2f).fillMaxHeight(), title = "ECONOMIC ACTIVITY") {
            LazyColumn(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.inboxMessages.filter { it.sender.contains("Finance", true) || it.subject.contains("financial", true) }) { message ->
                    ListItem(
                        headlineContent = { Text(message.subject, color = Color.White, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(message.message, color = Color.Gray, maxLines = 2) },
                        colors = ListItemDefaults.colors(containerColor = Color.White.copy(.025f))
                    )
                }
                if (state.inboxMessages.none { it.sender.contains("Finance", true) || it.subject.contains("financial", true) }) {
                    item { Text("No transactions recorded yet.", color = Color.Gray) }
                }
            }
        }
    }
}

@Composable private fun FinanceLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text("€${String.format(Locale.getDefault(), "%,d", value)}", color = FM_GREEN, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ObjectivesDetailHub(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppColumn(modifier = Modifier.fillMaxSize(), title = "BOARD OBJECTIVES") {
        LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.objectives) { objective ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(.035f))) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(objective.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(objective.priority.name, color = FM_GREEN, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (objective.targetValue > 0) objective.currentProgress.toFloat() / objective.targetValue else 0f },
                            modifier = Modifier.fillMaxWidth().height(5.dp), color = FM_GREEN
                        )
                        Text("${objective.currentProgress} / ${objective.targetValue}", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
            if (state.objectives.isEmpty()) item { Text("No board objectives available.", color = Color.Gray) }
        }
    }
}
