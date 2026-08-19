package com.mountsa.fmsimulation.domain.engine

import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubManager @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun updateClubOverall(clubId: Long) {
        val club = repository.getClubById(clubId) ?: return
        val players = repository.getPlayersByClubSync(clubId)
        if (players.isEmpty()) return

        // Take top 20 players to represent the squad strength
        val topPlayers = players.sortedByDescending { it.overall }.take(20)
        
        val avgOverall = topPlayers.map { it.overall }.average().toInt()
        
        // Calculate sectoral ratings based on player positions
        val defenders = players.filter { it.position in listOf("GK", "CB", "RB", "LB", "RWB", "LWB") }
        val midfielders = players.filter { it.position in listOf("CDM", "CM", "CAM", "LM", "RM") }
        val attackers = players.filter { it.position in listOf("ST", "CF", "RW", "LW") }

        val defRating = if (defenders.isNotEmpty()) defenders.sortedByDescending { it.overall }.take(5).map { it.overall }.average().toInt() else avgOverall
        val midRating = if (midfielders.isNotEmpty()) midfielders.sortedByDescending { it.overall }.take(5).map { it.overall }.average().toInt() else avgOverall
        val attRating = if (attackers.isNotEmpty()) attackers.sortedByDescending { it.overall }.take(3).map { it.overall }.average().toInt() else avgOverall

        val updatedClub = club.copy(
            overall = avgOverall,
            attack = attRating,
            midfield = midRating,
            defense = defRating,
            playerCount = players.size
        )

        repository.updateClub(updatedClub)
    }

    suspend fun updateAllClubsOverall() {
        val clubs = repository.getAllClubsSync()
        clubs.forEach { updateClubOverall(it.id) }
    }
}
