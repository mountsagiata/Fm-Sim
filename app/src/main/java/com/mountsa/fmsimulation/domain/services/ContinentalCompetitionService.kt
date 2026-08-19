// File: domain/services/ContinentalCompetitionService.kt
package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContinentalCompetitionService @Inject constructor(
    private val repository: DataRepository,
    private val simulator: MatchSimulator
) {
    suspend fun generateContinentalFixtures() {
        val career = repository.getCareer().first() ?: return
        val currentSeasonName = career.season
        val currentSeasonYear = currentSeasonName.split("/").first().toIntOrNull() ?: 2027

        // Mengambil sampel klub dengan reputasi tertinggi di database untuk simulasi turnamen mini kontinental
        val allNations = repository.getAllNations().first()
        val continentalClubs = mutableListOf<Long>()

        for (nation in allNations) {
            val clubs = repository.getClubsByLeague(nation.id).first()
                .sortedByDescending { it.reputation }
                .take(2) // Ambil top 2 klub per wilayah kompetisi
            continentalClubs.addAll(clubs.map { it.id })
        }

        if (continentalClubs.size < 4) return

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = career.currentDate
        calendar.add(Calendar.DAY_OF_MONTH, 3) // Diadwalkan di tengah pekan berikutnya

        val continentalMatches = mutableListOf<MatchEntity>()

        // Simulasi pembentukan bagan fase gugur acak (Knockout Quarter-Finals)
        val shuffledClubs = continentalClubs.shuffled()
        for (i in 0 until shuffledClubs.size - 1 step 2) {
            continentalMatches.add(
                MatchEntity(
                    leagueId = null, // Bernilai null menandakan turnamen non-domestik liga
                    cupId = 999L,    // ID Penanda Continental Cup
                    season = currentSeasonName,
                    stage = "QUARTER_FINAL",
                    homeClubId = shuffledClubs[i],
                    awayClubId = shuffledClubs[i + 1],
                    matchDate = calendar.timeInMillis
                )
            )
        }
        repository.insertMatches(continentalMatches)
    }

    suspend fun processContinentalMatches() {
        val career = repository.getCareer().first() ?: return
        // Ambil laga piala kontinental yang dijadwalkan pada hari ini
        val todaysContinentalMatches = repository.getMatchesByDate(career.currentDate)
            .filter { it.cupId == 999L && !it.isPlayed }

        for (match in todaysContinentalMatches) {
            simulator.simulateMatch(match)
        }
    }
}
