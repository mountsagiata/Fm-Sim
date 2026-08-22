package com.mountsa.fmsimulation.ui.screens.dashboard.squad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.domain.models.Formation
import com.mountsa.fmsimulation.domain.models.Formations
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.SquadViewModel
import java.util.Locale

@Composable
fun SquadHub(dashboardViewModel: DashboardViewModel, squadViewModel: SquadViewModel) {
    val club by dashboardViewModel.club.collectAsStateWithLifecycle()
    val players by squadViewModel.players.collectAsStateWithLifecycle()
    val selectedFormation by squadViewModel.selectedFormation.collectAsStateWithLifecycle()
    val startingXI by squadViewModel.startingXI.collectAsStateWithLifecycle()
    val showPlayerSelector by squadViewModel.showPlayerSelector.collectAsStateWithLifecycle()
    val selectedPlayer by squadViewModel.selectedPlayer.collectAsStateWithLifecycle()
    var compactPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(club) {
        club?.let { squadViewModel.setClubId(it.id) }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
    val compact = maxWidth < 600.dp
    if (compact) {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = compactPage, containerColor = Color.Transparent) {
                Tab(compactPage == 0, { compactPage = 0 }, text = { Text(com.mountsa.fmsimulation.ui.localization.localized("PITCH")) })
                Tab(compactPage == 1, { compactPage = 1 }, text = { Text(com.mountsa.fmsimulation.ui.localization.localized("SQUAD")) })
            }
            if (compactPage == 0) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    TacticsPitch(selectedFormation, startingXI) { squadViewModel.openPlayerSelector(it) }
                    IconButton(
                        onClick = { squadViewModel.saveLineup() },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(40.dp).background(FM_GREEN, CircleShape)
                    ) { Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black) }
                }
            } else {
                Column(Modifier.weight(1f).fillMaxWidth()) {
                    SquadHeaderRow()
                    LazyColumn(Modifier.weight(1f)) {
                        items(players.sortedByDescending { it.overall }) { player ->
                            DetailedSquadPlayerRow(player, startingXI.any { it?.id == player.id }) { squadViewModel.selectPlayer(player) }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(54.dp).horizontalScroll(rememberScrollState()).padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Formations.DEFAULT_FORMATIONS.forEach { formation ->
                    FormationButton(
                        formation.name, formation.name == selectedFormation.name,
                        { squadViewModel.selectFormation(formation) }, Modifier.width(76.dp)
                    )
                }
            }
        }
    } else Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Kolom Kiri: Lineup atau Detail Player ---
        Box(modifier = Modifier.weight(1.1f)) {
            if (selectedPlayer == null) {
                AppColumn(
                    modifier = Modifier.fillMaxSize(),
                    title = ""
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TacticsPitch(
                            formation = selectedFormation,
                            startingXI = startingXI,
                            onSlotClick = { index -> squadViewModel.openPlayerSelector(index) }
                        )

                        // Save Button
                        IconButton(
                            onClick = { squadViewModel.saveLineup() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(44.dp)
                                .background(FM_GREEN, CircleShape)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black)
                        }
                    }
                }
            } else {
                PlayerDetailView(
                    player = selectedPlayer!!,
                    onClose = { squadViewModel.selectPlayer(null) }
                )
            }
        }

        // --- Kolom Kanan: Squad List & Formation Selection ---
        Column(
            modifier = Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Squad List
            AppColumn(
                modifier = Modifier.weight(1f),
                title = ""
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SquadHeaderRow()

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(players.sortedByDescending { it.overall }) { player ->
                            val isStarting = startingXI.any { it?.id == player.id }
                            DetailedSquadPlayerRow(
                                player = player,
                                isStarting = isStarting,
                                onClick = { squadViewModel.selectPlayer(player) }
                            )
                        }
                    }
                }
            }

            // Formation Selection Buttons
            AppColumn(modifier = Modifier.height(90.dp), title = "FORMATION") {
                Row(
                    modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Formations.DEFAULT_FORMATIONS.forEach { formation ->
                        val isSelected = formation.name == selectedFormation.name
                        FormationButton(
                            name = formation.name,
                            isSelected = isSelected,
                            onClick = { squadViewModel.selectFormation(formation) },
                            modifier = Modifier.width(76.dp)
                        )
                    }
                }
            }
        }
    }
    }

    if (showPlayerSelector) {
        PlayerSelectorDialog(
            availablePlayers = squadViewModel.getAvailablePlayers(),
            onPlayerSelected = { squadViewModel.selectPlayerForPosition(it) },
            onDismiss = { squadViewModel.closePlayerSelector() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerDetailView(player: PlayerEntity, onClose: () -> Unit) {
    AppColumn(
        modifier = Modifier.fillMaxSize(),
        title = "PLAYER DETAILS"
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Tombol Back: elemen TETAP (bukan didorong via weight-spacer), jadi
            // selalu terlihat di layar sekecil apapun, tidak pernah ikut hilang.
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(com.mountsa.fmsimulation.ui.localization.localized("BACK"), color = Color.White, fontSize = 12.sp)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // --- Kolom 1: Avatar & Info Dasar (Center Aligned), scrollable jika layar pendek ---
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp) // Ukuran avatar diperbesar agar lebih jelas
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/${player.avatarAsset}")
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Player Name
                Text(
                    text = player.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, // Diperbesar dari 13.sp
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                // Age | OVR
                Text(
                    text = "${player.age} Years | OVR: ${player.overall}",
                    color = Color.Gray,
                    fontSize = 12.sp, // Diperbesar dari 11.sp
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(2.dp))

                // Value
                Text(
                    text = "€${formatValue(player.marketValue)}",
                    color = Color(0xFF2ECC71), // Warna hijau khas nilai transfer pasar
                    fontSize = 14.sp, // Diperbesar dari 12.sp
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
            }

            // --- Pengelompokan Data Statistik Berdasarkan Kategori ---
            val categories = remember(player) {
                val list = mutableListOf<Pair<String, List<Pair<String, Int>>>>()

                // 1. Pace & Physical
                list.add("PHYSICAL & PACE" to listOf(
                    "Accel." to player.acceleration, "Speed" to player.sprintSpeed,
                    "Agility" to player.agility, "React." to player.reactions,
                    "Balance" to player.balance, "Jump" to player.jumping,
                    "Stamina" to player.stamina, "Strength" to player.strength,
                    "Aggr." to player.aggression
                ))

                // 2. Skill & Attack
                list.add("ATTACK & SKILLS" to listOf(
                    "Vision" to player.vision, "Pos." to player.positioning,
                    "Comp." to player.composure, "Finish." to player.finishing,
                    "Volley" to player.volleys, "Cross." to player.crossing,
                    "Head." to player.headingAccuracy, "Dribbl." to player.skillDribbling,
                    "Curve" to player.curve, "Control" to player.ballControl
                ))

                // 3. Passing & Defending (Atau data GK jika posisinya GK)
                if (player.position == "GK") {
                    list.add("GOALKEEPING" to listOf(
                        "GK Div." to player.gkDiving, "GK Hand." to player.gkHandling,
                        "GK Kick" to player.gkKicking, "GK Pos." to player.gkPositioning,
                        "GK Ref." to player.gkReflexes
                    ))
                } else {
                    list.add("DEFENDING & PASSING" to listOf(
                        "Short P." to player.shortPassing, "Long P." to player.longPassing,
                        "Penal." to player.penalties, "FK Acc." to player.fkAccuracy,
                        "Power" to player.shotPower, "Long S." to player.longShots,
                        "Mark." to player.markingAwareness, "Stand T." to player.standingTackle,
                        "Slide T." to player.slidingTackle, "Inter." to player.interceptions
                    ))
                }
                list
            }

            val pagerState = rememberPagerState(pageCount = { categories.size })

            // --- Kolom 2: Swipeable Stats Container ---
            Column(
                modifier = Modifier
                    .weight(2.8f) // Diperluas agar porsi stats lebih dominan
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Horizontal Pager Utama untuk swipe stats
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val (categoryTitle, stats) = categories[pageIndex]

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Nama Kategori Stats
                        Text(
                            text = categoryTitle,
                            color = Color(0xFF2ECC71),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Pemisahan stats menjadi 2 sub-kolom (kiri & kanan) di dalam page agar rapi
                        val halfSize = (stats.size + 1) / 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Sub-kolom Kiri
                            Column(modifier = Modifier.weight(1f)) {
                                stats.take(halfSize).forEach { stat ->
                                    StatRowItem(label = stat.first, value = stat.second)
                                }
                            }
                            // Sub-kolom Kanan
                            Column(modifier = Modifier.weight(1f)) {
                                stats.drop(halfSize).forEach { stat ->
                                    StatRowItem(label = stat.first, value = stat.second)
                                }
                            }
                        }
                    }
                }

                // --- Bagian Bottom: Divider & Slider (Pager Indicator) ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Slider Dots / Bar Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Color(0xFF2ECC71) else Color.Gray.copy(alpha = 0.4f))
                                    .size(width = if (isSelected) 20.dp else 6.dp, height = 5.dp)
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun StatRowItem(label: String, value: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), // Padding vertikal diperlonggar
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 12.sp, // Diperbesar dari 9.sp agar sangat terbaca
                maxLines = 1
            )
            Text(
                text = value.toString(),
                color = getStatColor(value),
                fontSize = 13.sp, // Diperbesar dari 10.sp
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.03f))
    }
}

