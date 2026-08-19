package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.PlayerHappiness
import com.mountsa.fmsimulation.core.enums.SquadRole
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MoraleService @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun updateDailyMorale(clubId: Long) {
        val players = repository.getPlayersByClubSync(clubId)
        val updatedPlayers = players.map { player ->
            var moraleChange = 0
            
            // 1. Playing time impact
            if (player.startingIndex == -1) {
                if (player.squadRole == SquadRole.STAR || player.squadRole == SquadRole.IMPORTANT) {
                    moraleChange -= 2 
                } else if (player.squadRole == SquadRole.PROSPECT && player.age < 21) {
                    moraleChange -= 1
                }
            } else {
                moraleChange += 1 
            }

            // 2. Wage vs Value impact (Item 14)
            val marketWage = player.marketValue / 1000
            if (player.wage < marketWage * 0.6) {
                moraleChange -= 1
            }

            // 3. Happiness state recovery/decay
            when (player.happiness) {
                PlayerHappiness.DELIGHTED -> moraleChange += 1
                PlayerHappiness.UNHAPPY -> moraleChange -= 1
                else -> {}
            }

            val newMorale = (player.morale + moraleChange + Random.nextInt(-1, 2)).coerceIn(0, 100)
            val newHappiness = calculateHappiness(newMorale)

            player.copy(morale = newMorale, happiness = newHappiness)
        }
        repository.updatePlayers(updatedPlayers)
    }

    suspend fun applyMatchResultImpact(clubId: Long, isWin: Boolean, isDraw: Boolean) {
        val players = repository.getPlayersByClubSync(clubId)
        val impact = if (isWin) 6 else if (isDraw) 1 else -6
        
        val updatedPlayers = players.map { 
            val newMorale = (it.morale + impact).coerceIn(0, 100)
            it.copy(morale = newMorale, happiness = calculateHappiness(newMorale))
        }
        repository.updatePlayers(updatedPlayers)
    }

    suspend fun applyTransferRejectImpact(playerId: Long) {
        val player = repository.getPlayerById(playerId) ?: return
        // Only impacts morale if the player is ambitious or the buying club is bigger
        // For now, simple drop
        val newMorale = (player.morale - 15).coerceIn(0, 100)
        repository.updatePlayer(player.copy(
            morale = newMorale, 
            happiness = calculateHappiness(newMorale)
        ))
    }

    suspend fun applyManagerAnswerImpact(clubId: Long, positive: Boolean) {
        val players = repository.getPlayersByClubSync(clubId)
        val impact = if (positive) 4 else -4
        val updatedPlayers = players.map { 
            val newMorale = (it.morale + impact).coerceIn(0, 100)
            it.copy(morale = newMorale, happiness = calculateHappiness(newMorale))
        }
        repository.updatePlayers(updatedPlayers)
    }

    private fun calculateHappiness(morale: Int): PlayerHappiness = when {
        morale > 85 -> PlayerHappiness.DELIGHTED
        morale > 70 -> PlayerHappiness.HAPPY
        morale > 45 -> PlayerHappiness.CONTENT
        morale > 25 -> PlayerHappiness.CONCERNED
        else -> PlayerHappiness.UNHAPPY
    }
}
