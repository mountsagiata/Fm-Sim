package com.mountsa.fmsimulation.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.SaveCareerEntity
import com.mountsa.fmsimulation.ui.screens.dashboard.calendar.CalendarHub
import com.mountsa.fmsimulation.ui.screens.dashboard.components.getDateString
import com.mountsa.fmsimulation.ui.screens.dashboard.components.getDayName
import com.mountsa.fmsimulation.ui.screens.dashboard.components.LeagueLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.home.HomeHub
import com.mountsa.fmsimulation.ui.screens.dashboard.home.FinanceDetailHub
import com.mountsa.fmsimulation.ui.screens.dashboard.home.ObjectivesDetailHub
import com.mountsa.fmsimulation.ui.screens.dashboard.fixtures.CompetitionFixturesHub
import com.mountsa.fmsimulation.ui.screens.dashboard.inbox.InboxHub
import com.mountsa.fmsimulation.ui.screens.dashboard.league.LeagueHub
import com.mountsa.fmsimulation.ui.screens.dashboard.myclub.MyClubHub
import com.mountsa.fmsimulation.ui.screens.dashboard.scouting.ScoutingHub
import com.mountsa.fmsimulation.ui.screens.dashboard.settings.SettingsHub
import com.mountsa.fmsimulation.ui.screens.dashboard.shop.ShopHub
import com.mountsa.fmsimulation.ui.screens.dashboard.squad.SquadHub
import com.mountsa.fmsimulation.ui.screens.dashboard.training.TrainingHub
import com.mountsa.fmsimulation.ui.screens.dashboard.transfer.TransferHub
import com.mountsa.fmsimulation.ui.screens.match.*
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.MatchFlow
import com.mountsa.fmsimulation.ui.viewmodel.SquadViewModel
import androidx.compose.foundation.layout.BoxWithConstraints
import com.mountsa.fmsimulation.utils.AudioManager
import java.util.Locale
import kotlinx.coroutines.delay

val FM_GREEN = Color(0xFF00FF5F)
val FM_DARK_BG = Color(0xFF080808)
val FM_CARD_BG = Color(0xFF111111)

private val SIDEBAR_WIDTH = 72.dp
private val ITEM_SIZE_MAX = 46.dp
private val ITEM_SIZE_MIN = 32.dp
private val ICON_SIZE = 22.dp
private val CORNER_RADIUS = 12.dp

