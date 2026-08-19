// ui/screens/squad/SquadScreen.kt
package com.mountsa.fmsimulation.ui.screens.squad
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.domain.models.Formation
import com.mountsa.fmsimulation.domain.models.Formations
import com.mountsa.fmsimulation.ui.viewmodel.SquadViewModel
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Rest of the code...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(
    viewModel: SquadViewModel = hiltViewModel()
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    val selectedFormation by viewModel.selectedFormation.collectAsStateWithLifecycle()
    val startingXI by viewModel.startingXI.collectAsStateWithLifecycle()
    val substitutes by viewModel.substitutes.collectAsStateWithLifecycle()
    val showPlayerSelector by viewModel.showPlayerSelector.collectAsStateWithLifecycle()
    val selectedPlayer by viewModel.selectedPlayer.collectAsStateWithLifecycle()

    // Set club ID when screen loads
    LaunchedEffect(Unit) {
        // You need to get club ID from repository
        // viewModel.setClubId(clubId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Squad Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Formation selector
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = {},
                        modifier = Modifier
                    ) {
                        Formations.DEFAULT_FORMATIONS.forEach { formation ->
                            DropdownMenuItem(
                                text = { Text(formation.name) },
                                onClick = { viewModel.selectFormation(formation) }
                            )
                        }
                    }

                    TextButton(onClick = { viewModel.saveLineup() }) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left side - Formation Pitch
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1A5C2E))
            ) {
                FormationPitch(
                    formation = selectedFormation,
                    startingXI = startingXI,
                    onPlayerClick = { index ->
                        viewModel.openPlayerSelector(index)
                    }
                )
            }

            // Right side - Players List & Substitutes
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Starting XI Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "STARTING XI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            startingXI.filterNotNull().take(5).forEach { player ->
                                PlayerChip(player = player, size = 40)
                            }
                            if (startingXI.filterNotNull().size > 5) {
                                Text("+${startingXI.filterNotNull().size - 5}", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Substitutes
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "SUBSTITUTES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        substitutes.forEachIndexed { index, player ->
                            if (player != null) {
                                SubstituteItem(
                                    player = player,
                                    onMoveToXI = { viewModel.moveToStartingXI(player) }
                                )
                            }
                        }
                    }
                }

                // Available Players
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val availablePlayers = viewModel.getAvailablePlayers()

                    item {
                        Text(
                            "AVAILABLE PLAYERS (${availablePlayers.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    items(availablePlayers) { player ->
                        AvailablePlayerItem(
                            player = player,
                            onAddToSub = { viewModel.moveToSubstitute(player) }
                        )
                    }
                }
            }
        }
    }

    // Player Selector Dialog
    if (showPlayerSelector) {
        PlayerSelectorDialog(
            players = players,
            onSelect = { viewModel.selectPlayerForPosition(it) },
            onDismiss = { viewModel.closePlayerSelector() }
        )
    }
}
@Composable
fun FormationPitch(
    formation: Formation,
    startingXI: List<PlayerEntity?>,
    onPlayerClick: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Pitch background
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0D3B1F))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0D3B1F))
            )
        }

        // Player positions
        formation.positions.forEachIndexed { index, position ->
            val player = startingXI.getOrNull(index)
            val xPos = (position.x / 100f) * (configuration.screenWidthDp - 32)
            val yPos = (position.y / 100f) * (configuration.screenHeightDp - 32)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = xPos.dp, y = yPos.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            if (player != null) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.5f)
                        )
                        .clickable { onPlayerClick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        if (player != null) {
                            Text(
                                text = player.shirtNumber.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = position.name,
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = position.name,
                                fontSize = 8.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerPositionButton(
    player: PlayerEntity?,
    positionName: String,
    x: Float,
    y: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(
                    x = (x * (LocalConfiguration.current.screenWidthDp - 32).dp).value.dp,
                    y = (y * (LocalConfiguration.current.screenHeightDp - 32).dp).value.dp
                )
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    if (player != null) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.5f)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                if (player != null) {
                    Text(
                        text = player.shirtNumber.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = positionName,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = positionName,
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerChip(
    player: PlayerEntity,
    size: Int = 36
) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = Color(0xFF2196F3)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = player.shirtNumber.toString(),
                fontSize = (size / 3).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun SubstituteItem(
    player: PlayerEntity,
    onMoveToXI: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMoveToXI() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = player.shirtNumber.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                Column {
                    Text(
                        text = player.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${player.position} • OVR ${player.overall}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Move to Starting XI",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun AvailablePlayerItem(
    player: PlayerEntity,
    onAddToSub: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddToSub() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = player.shirtNumber.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Column {
                    Text(
                        text = player.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${player.position} • OVR ${player.overall} • Age ${player.age}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Add to Substitutes",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun PlayerSelectorDialog(
    players: List<PlayerEntity>,
    onSelect: (PlayerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Player") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(players) { player ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(player) },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = player.shirtNumber.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(30.dp)
                            )
                            Column {
                                Text(player.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "${player.position} • OVR ${player.overall}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}