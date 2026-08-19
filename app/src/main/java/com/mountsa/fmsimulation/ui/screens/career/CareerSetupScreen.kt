package com.mountsa.fmsimulation.ui.screens.career

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.LeagueEntity
import com.mountsa.fmsimulation.data.local.entities.NationEntity
import com.mountsa.fmsimulation.ui.viewmodel.CareerSetupViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val DarkBackground = Color(0xFF080C10)
private val CardBackground = Color(0xFF11171D)
private val AccentGreen = Color(0xFF00FF5F)
private val TextGray = Color(0xFF94A3B8)

@Composable
fun CareerSetupScreen(
    viewModel: CareerSetupViewModel = hiltViewModel()
) {
    val step by viewModel.step.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val generationMessage by viewModel.generationMessage.collectAsStateWithLifecycle()
    val generationProgress by viewModel.generationProgress.collectAsStateWithLifecycle()

    val nations by viewModel.nations.collectAsStateWithLifecycle()
    val leagues by viewModel.leagues.collectAsStateWithLifecycle()
    val clubs by viewModel.clubs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val selectedNation by viewModel.selectedNation.collectAsStateWithLifecycle()
    val selectedLeague by viewModel.selectedLeague.collectAsStateWithLifecycle()
    val selectedClub by viewModel.selectedClub.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { /* Handle back */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(CardBackground, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "NEW CAREER",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose your path to glory",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }

                CareerStepper(currentStep = step)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.confirmCareer() },
                        enabled = selectedClub != null && !loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (selectedClub != null && !loading) AccentGreen else Color.DarkGray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("Start Game", color = if (selectedClub != null) AccentGreen else TextGray, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (selectedClub != null) AccentGreen else TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Three Panel Layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Panel 1: Choose Country
                SelectionPanel(
                    modifier = Modifier.weight(1f),
                    title = "CHOOSE COUNTRY",
                    subtitle = "Select a country",
                    icon = Icons.Default.Public,
                    isSelected = selectedNation != null
                ) {
                    Column {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            hint = "Search country..."
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(nations) { nation ->
                                NationListItem(
                                    nation = nation,
                                    isSelected = selectedNation?.id == nation.id,
                                    onClick = { viewModel.selectNation(nation) }
                                )
                            }
                        }
                    }
                }

                // Panel 2: Choose League
                SelectionPanel(
                    modifier = Modifier.weight(1f),
                    title = "CHOOSE LEAGUE",
                    subtitle = selectedNation?.let { "Available leagues in ${it.name}" } ?: "Select country first",
                    icon = Icons.Default.EmojiEvents,
                    enabled = selectedNation != null,
                    isSelected = selectedLeague != null
                ) {
                    if (selectedNation == null) {
                        EmptySelectionHint("Select a country to see available leagues")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(leagues) { leagueUi ->
                                LeagueListItem(
                                    league = leagueUi.league,
                                    teamCount = leagueUi.teamCount,
                                    isSelected = selectedLeague?.id == leagueUi.league.id,
                                    onClick = { viewModel.selectLeague(leagueUi.league) }
                                )
                            }
                        }
                    }
                }

                // Panel 3: Choose Team
                SelectionPanel(
                    modifier = Modifier.weight(1.3f),
                    title = "CHOOSE TEAM",
                    subtitle = selectedLeague?.let { "Clubs in ${it.name}" } ?: "Select league first",
                    icon = Icons.Default.Shield,
                    enabled = selectedLeague != null,
                    isSelected = selectedClub != null
                ) {
                    if (selectedLeague == null) {
                        EmptySelectionHint("Select a league to see available teams")
                    } else {
                        Column {
                            if (selectedClub != null) {
                                TeamDetailCard(club = selectedClub!!)
                                Spacer(Modifier.height(16.dp))
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(clubs) { club ->
                                    TeamListItem(
                                        club = club,
                                        isSelected = selectedClub?.id == club.id,
                                        onClick = { viewModel.selectClub(club) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loading/Generation Overlay
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(300.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { generationProgress },
                        color = AccentGreen,
                        trackColor = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = generationMessage,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { generationProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = AccentGreen,
                        trackColor = Color.DarkGray
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${(generationProgress * 100).toInt()}%",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySelectionHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = TextGray.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
fun NationListItem(nation: NationEntity, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) AccentGreen else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFF1E293B) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "file:///android_asset/${nation.flagAsset}",
            contentDescription = null,
            modifier = Modifier.size(24.dp, 16.dp),
            contentScale = ContentScale.FillBounds,
            error = null
        )
        Spacer(Modifier.width(10.dp))
        Text(nation.name, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(nation.shortName, color = if (isSelected) AccentGreen else TextGray, fontSize = 10.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextGray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun LeagueListItem(league: LeagueEntity, teamCount: Int, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) AccentGreen else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFF1E293B) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "file:///android_asset/${league.logoAsset}",
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(league.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Division ${league.division}", color = TextGray, fontSize = 10.sp)
            }
        }
        Text("$teamCount Teams", color = TextGray, fontSize = 10.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextGray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun TeamDetailCard(club: ClubEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "file:///android_asset/${club.logoAsset}",
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(club.name.uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Row {
                    val stars = when {
                        club.overall >= 80 -> 5
                        club.overall >= 75 -> 4
                        club.overall >= 70 -> 3
                        else -> 2
                    }
                    repeat(stars) { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp)) }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AttributeItem("ATT", club.attack)
                    AttributeItem("MID", club.midfield)
                    AttributeItem("DEF", club.defense)
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
                CircularProgressIndicator(
                    progress = { club.overall / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = AccentGreen,
                    strokeWidth = 3.dp,
                    trackColor = Color.DarkGray
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(club.overall.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailItem("Budget", "€${club.budget / 1_000_000}.0M")
            DetailItem("Stadium", club.stadium)
            DetailItem("Rep", "${club.reputation}")
        }
    }
}

@Composable
fun TeamListItem(club: ClubEntity, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) AccentGreen else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFF1E293B) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "file:///android_asset/${club.logoAsset}",
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(club.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row {
                val stars = when {
                    club.overall >= 80 -> 4
                    club.overall >= 70 -> 3
                    else -> 2
                }
                repeat(stars) { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp)) }
            }
        }
        Box(
            modifier = Modifier
                .border(1.dp, Color.DarkGray, CircleShape)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(club.overall.toString(), color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Default.FavoriteBorder, null, tint = TextGray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, hint: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, tint = TextGray, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(hint, color = TextGray, fontSize = 12.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(AccentGreen),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Default.Close, null, tint = TextGray)
            }
        }
    }
}

@Composable
fun SelectionPanel(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    content: @Composable () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    val borderColor = if (isSelected) AccentGreen.copy(alpha = 0.5f) else Color(0xFF1E293B)
    
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = CardBackground.copy(alpha = alpha),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if (enabled) AccentGreen else TextGray, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(subtitle, color = TextGray, fontSize = 10.sp)
                }
                if (isSelected) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CareerStepper(currentStep: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepItem(1, "Country", currentStep >= 0)
        StepDivider(currentStep > 0)
        StepItem(2, "League", currentStep >= 1)
        StepDivider(currentStep > 1)
        StepItem(3, "Team", currentStep >= 2)
        StepDivider(currentStep > 2)
        StepItem(4, "Ready", currentStep >= 3)
    }
}

@Composable
fun StepItem(step: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(if (isActive) AccentGreen else Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(step.toString(), color = if (isActive) Color.Black else TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, color = if (isActive) AccentGreen else TextGray, fontSize = 8.sp)
    }
}

@Composable
fun StepDivider(isActive: Boolean) {
    Box(
        modifier = Modifier
            .width(30.dp)
            .height(1.dp)
            .padding(horizontal = 4.dp)
            .offset(y = (-6).dp)
            .background(if (isActive) AccentGreen else Color(0xFF1E293B))
    )
}

@Composable
fun AttributeItem(label: String, value: Int) {
    Column {
        Text(label, color = TextGray, fontSize = 8.sp)
        Text(value.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(label, color = TextGray, fontSize = 8.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 10.sp, maxLines = 1)
    }
}