/** Number of items in the sidebar (logo + 8 menu entries), used to size items to fit. */
private const val SIDEBAR_SLOT_COUNT = 10

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    squadViewModel: SquadViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf("Home") }
    val club by dashboardViewModel.club.collectAsStateWithLifecycle()
    val career by dashboardViewModel.career.collectAsStateWithLifecycle()
    val matchFlow by dashboardViewModel.matchFlowState.collectAsStateWithLifecycle()
    val matchSession by dashboardViewModel.matchSession.collectAsStateWithLifecycle()
    val isLoading by dashboardViewModel.isLoading.collectAsStateWithLifecycle()
    val loadingMessage by dashboardViewModel.loadingMessage.collectAsStateWithLifecycle()
    val audioManager = dashboardViewModel.audioManager
    val autosaveVisible by dashboardViewModel.autosaveVisible.collectAsStateWithLifecycle()
    val trackAnnouncement by audioManager.trackAnnouncement.collectAsStateWithLifecycle()
    var showTrackAnnouncement by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Background music: plays while on the dashboard, pauses during the live
    // match simulation (crowd ambience takes over then), resumes afterwards.
    DisposableEffect(Unit) {
        audioManager.playBackgroundMusic()
        onDispose { }
    }
    LaunchedEffect(matchFlow) {
        if (matchFlow == MatchFlow.SIMULATION) {
            audioManager.stopBackgroundMusic()
        } else {
            audioManager.playBackgroundMusic()
        }
    }
    LaunchedEffect(trackAnnouncement?.sequence) {
        if (trackAnnouncement != null && matchFlow != MatchFlow.SIMULATION) {
            showTrackAnnouncement = true
            delay(2_800)
            showTrackAnnouncement = false
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    // Compute a sidebar item size that actually fits the available screen
    // height, so nothing overlaps/gets clipped on small screens, and items
    // don't awkwardly float with huge gaps on large screens either.
    val estimatedChromeHeight = 24.dp // top/bottom padding of the sidebar column
    val availableForItems = maxHeight - estimatedChromeHeight
    val rawItemSize = availableForItems / SIDEBAR_SLOT_COUNT
    val itemSize = rawItemSize.coerceIn(ITEM_SIZE_MIN, ITEM_SIZE_MAX)
    val compactWidth = maxWidth < 600.dp
    val sidebarWidth = if (compactWidth) 56.dp else SIDEBAR_WIDTH
    val contentPadding = if (compactWidth) 6.dp else 16.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // --- LEFT SIDEBAR ---
            Column(
                modifier = Modifier
                    .width(sidebarWidth)
                    .fillMaxHeight()
                    .background(Color.Black)
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Competition identity lives in the rail. The club crest belongs
                // beside the club name in the top bar (not the other way around).
                Surface(
                    modifier = Modifier.size(itemSize),
                    shape = RoundedCornerShape(CORNER_RADIUS),
                    color = FM_CARD_BG,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if ((club?.leagueId ?: 0L) > 0L) {
                            LeagueLogo(
                                leagueId = club!!.leagueId,
                                size = (itemSize.value * .7f).dp
                            )
                        } else {
                            Text(com.mountsa.fmsimulation.ui.localization.localized("🛡️"), fontSize = 20.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Menu Items (scrollable so items never overlap/get clipped on short screens)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val menuItems = listOf(
                        Triple("Home", Icons.Default.Home, "Home"),
                        Triple("Squad", Icons.Default.Groups, "Squad"),
                        Triple("League", Icons.Default.TableRows, "League"),
                        Triple("Fixtures", Icons.Default.EventNote, "Fixtures"),
                        Triple("Training", Icons.Default.FitnessCenter, "Training"),
                        Triple("Transfer", Icons.Default.SwapHoriz, "Transfers"),
                        Triple("Scouting", Icons.Default.PersonSearch, "Scouting"),
                        Triple("Club", Icons.Default.Shield, "Club"),
                        Triple("Shop", Icons.Default.ShoppingCart, "Shop")
                    )

                    menuItems.forEach { (id, icon, label) ->
                        val isSelected = selectedTab == id
                        SidebarItem(
                            icon = icon,
                            label = label,
                            isSelected = isSelected,
                            size = itemSize,
                            onClick = {
                                audioManager.playClickSound()
                                selectedTab = id
                            }
                        )
                        Spacer(Modifier.height((itemSize / 4.6f).coerceIn(4.dp, 10.dp)))
                    }
                }
            }

            // --- MAIN CONTENT AREA ---
            Column(modifier = Modifier.weight(1f).background(FM_DARK_BG)) {
                TopBar(
                    club = club,
                    career = career,
                    dashboardViewModel = dashboardViewModel,
                    currentTab = selectedTab,
                    onInboxClick = { selectedTab = "Inbox" },
                    onSettingsClick = { selectedTab = "Settings" },
                    onCalendarClick = { selectedTab = "Calendar" }
                )

                Box(modifier = Modifier.fillMaxSize().padding(horizontal = contentPadding, vertical = 8.dp)) {
                    when (selectedTab) {
                        "Home" -> HomeHub(
                            dashboardViewModel,
                            onNavigateToCalendar = { selectedTab = "Calendar" },
                            onNavigateToLeague = { selectedTab = "League" },
                            onNavigateToTraining = { selectedTab = "Training" },
                            onNavigateToSquad = { selectedTab = "Squad" },
                            onNavigateToInbox = { selectedTab = "Inbox" },
                            onNavigateToFinance = { selectedTab = "Finance" },
                            onNavigateToObjectives = { selectedTab = "Objectives" },
                            onNavigateToFixtures = { selectedTab = "Fixtures" }
                        )
                        "Squad" -> SquadHub(dashboardViewModel, squadViewModel)
                        "League" -> LeagueHub(dashboardViewModel)
                        "Fixtures" -> CompetitionFixturesHub(dashboardViewModel)
                        "Training" -> TrainingHub(dashboardViewModel)
                        "Inbox" -> InboxHub(dashboardViewModel)
                        "Transfer" -> TransferHub(dashboardViewModel)
                        "Scouting" -> ScoutingHub(dashboardViewModel)
                        "Club" -> MyClubHub(dashboardViewModel)
                        "Shop" -> ShopHub(dashboardViewModel)
                        "Settings" -> SettingsHub(dashboardViewModel)
                        "Finance" -> FinanceDetailHub(dashboardViewModel)
                        "Objectives" -> ObjectivesDetailHub(dashboardViewModel)
                    }
                }
            }
        }

        // --- FULL SCREEN CALENDAR OVERLAY ---
        if (selectedTab == "Calendar") {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                CalendarHub(
                    viewModel = dashboardViewModel,
                    onBack = { selectedTab = "Home" }
                )
            }
        }

        if (selectedTab == "Settings") {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                SettingsHub(dashboardViewModel, onBack = { selectedTab = "Home" })
            }
        }

        // --- MATCH FLOW OVERLAY ---
        if (matchFlow != MatchFlow.NONE) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = FM_DARK_BG
            ) {
                when (matchFlow) {
                    MatchFlow.REVEAL -> MatchRevealScreen(dashboardViewModel)
                    MatchFlow.LINEUP -> StartingLineupScreen(dashboardViewModel)
                    MatchFlow.SIMULATION -> MatchSimulationScreen(dashboardViewModel)
                    MatchFlow.RESULT -> MatchResultScreen(dashboardViewModel)
                    MatchFlow.POST -> MatchStageBackground {
                        PostMatchProgress(
                            leagueId = matchSession?.match?.leagueId,
                            message = if (isLoading) loadingMessage else "The team is heading back to the dressing room."
                        )
                    }
                    else -> {}
                }
            }
        }

        AnimatedVisibility(
            visible = showTrackAnnouncement && matchFlow != MatchFlow.SIMULATION,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 72.dp, end = 18.dp),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Surface(
                color = Color(0xEE111418),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FM_GREEN.copy(.35f))
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Album, contentDescription = null, tint = FM_GREEN, modifier = Modifier.size(18.dp))
                    Text(trackAnnouncement?.title.orEmpty(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedVisibility(
            visible = autosaveVisible,
            modifier = Modifier.align(Alignment.TopEnd).padding(18.dp),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Surface(color = Color(0xEE102218), shape = RoundedCornerShape(9.dp)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, null, tint = FM_GREEN, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("CAREER AUTOSAVED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
    } // end BoxWithConstraints
}

@Composable
private fun PostMatchProgress(leagueId: Long?, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                com.mountsa.fmsimulation.ui.localization.localized("POST-MATCH ANALYSIS"),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                leagueId?.takeIf { it > 0L }?.let { LeagueLogo(it, 26.dp) }
                Box(Modifier.widthIn(min = 280.dp, max = 520.dp).height(34.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = message,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                        label = "post_match_update"
                    ) { update ->
                        Text(update, color = Color.LightGray, fontSize = 13.sp, maxLines = 1)
                    }
                    Box(
                        Modifier.align(Alignment.CenterStart).fillMaxHeight().width(42.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Black, Color.Transparent)))
                    )
                    Box(
                        Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(42.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black)))
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(260.dp).height(4.dp).clip(RoundedCornerShape(3.dp)),
                color = FM_GREEN,
                trackColor = Color.White.copy(.08f)
            )
        }
    }
}

