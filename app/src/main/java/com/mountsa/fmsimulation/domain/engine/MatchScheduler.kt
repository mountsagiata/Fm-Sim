// File: domain/engine/MatchScheduler.kt
package com.mountsa.fmsimulation.domain.engine

import android.util.Log
import com.mountsa.fmsimulation.data.local.entities.CalendarEventEntity
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class MatchScheduler @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun scheduleSeason(leagueId: Long) {
        val clubs = repository.getClubsByLeague(leagueId).first().toMutableList()
        if (clubs.size < 2) return

        val career = repository.getCareer().first()
        // PERBAIKAN: Default ke 2025 agar sinkron dengan CareerSetup
        val currentSeasonYear = career?.season?.split("/")?.first()?.toIntOrNull() ?: 2025
        val currentSeasonName = career?.season ?: "2025/2026"

        Log.d("MatchScheduler", "Scheduling league $leagueId for season $currentSeasonName (Year: $currentSeasonYear)")

        repository.deleteMatchesByLeague(leagueId)
        for (club in clubs) {
            repository.deleteEventsByType(club.id, "MATCH")
        }

        val matches = mutableListOf<MatchEntity>()
        val calendarEvents = mutableListOf<CalendarEventEntity>()
        
        val hasGhost = clubs.size % 2 != 0
        val teamCount = if (hasGhost) clubs.size + 1 else clubs.size
        val rounds = teamCount - 1
        val matchesPerRound = teamCount / 2

        val baseIds = clubs.map { it.id }.toMutableList()
        if (hasGhost) baseIds.add(-1L)

        val firstHalfPairings = mutableListOf<List<Pair<Long, Long>>>()
        val tempIds = baseIds.toMutableList()
        
        for (round in 0 until rounds) {
            val roundPairings = mutableListOf<Pair<Long, Long>>()
            for (matchIdx in 0 until matchesPerRound) {
                val home = tempIds[matchIdx]
                val away = tempIds[teamCount - 1 - matchIdx]
                if (home != -1L && away != -1L) {
                    val isHome = (round + matchIdx) % 2 == 0
                    if (isHome) roundPairings.add(home to away) else roundPairings.add(away to home)
                }
            }
            firstHalfPairings.add(roundPairings)
            val last = tempIds.removeAt(tempIds.size - 1)
            tempIds.add(1, last)
        }

        // Start from first Saturday of August
        val calendar = Calendar.getInstance().apply {
            set(currentSeasonYear, Calendar.AUGUST, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Generate matches...
        for (roundMatches in firstHalfPairings) {
            for (pair in roundMatches) {
                matches.add(MatchEntity(
                    leagueId = leagueId,
                    homeClubId = pair.first,
                    awayClubId = pair.second,
                    matchDate = calendar.timeInMillis,
                    season = currentSeasonName,
                    stage = "LEAGUE"
                ))
            }
            calendar.add(Calendar.DAY_OF_MONTH, 7)
        }

        // Pre-season friendlies give every club match preparation before the
        // competitive calendar starts. They deliberately have no leagueId so
        // they never alter league standings.
        val friendlyDate = Calendar.getInstance().apply {
            set(currentSeasonYear, Calendar.JULY, 19, 15, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        clubs.shuffled().chunked(2).filter { it.size == 2 }.forEachIndexed { index, pair ->
            matches += MatchEntity(
                homeClubId = pair[0].id,
                awayClubId = pair[1].id,
                matchDate = friendlyDate.timeInMillis + index * 30L * 60L * 1000L,
                season = currentSeasonName,
                stage = "FRIENDLY"
            )
        }

        // Domestic cup opening round. Later rounds are generated from winners
        // by the season processor; cupId keeps it separate from league tables.
        val cupDate = Calendar.getInstance().apply {
            set(currentSeasonYear, Calendar.SEPTEMBER, 23, 19, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }
        clubs.shuffled().chunked(2).filter { it.size == 2 }.forEachIndexed { index, pair ->
            matches += MatchEntity(
                cupId = leagueId * 100 + 1,
                homeClubId = pair[0].id,
                awayClubId = pair[1].id,
                matchDate = cupDate.timeInMillis + index * 15L * 60L * 1000L,
                season = currentSeasonName,
                stage = "DOMESTIC_CUP_R1"
            )
        }

        // Seed a continental group schedule for the strongest four clubs in
        // each loaded league. This makes the competition visible immediately
        // while still using the real clubs and match engine.
        val continentalClubs = clubs.sortedByDescending { it.overall }.take(4)
        val continentalDates = listOf(Calendar.OCTOBER to 1, Calendar.OCTOBER to 22, Calendar.NOVEMBER to 5)
        continentalDates.forEachIndexed { round, (month, day) ->
            val roundDate = Calendar.getInstance().apply {
                set(currentSeasonYear, month, day, 20, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (continentalClubs.size == 4) {
                val rotation = continentalClubs.drop(round % 4) + continentalClubs.take(round % 4)
                listOf(rotation[0] to rotation[3], rotation[1] to rotation[2]).forEach { pair ->
                    matches += MatchEntity(
                        cupId = leagueId * 100 + 2,
                        homeClubId = pair.first.id,
                        awayClubId = pair.second.id,
                        matchDate = roundDate.timeInMillis,
                        season = currentSeasonName,
                        stage = "CONTINENTAL_GROUP"
                    )
                }
            }
        }

        calendar.add(Calendar.DAY_OF_MONTH, 14) // Break

        for (roundMatches in firstHalfPairings) {
            for (pair in roundMatches) {
                matches.add(MatchEntity(
                    leagueId = leagueId,
                    homeClubId = pair.second,
                    awayClubId = pair.first,
                    matchDate = calendar.timeInMillis,
                    season = currentSeasonName,
                    stage = "LEAGUE"
                ))
            }
            calendar.add(Calendar.DAY_OF_MONTH, 7)
        }

        repository.insertMatches(matches)

        // Materialise the complete match calendar up front. Previously events
        // were only created on the day itself, which made every future date
        // look empty even though fixtures already existed.
        matches.forEach { match ->
            val competition = when {
                match.stage == "FRIENDLY" -> "Friendly"
                match.stage.startsWith("DOMESTIC_CUP") -> "Domestic Cup"
                match.stage.startsWith("CONTINENTAL") -> "Continental"
                else -> "League"
            }
            listOf(match.homeClubId, match.awayClubId).forEach { clubId ->
                calendarEvents += CalendarEventEntity(
                    clubId = clubId,
                    eventDate = match.matchDate,
                    type = "MATCH",
                    title = "$competition match",
                    description = match.stage.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                )
            }
        }
        repository.insertEvents(calendarEvents)
        Log.d("MatchScheduler", "Inserted ${matches.size} matches for league $leagueId")
    }
}
