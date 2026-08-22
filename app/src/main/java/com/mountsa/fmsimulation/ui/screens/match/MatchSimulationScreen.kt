package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mountsa.fmsimulation.core.match.event.EventType
import com.mountsa.fmsimulation.core.match.event.MatchEvent
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.domain.models.Formations
import com.mountsa.fmsimulation.ui.screens.dashboard.squad.TacticsPitch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MatchSimulationScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.matchSession ?: return
    val match = session.match

    val events = remember(match.matchEvents) {
        try {
            Gson().fromJson<List<MatchEvent>>(
                match.matchEvents,
                object : TypeToken<List<MatchEvent>>() {}.type
            ) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    var currentMinute by remember { mutableIntStateOf(0) }
    var halfTimePaused by remember { mutableStateOf(false) }
    var autoPaused by remember { mutableStateOf(false) }
    var speed by remember { mutableIntStateOf(2) }
    var rightTab by remember { mutableIntStateOf(0) }
    var mentality by remember { mutableStateOf("MID") }
    var showSquad by remember { mutableStateOf(false) }
    var selectedMatchStarter by remember { mutableStateOf<Long?>(null) }
    val userIsHome = uiState.club?.id == match.homeClubId
    val matchLineup = if (userIsHome) session.homeLineup else session.awayLineup
    val matchBench = if (userIsHome) session.homeBench else session.awayBench
    val listState = rememberLazyListState()

    val audioManager = viewModel.audioManager
    DisposableEffect(Unit) {
        audioManager.playCrowdAmbience()
        onDispose { audioManager.stopCrowdAmbience() }
    }

    LaunchedEffect(Unit) {
        while (currentMinute < 90) {
            if (currentMinute == 45) {
                halfTimePaused = true
                while (halfTimePaused) delay(100)
            }
            while (autoPaused) delay(100)
            delay(800L / speed)
            currentMinute += 1
        }
    }

    val visibleEvents = events.filter { it.minute <= currentMinute }
    val latestEvent = visibleEvents.lastOrNull()
    val latestCommentary = latestEvent?.commentary?.takeIf { it.isNotBlank() }
        ?: latestEvent?.let(::fallbackCommentary)
        ?: "The teams are ready. Live commentary will appear here."

    val currentHomeScore = visibleEvents.lastOrNull()?.scoreHome ?: 0
    val currentAwayScore = visibleEvents.lastOrNull()?.scoreAway ?: 0

    LaunchedEffect(visibleEvents.size) {
        if (visibleEvents.isNotEmpty()) {
            listState.animateScrollToItem(visibleEvents.size - 1)
            val event = visibleEvents.last()
            if (event.minute == currentMinute && event.type in setOf(EventType.GOAL, EventType.RED_CARD, EventType.INJURY, EventType.PENALTY_GOAL)) {
                autoPaused = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(FM_DARK_BG).padding(12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClubLogo(clubId = match.homeClubId, size = 22.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(session.homeShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(
                    color = FM_GREEN.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(session.competitionName.uppercase(), color = FM_GREEN, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(session.awayShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    ClubLogo(clubId = match.awayClubId, size = 22.dp)
                }
            }

            // SCORE & TIME dengan animasi
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                AnimatedContent(
                    targetState = currentHomeScore,
                    transitionSpec = {
                        fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -it / 2 }) togetherWith
                        fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
                    }
                ) { score ->
                    Text(score.toString(), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(if (currentMinute >= 90) "FT" else "$currentMinute'", color = FM_GREEN, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(if (currentMinute >= 90) "FULL TIME" else "MENIT", color = Color.Gray, fontSize = 7.sp, letterSpacing = 1.sp)
                }
                AnimatedContent(
                    targetState = currentAwayScore,
                    transitionSpec = {
                        fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -it / 2 }) togetherWith
                        fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
                    }
                ) { score ->
                    Text(score.toString(), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                }
            }

            // PROGRESS BAR
            Box(modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.CenterHorizontally).height(4.dp).clip(RoundedCornerShape(2.dp))) {
                LinearProgressIndicator(
                    progress = { currentMinute / 90f },
                    modifier = Modifier.fillMaxSize(),
                    color = FM_GREEN,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(visible = halfTimePaused) {
                Surface(
                    color = Color(0xFF17231B),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(com.mountsa.fmsimulation.ui.localization.localized("HALF-TIME"), color = FM_GREEN, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text(com.mountsa.fmsimulation.ui.localization.localized("Review events, statistics and make tactical changes."), color = Color.Gray, fontSize = 10.sp)
                        }
                        Button(onClick = { halfTimePaused = false }, colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN)) {
                            Text(com.mountsa.fmsimulation.ui.localization.localized("START SECOND HALF"), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1.25f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Surface(Modifier.weight(1f).fillMaxWidth(), color = Color(0xFF123817), shape = RoundedCornerShape(10.dp)) {
                        LiveTacticalPitch(
                            homeLineup = session.homeLineup,
                            awayLineup = session.awayLineup,
                            homeClubId = match.homeClubId,
                            currentMinute = currentMinute,
                            latestEvent = latestEvent,
                            speed = speed,
                            isPaused = halfTimePaused || autoPaused || currentMinute >= 90,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(Modifier.fillMaxWidth().background(Color.White.copy(.035f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text("ATTACK MOMENTUM", color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Row(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(5.dp))) {
                            Box(Modifier.weight((currentMinute % 60 + 20).toFloat()).fillMaxHeight().background(FM_GREEN))
                            Box(Modifier.weight((80 - currentMinute % 60).toFloat()).fillMaxHeight().background(Color(0xFFFF5252)))
                        }
                    }
                }

                Column(Modifier.weight(1f).fillMaxHeight()) {
                    TabRow(selectedTabIndex = rightTab, containerColor = Color.Transparent) {
                        listOf("LIVE EVENT", "COMMENTARY", "STATS").forEachIndexed { index, title ->
                            Tab(rightTab == index, { rightTab = index }, text = { Text(title, fontSize = 9.sp) })
                        }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth().background(Color.White.copy(.03f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        when (rightTab) {
                            0 -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) { items(visibleEvents) { event -> EventText(event.minute, event.type.name.replace("_", " "), event.type, event.playerName) } }
                            1 -> Text(latestCommentary, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                            else -> Column { val p = currentMinute / 90f; StatRowModern("Shots", (match.shotsHome*p).toInt(), (match.shotsAway*p).toInt()); StatRowModern("Target", (match.shotsOnTargetHome*p).toInt(), (match.shotsOnTargetAway*p).toInt()); StatRowModern("Poss %", match.possessionHome, match.possessionAway); StatRowModern("Corners", (match.cornersHome*p).toInt(), (match.cornersAway*p).toInt()) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("DEF", "MID", "ATT").forEach { option -> FilterChip(mentality == option, { mentality = option }, { Text(option, fontSize = 9.sp) }, modifier = Modifier.weight(1f)) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf(2,4,8).forEach { value -> FilterChip(speed == value, { speed = value }, { Text("${value}x", fontSize = 9.sp) }) }
                        Button(onClick = { showSquad = true }, modifier = Modifier.weight(1f).height(34.dp), contentPadding = PaddingValues(4.dp), colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN)) { Text("SQUAD / TACTICS", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            if (autoPaused) Row(Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.End) {
                Button(onClick = { autoPaused = false }, colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN), modifier = Modifier.height(34.dp)) { Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold) }
            }

            // FOOTER BUTTON
            if (currentMinute >= 90) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it })
                ) {
                    Button(
                        onClick = { viewModel.nextMatchFlowStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(com.mountsa.fmsimulation.ui.localization.localized("VIEW RESULT"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
    if (showSquad) Dialog(onDismissRequest = { showSquad = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.92f).fillMaxHeight(.9f), color = Color(0xFF0B0D0F), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SQUAD & TACTICS", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    TextButton(onClick = { showSquad = false }) { Text("CLOSE", color = Color.Gray) }
                }
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1.2f).fillMaxHeight()) {
                        TacticsPitch(
                            formation = Formations.DEFAULT_FORMATIONS.first(),
                            startingXI = matchLineup.map { it as com.mountsa.fmsimulation.data.local.entities.PlayerEntity? },
                            onSlotClick = { index -> selectedMatchStarter = matchLineup.getOrNull(index)?.id }
                        )
                    }
                    Column(Modifier.weight(.8f).fillMaxHeight()) {
                        Text("SUBSTITUTES", color = FM_GREEN, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        LazyColumn(Modifier.weight(1f)) {
                            items(matchBench) { player ->
                                Row(
                                    Modifier.fillMaxWidth().clickable(enabled = selectedMatchStarter != null) {
                                        selectedMatchStarter?.let { viewModel.swapMatchPlayer(it, player.id) }
                                        selectedMatchStarter = null
                                    }.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(player.shortName, color = Color.White, fontSize = 10.sp)
                                    Text("${player.position}  ${player.fitness}%", color = FM_GREEN, fontSize = 9.sp)
                                }
                                HorizontalDivider(color = Color.White.copy(.06f))
                            }
                        }
                        Text("INSTRUCTIONS", color = FM_GREEN, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("DEF", "MID", "ATT").forEach { item -> FilterChip(mentality == item, { mentality = item }, { Text(item, fontSize = 8.sp) }, modifier = Modifier.weight(1f)) }
                        }
                        Button(onClick = { showSquad = false; autoPaused = false }, Modifier.fillMaxWidth().height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN)) { Text("APPLY & RESUME", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

private data class PitchEntity(
    val playerId: Long,
    val isHome: Boolean,
    val position: String,
    val baseX: Float,
    val baseY: Float,
    val motionSeed: Float
)

private data class RenderedPitchEntity(
    val entity: PitchEntity,
    val x: Float,
    val y: Float
)

/**
 * Horizontal broadcast pitch backed by the actual saved match lineups.
 *
 * Coordinates are normalized so the same pitch scales cleanly on compact phones,
 * foldables and tablets. HOME attacks left-to-right and AWAY right-to-left.
 */
@Composable
private fun LiveTacticalPitch(
    homeLineup: List<PlayerEntity>,
    awayLineup: List<PlayerEntity>,
    homeClubId: Long,
    currentMinute: Int,
    latestEvent: MatchEvent?,
    speed: Int,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val homeEntities = remember(homeLineup) { buildPitchEntities(homeLineup, isHome = true) }
    val awayEntities = remember(awayLineup) { buildPitchEntities(awayLineup, isHome = false) }
    val entities = remember(homeEntities, awayEntities) { homeEntities + awayEntities }

    val motionPhase = remember { Animatable(0f) }
    val safeSpeed = speed.coerceIn(1, 8)
    LaunchedEffect(safeSpeed, isPaused) {
        if (isPaused) return@LaunchedEffect
        val fullTurn = (PI * 2.0).toFloat()
        while (true) {
            motionPhase.animateTo(
                targetValue = motionPhase.value + fullTurn,
                animationSpec = tween(
                    durationMillis = (5_200 / safeSpeed).coerceAtLeast(650),
                    easing = LinearEasing
                )
            )
            motionPhase.snapTo(motionPhase.value % fullTurn)
        }
    }

    val attackingEvents = remember {
        setOf(
            EventType.SHOT,
            EventType.SHOT_ON_TARGET,
            EventType.GOAL,
            EventType.PENALTY,
            EventType.PENALTY_GOAL,
            EventType.CORNER,
            EventType.FREEKICK,
            EventType.COUNTER_ATTACK,
            EventType.GREAT_CHANCE,
            EventType.CROSS
        )
    }
    val eventIsHome = when (latestEvent?.teamId) {
        homeClubId -> true
        null -> currentMinute % 4 < 2
        else -> false
    }
    val territoryTarget = when {
        latestEvent?.type?.let { it in attackingEvents } == true && eventIsHome -> 0.075f
        latestEvent?.type?.let { it in attackingEvents } == true -> -0.075f
        eventIsHome -> 0.025f
        else -> -0.025f
    }
    val territoryBias by animateFloatAsState(
        targetValue = territoryTarget,
        animationSpec = tween((650 / safeSpeed).coerceAtLeast(140), easing = FastOutSlowInEasing)
    )

    Canvas(modifier = modifier.fillMaxSize().padding(8.dp)) {
        val pitchBackground = Color(0xFF0E351B)
        val pitchStripe = Color(0xFF174522)
        val pitchLine = Color.White.copy(alpha = 0.52f)
        val awayColor = Color(0xFFE34C4C)
        val ballColor = Color(0xFFFFE84D)
        val highlightColor = Color(0xFFFFC857)

        drawRect(pitchBackground)
        repeat(10) { stripe ->
            if (stripe % 2 == 0) {
                val stripeWidth = size.width / 10f
                drawRect(
                    color = pitchStripe.copy(alpha = 0.42f),
                    topLeft = Offset(stripe * stripeWidth, 0f),
                    size = Size(stripeWidth, size.height)
                )
            }
        }

        val insetX = 8.dp.toPx()
        val insetY = 7.dp.toPx()
        val fieldWidth = (size.width - insetX * 2f).coerceAtLeast(1f)
        val fieldHeight = (size.height - insetY * 2f).coerceAtLeast(1f)
        val lineWidth = 1.2.dp.toPx()

        fun point(x: Float, y: Float) = Offset(
            x = insetX + x.coerceIn(0f, 1f) * fieldWidth,
            y = insetY + y.coerceIn(0f, 1f) * fieldHeight
        )

        // Touchline, halfway line and centre markings.
        drawRect(
            color = pitchLine,
            topLeft = Offset(insetX, insetY),
            size = Size(fieldWidth, fieldHeight),
            style = Stroke(lineWidth)
        )
        drawLine(pitchLine, point(0.5f, 0f), point(0.5f, 1f), lineWidth)
        drawCircle(
            color = pitchLine,
            radius = minOf(fieldWidth, fieldHeight) * 0.145f,
            center = point(0.5f, 0.5f),
            style = Stroke(lineWidth)
        )
        drawCircle(pitchLine, radius = 1.4.dp.toPx(), center = point(0.5f, 0.5f))

        // Penalty areas, six-yard boxes, spots and goals on both ends.
        val penaltyWidth = fieldWidth * 0.16f
        val penaltyHeight = fieldHeight * 0.54f
        val penaltyTop = insetY + (fieldHeight - penaltyHeight) / 2f
        val sixYardWidth = fieldWidth * 0.065f
        val sixYardHeight = fieldHeight * 0.25f
        val sixYardTop = insetY + (fieldHeight - sixYardHeight) / 2f
        drawRect(pitchLine, Offset(insetX, penaltyTop), Size(penaltyWidth, penaltyHeight), style = Stroke(lineWidth))
        drawRect(pitchLine, Offset(insetX + fieldWidth - penaltyWidth, penaltyTop), Size(penaltyWidth, penaltyHeight), style = Stroke(lineWidth))
        drawRect(pitchLine, Offset(insetX, sixYardTop), Size(sixYardWidth, sixYardHeight), style = Stroke(lineWidth))
        drawRect(pitchLine, Offset(insetX + fieldWidth - sixYardWidth, sixYardTop), Size(sixYardWidth, sixYardHeight), style = Stroke(lineWidth))
        drawCircle(pitchLine, radius = 1.2.dp.toPx(), center = point(0.11f, 0.5f))
        drawCircle(pitchLine, radius = 1.2.dp.toPx(), center = point(0.89f, 0.5f))

        val goalDepth = 4.dp.toPx()
        val goalHeight = fieldHeight * 0.18f
        val goalTop = insetY + (fieldHeight - goalHeight) / 2f
        drawRect(pitchLine, Offset(insetX - goalDepth, goalTop), Size(goalDepth, goalHeight), style = Stroke(lineWidth))
        drawRect(pitchLine, Offset(insetX + fieldWidth, goalTop), Size(goalDepth, goalHeight), style = Stroke(lineWidth))

        val phase = motionPhase.value
        val rendered = entities.map { entity ->
            val seed = entity.motionSeed
            val roleAmplitude = if (entity.position == "GK") 0.006f else 0.014f
            val runDirection = if (entity.isHome) 1f else -1f
            var x = entity.baseX + territoryBias +
                sin((phase + seed).toDouble()).toFloat() * roleAmplitude
            var y = entity.baseY +
                cos((phase * 0.82f + seed).toDouble()).toFloat() * (roleAmplitude * 1.35f)

            val isEventPlayer = latestEvent?.let {
                it.playerId == entity.playerId && it.type in attackingEvents
            } == true
            if (isEventPlayer) {
                val eventTargetX = if (entity.isHome) 0.82f else 0.18f
                x = x * 0.55f + eventTargetX * 0.45f
                y += sin((phase * 1.35f + seed).toDouble()).toFloat() * 0.018f
            } else {
                x += runDirection * sin((phase * 0.55f + seed).toDouble()).toFloat() * 0.006f
            }

            RenderedPitchEntity(
                entity = entity,
                x = x.coerceIn(0.025f, 0.975f),
                y = y.coerceIn(0.035f, 0.965f)
            )
        }

        val dotRadius = (size.minDimension * 0.019f)
            .coerceIn(4.2.dp.toPx(), 7.dp.toPx())
        rendered.forEach { player ->
            val centre = point(player.x, player.y)
            val playerColor = if (player.entity.isHome) FM_GREEN else awayColor
            drawCircle(Color.Black.copy(alpha = 0.48f), dotRadius + 2.dp.toPx(), centre)
            if (player.entity.playerId == latestEvent?.playerId) {
                drawCircle(
                    color = highlightColor,
                    radius = dotRadius + 3.dp.toPx(),
                    center = centre,
                    style = Stroke(1.4.dp.toPx())
                )
            }
            drawCircle(playerColor, dotRadius, centre)
            drawCircle(Color.White.copy(alpha = 0.92f), dotRadius * 0.28f, centre)
            if (player.entity.position == "GK") {
                drawCircle(Color.White.copy(alpha = 0.72f), dotRadius, centre, style = Stroke(1.dp.toPx()))
            }
        }

        val eventPlayer = rendered.firstOrNull { it.entity.playerId == latestEvent?.playerId }
        val ballPosition = when {
            latestEvent?.type?.let { it in setOf(EventType.MATCH_START, EventType.KICKOFF_FIRST, EventType.SECOND_HALF) } == true -> 0.5f to 0.5f
            latestEvent?.type == EventType.CORNER && eventIsHome -> 0.965f to if (currentMinute % 2 == 0) 0.04f else 0.96f
            latestEvent?.type == EventType.CORNER -> 0.035f to if (currentMinute % 2 == 0) 0.04f else 0.96f
            eventPlayer != null -> {
                val direction = if (eventPlayer.entity.isHome) 1f else -1f
                (eventPlayer.x + direction * 0.024f).coerceIn(0.02f, 0.98f) to
                    (eventPlayer.y + sin(phase.toDouble()).toFloat() * 0.008f).coerceIn(0.03f, 0.97f)
            }
            else -> {
                val fallbackX = 0.5f + territoryBias * 1.8f + sin((phase * 0.72f).toDouble()).toFloat() * 0.07f
                val fallbackY = 0.5f + cos((phase * 0.64f).toDouble()).toFloat() * 0.16f
                fallbackX.coerceIn(0.04f, 0.96f) to fallbackY.coerceIn(0.04f, 0.96f)
            }
        }
        val ballCentre = point(ballPosition.first, ballPosition.second)
        drawCircle(ballColor.copy(alpha = 0.18f), dotRadius * 2.6f, ballCentre)
        drawCircle(ballColor, dotRadius * 0.7f, ballCentre)
        drawCircle(Color.Black.copy(alpha = 0.78f), dotRadius * 0.7f, ballCentre, style = Stroke(0.8.dp.toPx()))
    }
}

private fun buildPitchEntities(
    lineup: List<PlayerEntity>,
    isHome: Boolean
): List<PitchEntity> {
    val players = lineup.take(11)
    return players
        .groupBy { positionBand(it.position) }
        .toSortedMap()
        .flatMap { (band, bandPlayers) ->
            val orderedPlayers = bandPlayers.sortedWith(
                compareBy<PlayerEntity> { verticalPositionPriority(it.position) }
                    .thenBy { it.id }
            )
            orderedPlayers.mapIndexed { index, player ->
                val homeX = when (band) {
                    0 -> 0.065f
                    1 -> 0.245f
                    2 -> 0.425f
                    else -> 0.625f
                }
                PitchEntity(
                    playerId = player.id,
                    isHome = isHome,
                    position = player.position.uppercase(),
                    baseX = if (isHome) homeX else 1f - homeX,
                    baseY = (index + 1f) / (orderedPlayers.size + 1f),
                    motionSeed = ((player.id % 997L).toFloat() / 997f) * (2f * PI.toFloat())
                )
            }
        }
}

private fun positionBand(position: String): Int = when (position.uppercase()) {
    "GK" -> 0
    "LB", "LWB", "CB", "LCB", "RCB", "RB", "RWB" -> 1
    "CDM", "DM", "CM", "LCM", "RCM", "CAM", "LM", "RM" -> 2
    else -> 3
}

private fun verticalPositionPriority(position: String): Int = when (position.uppercase()) {
    "LB", "LWB", "LM", "LW" -> 0
    "LCB", "LCM" -> 1
    "GK", "CB", "CDM", "DM", "CM", "CAM", "CF", "ST" -> 2
    "RCB", "RCM" -> 3
    "RB", "RWB", "RM", "RW" -> 4
    else -> 2
}

@Composable
fun SimCol(title: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp)).padding(8.dp)) {
        Text(title, color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun StatRowModern(label: String, h: Int, a: Int) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(h.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray.copy(alpha = 0.6f), fontSize = 8.sp)
            Text(a.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        val progress = if (h + a > 0) h.toFloat() / (h + a) else 0.5f
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.05f))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(FM_GREEN)
            )
        }
    }
}

@Composable
fun EventText(min: Int, text: String, type: EventType, playerName: String = "") {
    val color = when (type) {
        EventType.GOAL, EventType.PENALTY_GOAL, EventType.INJURY_TIME_GOAL -> FM_GREEN
        EventType.RED_CARD -> Color.Red
        EventType.YELLOW_CARD -> Color.Yellow
        EventType.GOAL_OWN -> Color(0xFFFF9800)
        EventType.SUBSTITUTION -> Color(0xFF2196F3)
        EventType.HALFTIME, EventType.MATCH_END -> Color(0xFF9C27B0)
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (type == EventType.GOAL || type == EventType.PENALTY_GOAL)
                    FM_GREEN.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(com.mountsa.fmsimulation.ui.localization.localized("$min'"), color = FM_GREEN, fontSize = 10.sp, modifier = Modifier.width(22.dp), fontWeight = FontWeight.Bold)
            Text(text, color = color, fontSize = 10.sp, fontWeight = if (type == EventType.GOAL || type == EventType.PENALTY_GOAL) FontWeight.Bold else FontWeight.Normal)
        }
        if (playerName.isNotEmpty()) {
            Text(playerName, color = Color.Gray.copy(alpha = 0.6f), fontSize = 9.sp)
        }
    }
}

private fun fallbackCommentary(event: MatchEvent): String = when (event.type) {
    EventType.MATCH_START, EventType.KICKOFF_FIRST -> "Kick-off. Both teams settle into their shape."
    EventType.HALFTIME -> "The referee signals half-time. Time for the managers to adjust."
    EventType.SECOND_HALF -> "The second half is underway."
    EventType.GOAL, EventType.PENALTY_GOAL -> "GOAL! ${event.playerName.ifBlank { event.teamName }} finds the net."
    EventType.SHOT, EventType.SHOT_ON_TARGET -> "${event.playerName.ifBlank { event.teamName }} gets a shot away."
    EventType.SAVE, EventType.BIG_SAVE, EventType.GOAL_KEEPER_SAVE -> "A strong save keeps the score unchanged."
    EventType.CORNER -> "Corner to ${event.teamName.ifBlank { "the attacking side" }}."
    EventType.FOUL -> "The referee stops play for a foul."
    EventType.YELLOW_CARD -> "Yellow card for ${event.playerName.ifBlank { "the offender" }}."
    EventType.RED_CARD -> "Red card! ${event.playerName.ifBlank { "A player" }} is sent off."
    EventType.SUBSTITUTION -> "A substitution changes the shape of ${event.teamName.ifBlank { "the team" }}."
    EventType.INJURY -> "Play is stopped while ${event.playerName.ifBlank { "the player" }} receives treatment."
    EventType.MATCH_END -> "Full-time. The referee brings the match to an end."
    else -> "${event.minute}' ${event.type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}."
}
