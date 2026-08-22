package com.mountsa.fmsimulation.ui.screens.dashboard.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import com.mountsa.fmsimulation.ui.screens.dashboard.home.cards.NextMatchCard
import com.mountsa.fmsimulation.ui.screens.dashboard.home.cards.TeamOverviewCard
import com.mountsa.fmsimulation.ui.screens.dashboard.home.cards.StandingCard
import com.mountsa.fmsimulation.ui.screens.dashboard.home.cards.FinanceCard
import com.mountsa.fmsimulation.ui.screens.dashboard.home.cards.NewsCard
import com.mountsa.fmsimulation.ui.screens.dashboard.home.cards.ObjectiveCard
import com.mountsa.fmsimulation.ui.screens.dashboard.home.sections.UpcomingFixturesSection
import com.mountsa.fmsimulation.ui.screens.dashboard.home.sections.LeagueStatsSection
import java.util.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun HomeHub(
    viewModel: DashboardViewModel,
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToLeague: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToSquad: () -> Unit = {},
    onNavigateToInbox: () -> Unit = {}
) {
    var selectedCardIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 900.dp || maxHeight < 520.dp
        if (compact) {
            Column(
                Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth().heightIn(min = 150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NextMatchCard(Modifier.weight(1f), uiState.nextMatch, uiState.isLoading) { viewModel.onContinueClick() }
                    TeamOverviewCard(Modifier.weight(1f).clickable { onNavigateToSquad() }, uiState.club)
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 135.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StandingCard(Modifier.weight(1f).clickable { onNavigateToLeague() }, uiState.standing, uiState.club)
                    FinanceCard(Modifier.weight(1f), uiState.club)
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 135.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NewsCard(Modifier.weight(1f).clickable { onNavigateToInbox() }, uiState.inboxMessages)
                    ObjectiveCard(Modifier.weight(1f), uiState.objectives)
                }
                UpcomingFixturesSection(Modifier.fillMaxWidth().heightIn(min = 150.dp), uiState.fixtures, onNavigateToCalendar)
                LeagueStatsSection(Modifier.fillMaxWidth().heightIn(min = 140.dp), uiState.topScorer, uiState.topAssister, uiState.bestPlayer)
            }
        } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ROW 1
            Row(modifier = Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                // 1. Next Match Today
                val isNextMatchSelected = selectedCardIndex == 0

                NextMatchCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isNextMatchSelected) 1.5.dp else 0.dp,
                            color = if (isNextMatchSelected) Color.LightGray else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCardIndex = 0 },
                    nextMatch = uiState.nextMatch,
                    isLoading = uiState.isLoading,
                    onContinue = {
                        viewModel.onContinueClick()
                    }
                )

                // 2. Team Overview
                val isTeamOverviewSelected = selectedCardIndex == 1

                TeamOverviewCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isTeamOverviewSelected) 1.5.dp else 0.dp,
                            color = if (isTeamOverviewSelected) Color.LightGray else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedCardIndex = 1
                            onNavigateToSquad()
                        },
                    club = uiState.club
                )

                // 3. Standing
                val isStandingSelected = selectedCardIndex == 2

                StandingCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isStandingSelected) 1.5.dp else 0.dp,
                            color = if (isStandingSelected) Color.LightGray else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedCardIndex = 2
                            onNavigateToLeague()
                        },
                    standing = uiState.standing,
                    club = uiState.club
                )
            }


            // ROW 2
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // 4. Finance
                val isFinanceSelected = selectedCardIndex == 3

                FinanceCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isFinanceSelected) 1.5.dp else 0.dp,
                            color = if (isFinanceSelected) Color.LightGray else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCardIndex = 3 },
                    club = uiState.club
                )

                // 5. News
                val isNewsSelected = selectedCardIndex == 4

                NewsCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isNewsSelected) 1.5.dp else 0.dp,
                            color = if (isNewsSelected) Color.LightGray else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedCardIndex = 4
                            onNavigateToInbox()
                        },
                    messages = uiState.inboxMessages
                )

                // 6. Objective
                val isObjectiveSelected = selectedCardIndex == 5

                ObjectiveCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isObjectiveSelected) 1.5.dp else 0.dp,
                            color = if (isObjectiveSelected) Color.LightGray else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCardIndex = 5 },
                    objectives = uiState.objectives
                )
            }

            // ROW 3
            Row(modifier = Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                // 7. Upcoming Fixtures
                UpcomingFixturesSection(
                    modifier = Modifier.weight(4.1f),
                    fixtures = uiState.fixtures,
                    onNavigateToCalendar = onNavigateToCalendar
                )

                // 8. Top Player
                LeagueStatsSection(
                    modifier = Modifier.weight(2f),
                    topScorer = uiState.topScorer,
                    topAssister = uiState.topAssister,
                    bestPlayer = uiState.bestPlayer
                )
            }
        }
        }
        }

        // Loading Overlay
        if (uiState.isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = FM_GREEN,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = uiState.loadingMessage,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = com.mountsa.fmsimulation.ui.localization.localized("Simulating football world..."),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatRowGreen(label: String, value: Int, isBold: Boolean = false) {
    val clampedValue = value.coerceIn(0, 99)
    val progress = clampedValue / 99f
    val progressColor = when {
        progress < 0.5f -> lerp(Color.Red, Color(0xFFFFA500), progress * 2)
        else -> lerp(Color(0xFFFFA500), FM_GREEN, (progress - 0.5f) * 2)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) Color.White else Color.Gray,
            modifier = Modifier.width(32.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(progressColor, shape = RoundedCornerShape(3.dp))
            )
        }
        Text(
            text = value.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = FM_GREEN,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun FixtureCard(date: Long, opponent: String, opponentClubId: Long) {
    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: ""

    Surface(
        color = Color(0xFF151515),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.width(160.dp).fillMaxHeight(0.95f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(day.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(month, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FM_GREEN)
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(com.mountsa.fmsimulation.ui.localization.localized("League Match"), fontSize = 8.sp, color = Color.Gray)
                Text(com.mountsa.fmsimulation.ui.localization.localized("vs $opponent"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.weight(1f))
            ClubLogo(clubId = opponentClubId, size = 24.dp)
        }
    }
}

@Composable
fun TopPlayerStat(category: String, name: String, value: String, avatarAsset: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 1.dp)
    ) {
        Text(category, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Box {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = Color.DarkGray
            ) {
                if (!avatarAsset.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/$avatarAsset")
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("👤"), modifier = Modifier.wrapContentSize(), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FM_GREEN)
        }
    }
}
