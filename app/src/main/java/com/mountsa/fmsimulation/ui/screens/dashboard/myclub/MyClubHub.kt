package com.mountsa.fmsimulation.ui.screens.dashboard.myclub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.PlayerAvatar
import com.mountsa.fmsimulation.core.enums.SquadRole

private data class StaffCandidate(val name: String, val specialty: String, val rating: Int, val nationId: Long)

private val staffMarket = listOf(
    StaffCandidate("Marco Silva", "TACTICS", 88, 38L),
    StaffCandidate("Daniel Wright", "FITNESS", 84, 14L),
    StaffCandidate("Luis Ortega", "SCOUTING", 91, 45L),
    StaffCandidate("Kenji Mori", "YOUTH", 86, 83L),
    StaffCandidate("Milan Petrovic", "GOALKEEPING", 82, 21L),
    StaffCandidate("Andre Costa", "MEDICAL", 89, 7L)
)

@Composable
fun MyClubHub(viewModel: DashboardViewModel) {
    val club by viewModel.club.collectAsStateWithLifecycle()
    val career by viewModel.career.collectAsStateWithLifecycle()
    val players by viewModel.squadPlayers.collectAsStateWithLifecycle()
    var rightTab by remember { mutableIntStateOf(0) }
    var selectedPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
    var staffQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val academy = players.filter { it.age <= 18 || it.squadRole == SquadRole.PROSPECT }
    
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(1.5f), title = "CLUB INFO") {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("MANAGER PROFILE", color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Surface(color = Color.White.copy(.035f), shape = RoundedCornerShape(9.dp)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("CAREER MANAGER", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${club?.managerPersonality?.name?.replace('_', ' ') ?: "BALANCED"} • ${club?.formation ?: "4-3-3"}", color = Color.Gray, fontSize = 9.sp)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text("${career?.managerRating ?: 75}", color = FM_GREEN, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("REPUTATION ${club?.reputation ?: 0}", color = Color.Gray, fontSize = 8.sp)
                        }
                    }
                }
                ClubDetailRow("Stadium", club?.stadium ?: "N/A")
                ClubDetailRow("Reputation", "${club?.reputation}/100")
                ClubDetailRow("Formation specialist", club?.formation ?: "4-3-3")
                ClubDetailRow("Tactical specialist", club?.mentality?.name?.replace('_', ' ') ?: "BALANCED")
                ClubDetailRow("Manager salary", "€${String.format("%,d", ((career?.managerRating ?: 75) * 1_250L))}/wk")
                ClubDetailRow("Staff slots", "${uiState.scouts.size}/6")
                ClubDetailRow("Squad record", "${players.size} players • ${players.count { it.injuryDaysRemaining > 0 }} injured")
            }
        }
        AppColumn(modifier = Modifier.weight(1f), title = "FACILITIES") {
            Column(Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = rightTab, containerColor = Color.Transparent) {
                    Tab(rightTab == 0, { rightTab = 0 }, text = { Text("FACILITIES", fontSize = 9.sp) })
                    Tab(rightTab == 1, { rightTab = 1 }, text = { Text("STAFF", fontSize = 9.sp) })
                    Tab(rightTab == 2, { rightTab = 2 }, text = { Text("ACADEMY", fontSize = 9.sp) })
                    Tab(rightTab == 3, { rightTab = 3 }, text = { Text("TALKS", fontSize = 9.sp) })
                }
                if (rightTab == 0) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        FacilityUpgradeRow("Medical Center", (club?.fanSatisfaction ?: 50), "MEDICAL") { viewModel.upgradeFacility(it) }
                        FacilityUpgradeRow("Training Ground", listOf(club?.attack ?: 0, club?.midfield ?: 0, club?.defense ?: 0).average().toInt(), "TRAINING") { viewModel.upgradeFacility(it) }
                        FacilityUpgradeRow("Data & Recruitment", club?.reputation ?: 50, "DATA") { viewModel.upgradeFacility(it) }
                        FacilityUpgradeRow("Youth Academy", club?.academyQuality ?: 50, "ACADEMY") { viewModel.upgradeFacility(it) }
                    }
                } else if (rightTab == 1) {
                    Column(Modifier.fillMaxSize().padding(8.dp)) {
                        OutlinedTextField(
                            value = staffQuery,
                            onValueChange = { staffQuery = it },
                            singleLine = true,
                            label = { Text("Search specialist / rating", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        )
                        Text("HIRED ${uiState.scouts.size}/6", color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 5.dp))
                        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(uiState.scouts, key = { "hired-${it.id}" }) { scout ->
                                Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(scout.name, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Text("STAFF • ${scout.judgmentAbility}", color = FM_GREEN, fontSize = 7.5.sp)
                                }
                            }
                            items(staffMarket.filter { candidate ->
                                staffQuery.isBlank() || candidate.name.contains(staffQuery, true) || candidate.specialty.contains(staffQuery, true) || candidate.rating.toString().contains(staffQuery)
                            }) { candidate ->
                                Row(Modifier.fillMaxWidth().background(Color.White.copy(.03f), RoundedCornerShape(7.dp)).padding(7.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(candidate.name, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        Text(candidate.specialty, color = Color.Gray, fontSize = 8.sp)
                                    }
                                    Text(candidate.rating.toString(), color = FM_GREEN, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Spacer(Modifier.width(6.dp))
                                    TextButton(
                                        onClick = { viewModel.hireScout(candidate.name, candidate.nationId, candidate.rating) },
                                        enabled = uiState.scouts.size < 6 && uiState.scouts.none { it.name == candidate.name },
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) { Text("HIRE", fontSize = 8.sp) }
                                }
                            }
                        }
                    }
                } else if (rightTab == 2) {
                    Column(Modifier.fillMaxSize().padding(8.dp)) {
                        Button(
                            onClick = viewModel::scoutAcademy,
                            modifier = Modifier.fillMaxWidth().height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN)
                        ) { Text("SCOUT YOUTH INTAKE • €250K", color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.Black) }
                        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(top = 6.dp)) {
                            items(academy, key = { it.id }) { player ->
                                Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    PlayerAvatar(player, 28.dp)
                                    Spacer(Modifier.width(7.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(player.shortName, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${player.position} • OVR ${player.overall} • POT ${player.potential}", color = Color.Gray, fontSize = 7.5.sp)
                                    }
                                    TextButton(onClick = { viewModel.promoteAcademyPlayer(player.id) }) { Text("PROMOTE", color = FM_GREEN, fontSize = 7.5.sp) }
                                }
                            }
                        }
                    }
                } else if (selectedPlayer == null) {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        items(players, key = { it.id }) { player ->
                            Row(
                                Modifier.fillMaxWidth().clickable { selectedPlayer = player }.padding(6.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                PlayerAvatar(player, 31.dp)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(player.shortName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Morale ${player.morale} • ${player.happiness.name}", color = Color.Gray, fontSize = 8.sp)
                                }
                                Text("TALK", color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        PlayerAvatar(selectedPlayer!!, 56.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(selectedPlayer!!.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Morale ${selectedPlayer!!.morale} • ${selectedPlayer!!.happiness.name}", color = Color.Gray, fontSize = 9.sp)
                        Spacer(Modifier.height(10.dp))
                        listOf("PRAISE" to "Praise form", "PROMISE" to "Promise minutes", "WARN" to "Warn discipline").forEach { (action, label) ->
                            OutlinedButton(
                                onClick = { viewModel.interactWithPlayer(selectedPlayer!!.id, action); selectedPlayer = null },
                                modifier = Modifier.fillMaxWidth().height(34.dp)
                            ) { Text(label, color = FM_GREEN, fontSize = 9.sp) }
                        }
                        TextButton(onClick = { selectedPlayer = null }) { Text("BACK", color = Color.Gray, fontSize = 9.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacilityUpgradeRow(name: String, level: Int, key: String, onUpgrade: (String) -> Unit) {
    Surface(color = Color.White.copy(.03f), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(name, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onUpgrade(key) }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) {
                    Text("UPGRADE", color = FM_GREEN, fontSize = 7.5.sp)
                }
            }
            LinearProgressIndicator(
                progress = { level.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = FM_GREEN,
                trackColor = Color.White.copy(.1f)
            )
            Text("LEVEL $level/100", color = Color.Gray, fontSize = 7.5.sp)
        }
    }
}

@Composable
fun ClubDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
