package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.model.MatchSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchSessionBuilder @Inject constructor(
    private val repository: DataRepository
) {

    suspend fun build(
        match: MatchEntity
    ): MatchSession {

        val homePlayers =
            repository
                .getPlayersByClubSync(
                    match.homeClubId
                )

        val awayPlayers =
            repository
                .getPlayersByClubSync(
                    match.awayClubId
                )

        val homeLineup =
            homePlayers
                .filter {
                    it.startingIndex in 0..10
                }
                .ifEmpty {
                    LineupSelector.select(homePlayers)
                }

        val awayLineup =
            awayPlayers
                .filter {
                    it.startingIndex in 0..10
                }
                .ifEmpty {
                    LineupSelector.select(awayPlayers)
                }

        val competitionName = when {
            match.leagueId != null -> repository.getLeagueName(match.leagueId)
            match.stage == "FRIENDLY" -> "Friendly"
            match.stage.startsWith("DOMESTIC_CUP") -> "Domestic Cup"
            match.stage.startsWith("CONTINENTAL") -> "Continental Cup"
            match.cupId != null -> "Cup"
            else -> match.stage.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        }

        val homeClub = repository.getClubById(match.homeClubId)
        val awayClub = repository.getClubById(match.awayClubId)

        return MatchSession(
            match = match,
            homeLineup = homeLineup,
            awayLineup = awayLineup,
            homeBench = homePlayers.filterNot { candidate -> homeLineup.any { it.id == candidate.id } }.sortedByDescending { it.overall }.take(12),
            awayBench = awayPlayers.filterNot { candidate -> awayLineup.any { it.id == candidate.id } }.sortedByDescending { it.overall }.take(12),
            homeClubName = homeClub?.name ?: "Home",
            awayClubName = awayClub?.name ?: "Away",
            homeShortName = homeClub?.shortName ?: "HME",
            awayShortName = awayClub?.shortName ?: "AWY",
            competitionName = competitionName,
            stadiumName = homeClub?.stadium ?: "Home Stadium",
            weather = "Clear",
            kickoffTime = "20:00"
        )
    }
}
