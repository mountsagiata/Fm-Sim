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
        Log.d("MatchScheduler", "Inserted ${matches.size} matches for league $leagueId")
    }
}
