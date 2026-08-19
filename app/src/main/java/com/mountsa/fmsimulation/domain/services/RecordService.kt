package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.local.entities.RecordEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordService @Inject constructor(
    private val repository: DataRepository
) {
    /**
     * Item 25: Check and update club records after each match.
     */
    suspend fun updateMatchRecords(match: MatchEntity) {
        val homeClubId = match.homeClubId
        val awayClubId = match.awayClubId

        // 1. Biggest Win Record
        checkBiggestWin(homeClubId, match.homeScore, match.awayScore)
        checkBiggestWin(awayClubId, match.awayScore, match.homeScore)

        // 2. Goal Scorer Records
        val players = repository.getPlayersByClubSync(homeClubId) + repository.getPlayersByClubSync(awayClubId)
        players.forEach { player ->
            checkMostGoalsRecord(player)
        }
    }

    private suspend fun checkBiggestWin(clubId: Long, scoreFor: Int, scoreAgainst: Int) {
        val margin = (scoreFor - scoreAgainst).toFloat()
        if (margin <= 0) return

        val currentRecord = repository.getRecordByKey("BIGGEST_WIN", clubId)
        if (currentRecord == null || margin > currentRecord.value) {
            val clubName = repository.getClubById(clubId)?.name ?: "Club"
            repository.insertRecord(
                RecordEntity(
                    recordKey = "BIGGEST_WIN",
                    clubId = clubId,
                    holderName = clubName,
                    value = margin,
                    dateAchieved = System.currentTimeMillis(),
                    description = "Biggest victory in club history: $scoreFor-$scoreAgainst"
                )
            )
        }
    }

    private suspend fun checkMostGoalsRecord(player: PlayerEntity) {
        val currentRecord = repository.getRecordByKey("MOST_GOALS_PLAYER", player.clubId)
        if (currentRecord == null || player.goals.toFloat() > currentRecord.value) {
            repository.insertRecord(
                RecordEntity(
                    recordKey = "MOST_GOALS_PLAYER",
                    clubId = player.clubId,
                    holderName = player.name,
                    value = player.goals.toFloat(),
                    dateAchieved = System.currentTimeMillis(),
                    description = "Most goals scored by a single player: ${player.goals}"
                )
            )
        }
    }
}
