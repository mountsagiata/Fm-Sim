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
                    homePlayers
                        .sortedByDescending {
                            it.overall
                        }
                        .take(11)
                }

        val awayLineup =
            awayPlayers
                .filter {
                    it.startingIndex in 0..10
                }
                .ifEmpty {
                    awayPlayers
                        .sortedByDescending {
                            it.overall
                        }
                        .take(11)
                }

        val leagueName =
            repository.getLeagueName(
                match.leagueId ?: 0L
            )

        val homeClub = repository.getClubById(match.homeClubId)
        val awayClub = repository.getClubById(match.awayClubId)

        return MatchSession(
            match = match,
            homeLineup = homeLineup,
            awayLineup = awayLineup,
            homeClubName = homeClub?.name ?: "Home",
            awayClubName = awayClub?.name ?: "Away",
            homeShortName = homeClub?.shortName ?: "HME",
            awayShortName = awayClub?.shortName ?: "AWY",
            competitionName = leagueName,
            stadiumName = homeClub?.stadium ?: "Home Stadium",
            weather = "Clear",
            kickoffTime = "20:00"
        )
    }
}
