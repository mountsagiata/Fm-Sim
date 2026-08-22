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
    val career by viewModel.career.collectAsStateWithLifecycle()
    val club = state.club
    val weeklyPlayerWages = state.squadPlayers.sumOf { it.wage }
    val managerWeeklySalary = ((career?.managerRating ?: 75) * 1_250L).coerceAtLeast(25_000L)
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppColumn(modifier = Modifier.weight(.82f), title = "FINANCIAL CONTROL") {
            Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FinanceLine("Club balance", club?.budget ?: 0)
                FinanceLine("Transfer budget", career?.transferBudget ?: club?.budget ?: 0)
                FinanceLine("Player wages / week", weeklyPlayerWages)
                FinanceLine("Manager salary / week", managerWeeklySalary)
                FinanceLine("Wage budget / week", club?.wageBudget ?: 0)
                HorizontalDivider(color = Color.White.copy(.08f))
                val wageLimit = club?.wageBudget?.takeIf { it > 0L } ?: (weeklyPlayerWages * 12 / 10)
                val wageUsage = if (wageLimit > 0) weeklyPlayerWages.toFloat() / wageLimit else 0f
                Text("WAGE CONTROL ${(wageUsage * 100).toInt()}%", color = Color.Gray, fontSize = 9.sp)
                LinearProgressIndicator(
                    progress = { wageUsage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = if (wageUsage < .9f) FM_GREEN else Color(0xFFFF6B6B)
                )
                Text(if ((club?.budget ?: 0) > 0) "STABLE" else "ATTENTION", color = FM_GREEN, fontWeight = FontWeight.Black)
            }
        }
        AppColumn(modifier = Modifier.weight(1f).fillMaxHeight(), title = "WAGE STRUCTURE") {
            LazyColumn(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.squadPlayers.sortedByDescending { it.wage }, key = { it.id }) { player ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(player.shortName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${player.position} • ${player.squadRole.name.replace('_', ' ')}", color = Color.Gray, fontSize = 8.sp)
                        }
                        Text("€${String.format(Locale.getDefault(), "%,d", player.wage)}", color = FM_GREEN, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color.White.copy(.045f))
                }
            }
        }
        AppColumn(modifier = Modifier.weight(1.05f).fillMaxHeight(), title = "ECONOMIC ACTIVITY") {
            LazyColumn(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