fun getStatColor(value: Int): Color {
    return when {
        value >= 80 -> Color(0xFF2ECC71) // Hijau
        value >= 71 -> Color(0xFFE67E22) // Orange
        value >= 60 -> Color(0xFFF1C40F) // Kuning
        else -> Color(0xFFE74C3C) // Merah
    }
}

fun formatValue(value: Long): String {
    return when {
        value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
        value >= 1_000 -> String.format(Locale.US, "%.0fK", value / 1_000f)
        else -> value.toString()
    }
}

@Composable
fun DetailedSquadPlayerRow(player: PlayerEntity, isStarting: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable { onClick() }
            .background(
                if (isStarting) FM_GREEN.copy(alpha = 0.12f)
                else Color.White.copy(alpha = 0.02f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Jersey Number
        Text(
            text = player.shirtNumber.toString(),
            modifier = Modifier.width(22.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isStarting) FM_GREEN else Color.Gray,
            textAlign = TextAlign.Center
        )

        // Position
        Text(
            text = player.position,
            modifier = Modifier.width(30.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = getPositionColor(player.position),
            textAlign = TextAlign.Center
        )

        // Avatar Face
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/${player.avatarAsset}")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(6.dp))

        // Short Name
        Text(
            text = player.shortName,
            modifier = Modifier.weight(1f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )

        // Nation Flag
        Box(
            modifier = Modifier.width(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/${player.flagAsset}")
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(16.dp, 12.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Arrow Form
        val formIcon = when {
            player.form > 65 -> Icons.Default.ArrowDropUp to FM_GREEN
            player.form < 45 -> Icons.Default.ArrowDropDown to Color.Red
            else -> null
        }

        Box(modifier = Modifier.width(16.dp), contentAlignment = Alignment.Center) {
            formIcon?.let { (icon, color) ->
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            } ?: Text(com.mountsa.fmsimulation.ui.localization.localized("-"), color = Color.Gray, fontSize = 9.sp)
        }

        // Age
        Text(
            text = player.age.toString(),
            modifier = Modifier.width(22.dp),
            fontSize = 10.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // OVR
        Text(
            text = player.overall.toString(),
            modifier = Modifier.width(26.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.End,
            color = if (player.overall >= 85) Color(0xFFFFD700) else Color.White
        )
    }
}

@Composable
fun SquadHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(com.mountsa.fmsimulation.ui.localization.localized("#"), modifier = Modifier.width(22.dp), fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Text(com.mountsa.fmsimulation.ui.localization.localized("POS"), modifier = Modifier.width(30.dp), fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.width(28.dp))
        Text(com.mountsa.fmsimulation.ui.localization.localized("PLAYER"), modifier = Modifier.weight(1f), fontSize = 8.sp, color = Color.Gray)
        Text(com.mountsa.fmsimulation.ui.localization.localized("NAT"), modifier = Modifier.width(24.dp), fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Text(com.mountsa.fmsimulation.ui.localization.localized("FOR"), modifier = Modifier.width(16.dp), fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Text(com.mountsa.fmsimulation.ui.localization.localized("AGE"), modifier = Modifier.width(22.dp), fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Text(com.mountsa.fmsimulation.ui.localization.localized("OVR"), modifier = Modifier.width(26.dp), fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.End)
    }
}

fun getPositionColor(pos: String): Color {
    return when (pos) {
        "GK" -> Color(0xFFFFD54F)
        "CB", "LB", "RB", "LWB", "RWB" -> Color(0xFF4FC3F7)
        "CDM", "CM", "CAM", "LM", "RM" -> Color(0xFF81C784)
        "ST", "CF", "LW", "RW" -> Color(0xFFE57373)
        else -> Color.White
    }
}

@Composable
fun TacticsPitch(
    formation: Formation,
    startingXI: List<PlayerEntity?>,
    onSlotClick: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F2410))
    ) {
        val width = maxWidth
        val height = maxHeight

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1.dp.toPx()
            val color = Color.White.copy(alpha = 0.15f)
            drawRect(color, style = Stroke(stroke))
            drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height / 2), androidx.compose.ui.geometry.Offset(size.width, size.height / 2), stroke)
            drawCircle(color, radius = size.width / 6, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), style = Stroke(stroke))
            drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.82f), size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.18f), style = Stroke(stroke))
            drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.2f, 0f), size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.18f), style = Stroke(stroke))
        }

        formation.positions.forEachIndexed { index, pos ->
            val player = startingXI.getOrNull(index)
            val xOffset = (pos.x / 100f) * width.value
            val yOffset = (pos.y / 100f) * height.value

            Box(
                modifier = Modifier
                    .offset(x = xOffset.dp - 24.dp, y = yOffset.dp - 24.dp)
                    .size(48.dp)
                    .clickable { onSlotClick(index) },
                contentAlignment = Alignment.Center
            ) {
                PlayerPitchIcon(player, pos.name)
            }
        }
    }
}