@Composable
fun SidebarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    size: androidx.compose.ui.unit.Dp = ITEM_SIZE_MAX,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .requiredSize(size)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            shape = RoundedCornerShape(CORNER_RADIUS),
            color = if (isSelected) FM_GREEN.copy(alpha = 0.12f) else Color.Transparent,
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FM_GREEN) else null
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) FM_GREEN else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size((size.value * 0.48f).dp)
                )
            }
        }
    }
}

@Composable
fun TopBar(
    club: ClubEntity?,
    career: SaveCareerEntity?,
    dashboardViewModel: DashboardViewModel,
    currentTab: String,
    onInboxClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val leagueName by dashboardViewModel.leagueName.collectAsStateWithLifecycle()
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val currentDate = career?.currentDate ?: System.currentTimeMillis()
    val news = remember(uiState.inboxMessages) {
        uiState.inboxMessages.filter { it.subject.isNotBlank() }.take(8)
    }
    var newsIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(news.size) {
        newsIndex = 0
        while (news.size > 1) {
            delay(4_200)
            newsIndex = (newsIndex + 1) % news.size
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
    val availableWidth = maxWidth
    val compact = availableWidth < 600.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .padding(horizontal = if (compact) 8.dp else 24.dp, vertical = if (compact) 6.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f).widthIn(min = 80.dp), verticalAlignment = Alignment.CenterVertically) {
            club?.id?.takeIf { it > 0L }?.let { clubId ->
                ClubLogo(clubId = clubId, size = if (compact) 24.dp else 32.dp)
                Spacer(Modifier.width(if (compact) 6.dp else 9.dp))
            }
        Column {
            Text(
                text = club?.name ?: "Manchester United",
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (compact) 14.sp else 20.sp,
                color = Color.White
            )
            if (!compact) Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = leagueName.ifEmpty { "Premier League" },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(com.mountsa.fmsimulation.ui.localization.localized("  |  "), fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = club?.shortName ?: "MUN",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        }

        if (!compact && availableWidth >= 840.dp) {
            Box(
                Modifier.weight(.8f).padding(horizontal = 16.dp).height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = news.getOrNull(newsIndex)?.subject ?: "GLOBAL FOOTBALL NEWS",
                    transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(220)) },
                    label = "global_news_ticker"
                ) { headline ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(5.dp).background(FM_GREEN, androidx.compose.foundation.shape.CircleShape))
                        Text(
                            headline.uppercase(),
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FM_CARD_BG,
                modifier = Modifier.height(if (compact) 34.dp else 40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = if (compact) 8.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = FM_GREEN
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%,d", (club?.budget ?: 0L) / 1_000_000)}M",
                        fontSize = if (compact) 12.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            val isInboxSelected = currentTab == "Inbox"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isInboxSelected) FM_GREEN.copy(alpha = 0.15f) else FM_CARD_BG,
                border = if (isInboxSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FM_GREEN) else null,
                modifier = Modifier
                    .size(if (compact) 34.dp else 40.dp)
                    .clickable { onInboxClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Inbox",
                        modifier = Modifier.size(18.dp),
                        tint = if (isInboxSelected) FM_GREEN else Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.clickable { onCalendarClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isCalendarSelected = currentTab == "Calendar"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCalendarSelected) FM_GREEN.copy(alpha = 0.15f) else FM_CARD_BG,
                    border = if (isCalendarSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FM_GREEN) else null,
                    modifier = Modifier.size(if (compact) 34.dp else 40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            modifier = Modifier.size(18.dp),
                            tint = if (isCalendarSelected) FM_GREEN else Color.White
                        )
                    }
                }

                if (!compact) Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = getDayName(currentDate),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = getDateString(currentDate),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 12.sp
                    )
                }
            }

            val isSettingsSelected = currentTab == "Settings"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSettingsSelected) FM_GREEN.copy(alpha = 0.15f) else FM_CARD_BG,
                border = if (isSettingsSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FM_GREEN) else null,
                modifier = Modifier
                    .size(if (compact) 34.dp else 40.dp)
                    .clickable { onSettingsClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(18.dp),
                        tint = if (isSettingsSelected) FM_GREEN else Color.White
                    )
                }
            }
        }
    }
    }
}
