package com.mountsa.fmsimulation.ui.screens.dashboard.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.data.local.entities.CalendarEventEntity
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

data class DayGridModel(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val timestamp: Long = -1L,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isPassed: Boolean = false,
    val match: MatchEntity? = null,
    val events: List<CalendarEventEntity> = emptyList()
)

@Composable
fun CalendarHub(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val leagueName by viewModel.leagueName.collectAsStateWithLifecycle()

    val seasonStart = remember {
        Calendar.getInstance().apply {
            set(2025, Calendar.JULY, 1)
        }
    }

    val seasonMonths = remember {
        buildList {
            val temp = seasonStart.clone() as Calendar
            repeat(60) { // 5 years
                add(temp.clone() as Calendar)
                temp.add(Calendar.MONTH, 1)
            }
        }
    }

    var currentMonth by remember {
        mutableStateOf(Calendar.getInstance().apply {
            if (uiState.currentDate > 0) {
                timeInMillis = uiState.currentDate
                set(Calendar.DAY_OF_MONTH, 1)
            }
        })
    }

    var selectedDate by remember { mutableLongStateOf(if (uiState.currentDate > 0) uiState.currentDate else System.currentTimeMillis()) }

    LaunchedEffect(uiState.currentDate) {
        if (uiState.currentDate > 0L) {
            selectedDate = uiState.currentDate
            currentMonth = Calendar.getInstance().apply {
                timeInMillis = uiState.currentDate
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    val matches = uiState.allMatches
    val events = uiState.calendarEvents
    val currentDate = uiState.currentDate
    val clubId = uiState.club?.id ?: -1L
    val allClubs = uiState.allClubs

    val selectedMonthIndex = seasonMonths.indexOfFirst {
        it.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
        it.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
    }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C10))
            .padding(8.dp)
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape).size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "CALENDAR",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "SEASON 2025/26",
                    color = FM_GREEN,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Month Slider
            Box(modifier = Modifier.weight(1f).widthIn(min = 220.dp, max = 520.dp).height(40.dp)) {
                val listState = rememberLazyListState()
                LaunchedEffect(selectedMonthIndex) {
                    listState.animateScrollToItem(index = maxOf(0, selectedMonthIndex - 2))
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(seasonMonths.size) { index ->
                        val isSelected = index == selectedMonthIndex
                        val monthCal = seasonMonths[index]
                        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(monthCal.time)

                        Text(
                            text = monthName.uppercase(),
                            color = when {
                                isSelected -> Color.White
                                kotlin.math.abs(index - selectedMonthIndex) >= 4 -> Color.White.copy(alpha = 0.12f)
                                kotlin.math.abs(index - selectedMonthIndex) == 3 -> Color.White.copy(alpha = 0.28f)
                                kotlin.math.abs(index - selectedMonthIndex) == 2 -> Color.White.copy(alpha = 0.45f)
                                else -> Color.White.copy(alpha = 0.7f)
                            },
                            fontSize = if (isSelected) 15.sp else 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clickable {
                                    currentMonth = seasonMonths[index].clone() as Calendar
                                    selectedDate = (currentMonth.clone() as Calendar).apply {
                                        set(Calendar.DAY_OF_MONTH, 1)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(.15f))

            // User Club Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.widthIn(min = 150.dp, max = 230.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(FM_GREEN, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = uiState.club?.name?.uppercase() ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                ClubLogo(clubId = clubId, size = 36.dp)
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxWidth < 760.dp || maxHeight > maxWidth
            val calendarContent: @Composable (Modifier) -> Unit = { modifier ->
                Box(modifier) {
                    CalendarGrid(
                        calendar = currentMonth, matches = matches, events = events,
                        today = currentDate, selectedDate = selectedDate, clubId = clubId,
                        allClubs = allClubs, leagueName = leagueName,
                        onDateClick = { selectedDate = it }
                    )
                }
            }
            val detailContent: @Composable (Modifier) -> Unit = { modifier ->
                Surface(
                    modifier = modifier,
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    SelectedDatePanel(
                        selectedDate = selectedDate, matches = matches, events = events,
                        clubId = clubId, allClubs = allClubs, leagueName = leagueName,
                        currentDate = currentDate
                    )
                }
            }

            if (compact) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    calendarContent(Modifier.fillMaxWidth().weight(1.45f))
                    detailContent(Modifier.fillMaxWidth().weight(.8f))
                }
            } else {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    calendarContent(Modifier.weight(2.2f).fillMaxHeight())
                    detailContent(Modifier.weight(.8f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    calendar: Calendar,
    matches: List<MatchEntity>,
    events: List<CalendarEventEntity>,
    today: Long,
    selectedDate: Long,
    clubId: Long,
    allClubs: List<ClubEntity>,
    leagueName: String,
    onDateClick: (Long) -> Unit
) {
    val gridData = remember(calendar, matches, events, today, selectedDate) {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = tempCal.get(Calendar.DAY_OF_WEEK)
        val prevCal = calendar.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val prevMonthDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = mutableListOf<DayGridModel>()
        val offset = if (firstDay == Calendar.SUNDAY) 6 else firstDay - 2

        for (i in offset - 1 downTo 0) {
            days.add(DayGridModel(dayNumber = prevMonthDays - i, isCurrentMonth = false))
        }

        for (i in 1..daysInMonth) {
            val cellCal = calendar.clone() as Calendar
            cellCal.set(Calendar.DAY_OF_MONTH, i)
            cellCal.set(Calendar.HOUR_OF_DAY, 0)
            cellCal.set(Calendar.MINUTE, 0)
            cellCal.set(Calendar.SECOND, 0)
            cellCal.set(Calendar.MILLISECOND, 0)
            val timestamp = cellCal.timeInMillis

            val dayMatch = matches.find { isSameDayFast(cellCal, it.matchDate) }
            val storedEvents = events.filter { isSameDayFast(cellCal, it.eventDate) }
            val dayEvents = storedEvents.ifEmpty {
                plannedEventsForDate(clubId, timestamp, matches)
            }

            days.add(
                DayGridModel(
                    dayNumber = i,
                    isCurrentMonth = true,
                    timestamp = timestamp,
                    isSelected = isSameDayFast(cellCal, selectedDate),
                    match = dayMatch,
                    events = dayEvents
                )
            )
        }

        var nextMonthDay = 1
        while (days.size < 42) {
            days.add(DayGridModel(dayNumber = nextMonthDay, isCurrentMonth = false))
            nextMonthDay++
        }
        days
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(gridData) { model ->
            DayCell(
                model = model,
                clubId = clubId,
                allClubs = allClubs,
                onClick = {
                    if (model.timestamp != -1L) onDateClick(model.timestamp)
                }
            )
        }
    }
}

@Composable
fun DayCell(
    model: DayGridModel,
    clubId: Long,
    allClubs: List<ClubEntity>,
    onClick: () -> Unit
) {
    val isMatchDay = model.match != null
    val isTraining = model.events.any { it.type == "TRAINING" }

    val infiniteTransition = rememberInfiniteTransition(label = "SelectedOutline")
    val outlineAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "Outline"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .alpha(if (model.isCurrentMonth) 1f else 0.28f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF05090D))
            .border(
                width = if (model.isSelected) 2.dp else 1.dp,
                color = if (model.isSelected) FM_GREEN.copy(alpha = outlineAlpha) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = model.isCurrentMonth) { onClick() }
            .padding(6.dp)
    ) {
        // DATE
        Text(
            text = model.dayNumber.toString(),
            color = if (model.isSelected) FM_GREEN else Color.White.copy(alpha = 0.82f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )

        if (model.isCurrentMonth) {
            if (isMatchDay) {
                val match = model.match!!
                val isPlayed = match.isPlayed

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ClubLogo(clubId = match.homeClubId, size = 18.dp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPlayed) "${match.homeScore} - ${match.awayScore}" else "VS",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.width(4.dp))
                        ClubLogo(clubId = match.awayClubId, size = 18.dp)
                    }

                    if (isPlayed) {
                        val isHome = match.homeClubId == clubId
                        val userScore = if (isHome) match.homeScore else match.awayScore
                        val oppScore = if (isHome) match.awayScore else match.homeScore
                        val result = when {
                            userScore > oppScore -> "W"
                            userScore < oppScore -> "L"
                            else -> "D"
                        }
                        val resultColor = when(result) {
                            "W" -> FM_GREEN
                            "L" -> Color.Red
                            else -> Color.Gray
                        }
                        Text(
                            text = result,
                            color = resultColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else if (isTraining) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.Cyan.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp).align(Alignment.BottomEnd)
                )
            }

            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (model.match != null) Icon(Icons.Default.EmojiEvents, null, tint = FM_GREEN, modifier = Modifier.size(11.dp))
                model.events.take(3).forEach { event ->
                    val icon = when (event.type) {
                        "TRAINING", "RECOVERY", "REST" -> Icons.Default.FitnessCenter
                        "TRANSFER" -> Icons.Default.SwapHoriz
                        "MEDIA", "BOARD" -> Icons.Default.Campaign
                        else -> Icons.Default.CalendarToday
                    }
                    val tint = when (event.type) {
                        "TRAINING" -> Color.Cyan
                        "RECOVERY", "REST" -> Color(0xFF80CBC4)
                        "TRANSFER" -> Color(0xFFFFB74D)
                        "MEDIA" -> Color(0xFFE040FB)
                        else -> Color.White.copy(.75f)
                    }
                    Icon(icon, event.title, tint = tint, modifier = Modifier.size(11.dp))
                }
            }
        }
    }
}

@Composable
fun SelectedDatePanel(
    selectedDate: Long,
    matches: List<MatchEntity>,
    events: List<CalendarEventEntity>,
    clubId: Long,
    allClubs: List<ClubEntity>,
    leagueName: String,
    currentDate: Long
) {
    val cal = remember(selectedDate) { Calendar.getInstance().apply { timeInMillis = selectedDate } }
    val dayMatch = matches.find { isSameDayFast(cal, it.matchDate) }
    val storedEvents = events.filter { isSameDayFast(cal, it.eventDate) }
    val dayEvents = storedEvents.ifEmpty {
        plannedEventsForDate(clubId, selectedDate, matches)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time).uppercase(),
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = SimpleDateFormat("dd MMMM", Locale.getDefault()).format(cal.time).uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(24.dp))

        if (dayMatch != null) {
            val isHome = dayMatch.homeClubId == clubId
            val opponentId = if (isHome) dayMatch.awayClubId else dayMatch.homeClubId
            val opponent = allClubs.find { it.id == opponentId }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(color = FM_GREEN.copy(.14f), shape = RoundedCornerShape(20.dp)) {
                    Text(
                        text = when { dayMatch.cupId != null -> "CUP"; dayMatch.leagueId != null -> leagueName.uppercase(); else -> "FRIENDLY" },
                        color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = opponent?.name?.uppercase() ?: "UNKNOWN OPPONENT",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                ClubLogo(clubId = opponentId, size = 120.dp)

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (isHome) "HOME" else "AWAY",
                    color = FM_GREEN,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (dayMatch.isPlayed) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${dayMatch.homeScore} - ${dayMatch.awayScore}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dayEvents) { event ->
                    EventCard(event)
                }

                if (dayEvents.isEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(com.mountsa.fmsimulation.ui.localization.localized("NO EVENTS"), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(18.dp))
                            Text("UPCOMING", color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            matches.asSequence().filter { it.matchDate > selectedDate }.sortedBy { it.matchDate }.take(3).forEach { future ->
                                val opponentId = if (future.homeClubId == clubId) future.awayClubId else future.homeClubId
                                val opponent = allClubs.firstOrNull { it.id == opponentId }
                                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(future.matchDate)), color = Color.Gray, fontSize = 10.sp)
                                    Text(opponent?.shortName ?: "TBD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(if (future.cupId != null) "CUP" else if (future.leagueId != null) "LEAGUE" else "FRIENDLY", color = FM_GREEN, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: CalendarEventEntity) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (event.type) {
                "TRAINING", "RECOVERY", "REST" -> Icons.Default.FitnessCenter
                "TRANSFER" -> Icons.Default.SwapHoriz
                "MEDIA", "BOARD" -> Icons.Default.Campaign
                else -> Icons.Default.CalendarToday
            }
            val color = when (event.type) {
                "TRAINING" -> Color.Cyan
                "RECOVERY", "REST" -> Color(0xFF80CBC4)
                "TRANSFER" -> Color(0xFFFFB74D)
                "MEDIA", "BOARD" -> Color(0xFFE040FB)
                else -> Color.White
            }

            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(event.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (event.description.isNotEmpty()) {
                    Text(event.description, color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}

private fun plannedEventsForDate(
    clubId: Long,
    timestamp: Long,
    matches: List<MatchEntity>
): List<CalendarEventEntity> {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    if (matches.any { isSameDayFast(calendar, it.matchDate) }) return emptyList()
    val previousDay = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
    val nextDay = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
    val playedYesterday = matches.any { isSameDayFast(previousDay, it.matchDate) }
    val matchTomorrow = matches.any { isSameDayFast(nextDay, it.matchDate) }
    val event = when {
        playedYesterday -> CalendarEventEntity(
            clubId = clubId,
            eventDate = timestamp,
            type = "RECOVERY",
            title = "Recovery",
            description = "Regeneration and medical review after matchday."
        )
        matchTomorrow -> CalendarEventEntity(
            clubId = clubId,
            eventDate = timestamp,
            type = "MEDIA",
            title = "Match preparation",
            description = "Tactical briefing and pre-match media duties."
        )
        calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> CalendarEventEntity(
            clubId = clubId,
            eventDate = timestamp,
            type = "REST",
            title = "Rest day",
            description = "Squad recovery and individual treatment."
        )
        else -> CalendarEventEntity(
            clubId = clubId,
            eventDate = timestamp,
            type = "TRAINING",
            title = "Training",
            description = "Scheduled technical, physical and tactical work."
        )
    }
    return listOf(event)
}

private fun isSameDayFast(cal1: Calendar, timestamp: Long): Boolean {
    if (timestamp <= 0) return false
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