@Composable
fun PlayerPitchIcon(player: PlayerEntity?, posLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (player != null) FM_GREEN else Color.White.copy(alpha = 0.1f), CircleShape)
                .border(1.2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (player != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/${player.avatarAsset}")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = player.overall.toString(),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.background(FM_GREEN, RoundedCornerShape(2.dp)).padding(horizontal = 1.dp)
                    )
                }
            } else {
                Text(posLabel, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = player?.shortName ?: "",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                .padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun FormationButton(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier
            .height(32.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) FM_GREEN.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, FM_GREEN) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                name,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) FM_GREEN else Color.White
            )
        }
    }
}

@Composable
fun PlayerSelectorDialog(
    availablePlayers: List<PlayerEntity>,
    onPlayerSelected: (PlayerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF151515)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(com.mountsa.fmsimulation.ui.localization.localized("Select Player"), color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(availablePlayers.sortedByDescending { it.overall }) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayerSelected(player) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.DarkGray)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("file:///android_asset/${player.avatarAsset}")
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(player.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(com.mountsa.fmsimulation.ui.localization.localized("${player.position} | OVR: ${player.overall}"), color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Icon(Icons.Default.SwapVert, contentDescription = null, tint = FM_GREEN)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("CANCEL"), color = Color.Gray)
                }
            }
        }
    }
}
