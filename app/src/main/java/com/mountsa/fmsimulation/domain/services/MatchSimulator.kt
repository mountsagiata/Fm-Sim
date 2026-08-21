package com.mountsa.fmsimulation.domain.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mountsa.fmsimulation.core.match.event.EventCategory
import com.mountsa.fmsimulation.core.match.event.EventFactory
import com.mountsa.fmsimulation.core.match.event.EventType
import com.mountsa.fmsimulation.core.match.event.MatchEvent
import com.mountsa.fmsimulation.core.enums.PlayerStatus
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.local.entities.StandingEntity
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchSimulator @Inject constructor(
    private val repository: DataRepository,
    private val tacticEngine: TacticEngine,
    private val moraleService: MoraleService
) {
    private val gson = Gson()

    private data class PlayerMatchStats(
        val playerId: Long,
        var goals: Int = 0,
        var assists: Int = 0,
        var shots: Int = 0,
        var shotsOnTarget: Int = 0,
        var tackles: Int = 0,
        var passes: Int = 0,
        var fouls: Int = 0,
        var yellowCards: Int = 0,
        var redCards: Int = 0,
        var rating: Float = 6.0f,
        var minutesPlayed: Int = 90
    )

    suspend fun simulateMatch(match: MatchEntity) {
        if (match.isPlayed) return

        val homeClub = repository.getClubById(match.homeClubId) ?: return
        val awayClub = repository.getClubById(match.awayClubId) ?: return

        val homePlayers = getPlayers(match.homeClubId, match.homeLineupJson)
        val awayPlayers = getPlayers(match.awayClubId, match.awayLineupJson)

        val homeStartingXI = selectLineupForCompetition(homePlayers, match.stage).toMutableList()
        val awayStartingXI = selectLineupForCompetition(awayPlayers, match.stage).toMutableList()

        if (homeStartingXI.isEmpty() || awayStartingXI.isEmpty()) return

        val playerStats = mutableMapOf<Long, PlayerMatchStats>()
        (homeStartingXI + awayStartingXI).forEach { playerStats[it.id] = PlayerMatchStats(it.id) }

        val homeTactic = tacticEngine.calculateTacticImpact(homeClub)
        val awayTactic = tacticEngine.calculateTacticImpact(awayClub)

        fun calculatePerf(p: PlayerEntity): Float {
            val sharpnessEffect = (p.sharpness / 100f) * 0.15f
            val fitnessEffect = (p.fitness / 100f) * 0.25f
            return 0.6f + sharpnessEffect + fitnessEffect
        }

        var hGoals = 0; var aGoals = 0
        var hShots = 0; var aShots = 0
        var hOnTarget = 0; var aOnTarget = 0
        var hFouls = 0; var aFouls = 0
        var hYellow = 0; var aYellow = 0
        var hRed = 0; var aRed = 0
        var hCorners = 0; var aCorners = 0
        var hXG = 0f; var aXG = 0f
        
        var homeWeight = 0.5f

        val events = mutableListOf<MatchEvent>()
        events.add(EventFactory.create(0, EventType.MATCH_START, EventCategory.MATCH, 0, 0, teamName = "Kick Off"))

        for (minute in 1..90) {
            if (minute == 45) events.add(EventFactory.create(45, EventType.HALFTIME, EventCategory.MATCH, hGoals, aGoals))
            
            val hAtt = (homeStartingXI.sumOf { ((it.shooting + it.pace) * calculatePerf(it)).toDouble() } / 11.0).toFloat() * homeTactic.att
            val hMid = (homeStartingXI.sumOf { ((it.passing + it.dribbling) * calculatePerf(it)).toDouble() } / 11.0).toFloat() * homeTactic.mid
            val hDef = (homeStartingXI.sumOf { ((it.defending + it.physical) * calculatePerf(it)).toDouble() } / 11.0).toFloat() * homeTactic.def
            
            val aAtt = (awayStartingXI.sumOf { ((it.shooting + it.pace) * calculatePerf(it)).toDouble() } / 11.0).toFloat() * awayTactic.att
            val aMid = (awayStartingXI.sumOf { ((it.passing + it.dribbling) * calculatePerf(it)).toDouble() } / 11.0).toFloat() * awayTactic.mid
            val aDef = (awayStartingXI.sumOf { ((it.defending + it.physical) * calculatePerf(it)).toDouble() } / 11.0).toFloat() * awayTactic.def

            homeWeight = (hMid / (hMid + aMid + 0.1f)) * 1.1f
            
            if (Random.nextFloat() < 0.15f) {
                val isHomeEvent = Random.nextFloat() < (homeWeight / (homeWeight + 1f))
                val actingTeam = if (isHomeEvent) homeClub else awayClub
                val actingXI = if (isHomeEvent) homeStartingXI else awayStartingXI
                val opponentXI = if (isHomeEvent) awayStartingXI else homeStartingXI
                val actingAtt = if (isHomeEvent) hAtt else aAtt
                val defendingDef = if (isHomeEvent) aDef else hDef

                val rand = Random.nextFloat()
                when {
                    rand < 0.30f -> {
                        val shooter = actingXI.random()
                        val assister = actingXI.filter { it.id != shooter.id }.randomOrNull() ?: shooter
                        
                        val penaltyRand = Random.nextFloat()
                        if (penaltyRand < 0.05f) {
                            events.add(EventFactory.create(minute, EventType.FREEKICK, EventCategory.ATTACK, hGoals, aGoals, shooter, actingTeam.id, actingTeam.name).copy(commentary = "PENALTY awarded to ${actingTeam.name}!"))
                            if (Random.nextFloat() < 0.8f) {
                                if (isHomeEvent) hGoals++ else aGoals++
                                playerStats[shooter.id]?.goals = (playerStats[shooter.id]?.goals ?: 0) + 1
                                events.add(EventFactory.create(minute, EventType.PENALTY_GOAL, EventCategory.ATTACK, hGoals, aGoals, shooter, actingTeam.id, actingTeam.name))
                            } else {
                                events.add(EventFactory.create(minute, EventType.MISS, EventCategory.ATTACK, hGoals, aGoals, shooter, actingTeam.id, actingTeam.name).copy(commentary = "Penalty MISSED!"))
                            }
                        } else {
                            val xG = (actingAtt / (actingAtt + defendingDef + 0.1f)) * 0.35f
                            if (isHomeEvent) hXG += xG else aXG += xG
                            if (isHomeEvent) hShots++ else aShots++
                            
                            if (Random.nextFloat() < xG) {
                                val isOwnGoal = Random.nextFloat() < 0.02f
                                if (isOwnGoal) {
                                    val culprit = opponentXI.random()
                                    if (isHomeEvent) hGoals++ else aGoals++
                                    events.add(EventFactory.create(minute, EventType.GOAL_OWN, EventCategory.ATTACK, hGoals, aGoals, culprit, actingTeam.id, actingTeam.name))
                                } else {
                                    if (isHomeEvent) hGoals++ else aGoals++
                                    if (isHomeEvent) hOnTarget++ else aOnTarget++
                                    playerStats[shooter.id]?.goals = (playerStats[shooter.id]?.goals ?: 0) + 1
                                    playerStats[assister.id]?.assists = (playerStats[assister.id]?.assists ?: 0) + 1
                                    events.add(EventFactory.create(minute, EventType.GOAL, EventCategory.ATTACK, hGoals, aGoals, shooter, actingTeam.id, actingTeam.name).copy(secondaryPlayerId = assister.id, secondaryPlayerName = assister.shortName))
                                }
                            } else {
                                if (Random.nextFloat() < 0.4f) { 
                                    if (isHomeEvent) hOnTarget++ else aOnTarget++
                                    events.add(EventFactory.create(minute, EventType.SHOT_ON_TARGET, EventCategory.ATTACK, hGoals, aGoals, shooter, actingTeam.id, actingTeam.name)) 
                                }
                                else events.add(EventFactory.create(minute, EventType.SHOT, EventCategory.ATTACK, hGoals, aGoals, shooter, actingTeam.id, actingTeam.name))
                            }
                        }
                    }
                    rand < 0.40f -> {
                        if (minute > 60 && actingXI.size == 11) {
                            val tiredPlayer = actingXI.minByOrNull { it.fitness }
                            if (tiredPlayer != null && tiredPlayer.fitness < 65) {
                                val bench = (if (isHomeEvent) homePlayers else awayPlayers).filter { p -> !actingXI.any { it.id == p.id } && p.status == PlayerStatus.FIT }
                                val sub = bench.maxByOrNull { it.overall }
                                if (sub != null) {
                                    actingXI.remove(tiredPlayer)
                                    actingXI.add(sub)
                                    playerStats[sub.id] = PlayerMatchStats(sub.id, minutesPlayed = 90 - minute)
                                    playerStats[tiredPlayer.id]?.minutesPlayed = minute
                                    events.add(EventFactory.create(minute, EventType.SUBSTITUTION, EventCategory.PLAYER, hGoals, aGoals, sub, actingTeam.id, actingTeam.name).copy(secondaryPlayerId = tiredPlayer.id, secondaryPlayerName = tiredPlayer.shortName))
                                }
                            }
                        }
                    }
                    rand < 0.50f -> {
                        val fouler = actingXI.random()
                        if (isHomeEvent) hFouls++ else aFouls++
                        val isRed = Random.nextFloat() < 0.1f
                        if (isRed) {
                            actingXI.remove(fouler)
                            if (isHomeEvent) hRed++ else aRed++
                            playerStats[fouler.id]?.redCards = 1
                            events.add(EventFactory.create(minute, EventType.RED_CARD, EventCategory.DISCIPLINE, hGoals, aGoals, fouler, actingTeam.id, actingTeam.name))
                        } else {
                            if (isHomeEvent) hYellow++ else aYellow++
                            playerStats[fouler.id]?.yellowCards = (playerStats[fouler.id]?.yellowCards ?: 0) + 1
                            events.add(EventFactory.create(minute, EventType.YELLOW_CARD, EventCategory.DISCIPLINE, hGoals, aGoals, fouler, actingTeam.id, actingTeam.name))
                        }
                    }
                    rand < 0.55f -> {
                        if (isHomeEvent) hCorners++ else aCorners++
                        events.add(EventFactory.create(minute, EventType.CORNER, EventCategory.ATTACK, hGoals, aGoals, teamId = actingTeam.id, teamName = actingTeam.name))
                    }
                }
            }
        }

        events.add(EventFactory.create(90, EventType.MATCH_END, EventCategory.MATCH, hGoals, aGoals, teamName = "Full Time"))

        (homeStartingXI + awayStartingXI).forEach { p ->
            val stats = playerStats[p.id]!!
            var rating = 6.0f + (p.overall / 50f)
            rating += stats.goals * 1.8f
            rating += stats.assists * 1.2f
            rating += stats.shotsOnTarget * 0.3f
            rating -= stats.yellowCards * 0.6f
            rating -= stats.redCards * 3.0f
            if (p.position == "GK") {
                val conceded = if (p.clubId == homeClub.id) aGoals else hGoals
                rating += if (conceded == 0) 1.5f else -conceded * 0.4f
            }
            stats.rating = rating.coerceIn(4.0f, 10.0f)
        }

        val motmId = playerStats.values.maxByOrNull { it.rating }?.playerId ?: -1L

        repository.updateMatch(match.copy(
            homeScore = hGoals, awayScore = aGoals, isPlayed = true,
            possessionHome = ((homeWeight * 100) / (homeWeight + 1f)).toInt().coerceIn(35, 65),
            possessionAway = 100 - ((homeWeight * 100) / (homeWeight + 1f)).toInt().coerceIn(35, 65),
            shotsHome = hShots, shotsAway = aShots, shotsOnTargetHome = hOnTarget, shotsOnTargetAway = aOnTarget,
            yellowCardsHome = hYellow, yellowCardsAway = aYellow, redCardsHome = hRed, redCardsAway = aRed,
            xGHome = hXG, xGAway = aXG, motmPlayerId = motmId,
            playerMatchStatsJson = gson.toJson(playerStats.values.toList()),
            matchEvents = gson.toJson(events.sortedBy { it.minute })
        ))

        if (match.stage == "LEAGUE") {
            updateStanding(match.leagueId!!, match.homeClubId, hGoals, aGoals)
            updateStanding(match.leagueId!!, match.awayClubId, aGoals, hGoals)
        }

        moraleService.applyMatchResultImpact(match.homeClubId, hGoals > aGoals, hGoals == aGoals)
        moraleService.applyMatchResultImpact(match.awayClubId, aGoals > hGoals, hGoals == aGoals)

        updatePostMatchPlayersGlobal(homePlayers, homeClub.pressing, playerStats)
        updatePostMatchPlayersGlobal(awayPlayers, awayClub.pressing, playerStats)
    }

    private fun selectLineupForCompetition(players: List<PlayerEntity>, stage: String): List<PlayerEntity> {
        val fit = players.filter { it.status == PlayerStatus.FIT && it.fitness > 45 }
        return fit.sortedByDescending { it.overall }.take(11)
    }

    private suspend fun updatePostMatchPlayersGlobal(players: List<PlayerEntity>, pressing: Int, stats: Map<Long, PlayerMatchStats>) {
        val updated = players.map { p ->
            val s = stats[p.id] ?: return@map p
            var status = p.status
            var susp = p.suspensionGamesRemaining
            if (s.redCards > 0) { status = PlayerStatus.SUSPENDED; susp = 3 }
            else if (p.yellowCards + s.yellowCards >= 5) { status = PlayerStatus.SUSPENDED; susp = 1 }

            p.copy(
                appearances = p.appearances + 1,
                goals = p.goals + s.goals,
                assists = p.assists + s.assists,
                yellowCards = p.yellowCards + s.yellowCards,
                redCards = p.redCards + s.redCards,
                averageRating = if (p.appearances == 0) s.rating else ((p.averageRating * p.appearances) + s.rating) / (p.appearances + 1),
                fitness = (p.fitness - (10 + (pressing / 10) + Random.nextInt(0, 5))).coerceAtLeast(0),
                status = status,
                suspensionGamesRemaining = susp
            )
        }
        repository.updatePlayers(updated)
    }

    private suspend fun updateStanding(leagueId: Long, clubId: Long, gf: Int, ga: Int) {
        val s = repository.getStandingByClub(leagueId, clubId) ?: StandingEntity(leagueId = leagueId, clubId = clubId, position = 1)
        val win = gf > ga; val draw = gf == ga
        repository.updateStanding(s.copy(
            played = s.played + 1, wins = s.wins + if(win) 1 else 0, draws = s.draws + if(draw) 1 else 0,
            losses = s.losses + if(!win && !draw) 1 else 0, goalsFor = s.goalsFor + gf, goalsAgainst = s.goalsAgainst + ga,
            goalDifference = (s.goalsFor + gf) - (s.goalsAgainst + ga), points = s.points + (if(win) 3 else if(draw) 1 else 0)
        ))
    }

    private suspend fun getPlayers(clubId: Long, json: String): List<PlayerEntity> {
        return if (json.isNotEmpty()) {
            try { gson.fromJson(json, object : TypeToken<List<PlayerEntity>>() {}.type) }
            catch (e: Exception) { repository.getPlayersByClubSync(clubId) }
        } else repository.getPlayersByClubSync(clubId)
    }
}
