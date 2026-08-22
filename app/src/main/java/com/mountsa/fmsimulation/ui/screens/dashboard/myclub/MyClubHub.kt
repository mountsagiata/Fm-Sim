package com.mountsa.fmsimulation.ui.screens.dashboard.myclub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.components.FacilityItem
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.PlayerAvatar
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MyClubHub(viewModel: DashboardViewModel) {
    val club by viewModel.club.collectAsStateWithLifecycle()
    val players by viewModel.squadPlayers.collectAsStateWithLifecycle()
    val managerProfile by viewModel.managerProfile.collectAsStateWithLifecycle()
    var rightTab by remember { mutableIntStateOf(0) }
    var selectedPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
    
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(1.5f), title = "CLUB INFO") {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(color = Color.White.copy(.035f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = managerProfile?.avatarUri?.let { uri ->
                                ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build()
                            } ?: ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/database/faces/0.webp")
                                .crossfade(true)
                                .build(),
                            contentDescription = managerProfile?.name,
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("MANAGER PROFILE", color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            Text(managerProfile?.name ?: "Manager", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(managerProfile?.title ?: "CAREER MANAGER", color = Color.Gray, fontSize = 8.sp)
                        }
                    }
                }
                ClubDetailRow("Stadium", club?.stadium ?: "N/A")
                ClubDetailRow("Reputation", "${club?.reputation}/100")
                ClubDetailRow("Formation", club?.formation ?: "4-3-3")
                ClubDetailRow("Tactical style", club?.mentality?.name ?: "BALANCED")
                ClubDetailRow("Youth Academy", "${club?.academyQuality ?: 0}/100")
                ClubDetailRow("Squad record", "${players.size} players")
                Spacer(Modifier.height(8.dp))
            }
        }
        AppColumn(modifier = Modifier.weight(1f), title = "FACILITIES") {
            Column(Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = rightTab, containerColor = Color.Transparent, modifier = Modifier.height(34.dp)) {
                    listOf("FACILITIES", "STAFF", "ACADEMY", "TALKS").forEachIndexed { index, title ->
                        Tab(
                            selected = rightTab == index,
                            onClick = { rightTab = index; selectedPlayer = null },
                            modifier = Modifier.height(34.dp),
                            text = { Text(title, fontSize = 8.sp) }
                        )
                    }
                }
                if (rightTab == 0) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FacilityItem("Medical Center", 0.85f)
                        FacilityItem("Training Pitch", 0.90f)
                        FacilityItem("Data Hub", 0.60f)
                        FacilityItem("Youth Academy", 0.75f)
                    }
                } else if (rightTab == 1) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(listOf("Assistant Manager", "Head Coach", "Chief Scout", "Head Physio")) { role ->
                            Surface(color = Color.White.copy(.03f), shape = androidx.compose.foundation.shape.RoundedCornerShape(7.dp)) {
                                Row(Modifier.fillMaxWidth().padding(9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(role, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("VACANT", color = Color.Gray, fontSize = 8.sp)
                                }
                            }
                        }
                    }
                } else if (rightTab == 2) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(color = Color.White.copy(.035f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("file:///android_asset/database/faces/0.webp")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Youth intake prospect",
                                    modifier = Modifier.size(44.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("YOUTH INTAKE", color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    Text("Next intake in 4 months", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Academy quality ${club?.academyQuality ?: 0}/100", color = Color.Gray, fontSize = 8.sp)
                                }
                            }
                        }
                        ClubDetailRow("Local recruitment", "${club?.localNationBias ?: 0}/100")
                        ClubDetailRow("Academy level", "${club?.academyQuality ?: 0}/100")
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
fun ClubDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
