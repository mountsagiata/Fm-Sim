// File: domain/engine/LeagueManager.kt
package com.mountsa.fmsimulation.domain.engine

import com.mountsa.fmsimulation.data.local.entities.SeasonHistoryEntity
import com.mountsa.fmsimulation.data.local.entities.StandingEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.services.BoardObjectiveGenerator
import com.mountsa.fmsimulation.domain.services.ManagerRatingService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeagueManager @Inject constructor(
    private val repository: DataRepository,
    private val matchScheduler: MatchScheduler,
    private val objectiveGenerator: BoardObjectiveGenerator,
    private val managerRatingService: ManagerRatingService
) {
    suspend fun updateStandings(leagueId: Long) {
        val standings = repository.getLeagueStandings(leagueId).first()
        val sorted = standings.sortedWith(
            compareByDescending<StandingEntity> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
        )

        sorted.forEachIndexed { index, standing ->
            repository.updateStanding(standing.copy(position = index + 1))
        }
    }

    suspend fun checkPromotionRelegation(leagueId: Long) {
        val career = repository.getCareer().first() ?: return
        val currentSeason = career.season

        val standings = repository.getLeagueStandings(leagueId).first()
        if (standings.isEmpty()) return

        val totalPlayedByTeam = standings.first().played
        val expectedMatches = (standings.size - 1) * 2
        if (totalPlayedByTeam < expectedMatches) return

        // Item 24: Save Season History
        saveSeasonHistory(leagueId, currentSeason, standings)

        // Update Manager Rating
        managerRatingService.updateManagerRating(career.selectedClubId)

        // Process Season Transition
        val years = currentSeason.split("/")
        val startYear = years.first().toIntOrNull() ?: 2027
        val endYear = years.last().toIntOrNull() ?: 2028
        val nextSeasonName = "${startYear + 1}/${endYear + 1}"

        val resetStandings = standings.map { clubStanding ->
            clubStanding.copy(
                played = 0, wins = 0, draws = 0, losses = 0,
                goalsFor = 0, goalsAgainst = 0, goalDifference = 0,
                points = 0, position = clubStanding.position
            )
        }

        resetStandings.forEach { repository.updateStanding(it) }

        repository.saveCareer(career.copy(season = nextSeasonName))
        objectiveGenerator.generateSeasonObjectives(career.selectedClubId)
        matchScheduler.scheduleSeason(leagueId)
    }

    private suspend fun saveSeasonHistory(leagueId: Long, season: String, standings: List<StandingEntity>) {
        val topScorer = repository.getTopScorersByLeague(leagueId, 1).first().firstOrNull()
        val bestPlayer = repository.getTopPlayersByLeague(leagueId, 1).first().firstOrNull()

        standings.forEach { standing ->
            repository.insertSeasonHistory(
                SeasonHistoryEntity(
                    clubId = standing.clubId,
                    season = season,
                    leagueId = leagueId,
                    position = standing.position,
                    played = standing.played,
                    wins = standing.wins,
                    draws = standing.draws,
                    losses = standing.losses,
                    points = standing.points,
                    goalsFor = standing.goalsFor,
                    goalsAgainst = standing.goalsAgainst,
                    trophy = if (standing.position == 1) "League Winner" else "",
                    topScorer = topScorer?.name ?: "",
                    topScorerGoals = topScorer?.goals ?: 0,
                    bestPlayer = bestPlayer?.name ?: "",
                    bestPlayerRating = bestPlayer?.averageRating ?: 0f
                )
            )
        }
    }
}