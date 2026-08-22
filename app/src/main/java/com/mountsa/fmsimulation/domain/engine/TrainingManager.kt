package com.mountsa.fmsimulation.domain.engine

import com.mountsa.fmsimulation.core.enums.PlayerStatus
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TrainingManager @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun processDailyTraining(clubId: Long) {
        val players = repository.getPlayersByClubSync(clubId)
        if (players.isEmpty()) return

        val updatedPlayers = players.map { player ->
            if (player.status == PlayerStatus.INJURED) {
                return@map player.copy(fitness = (player.fitness + 2).coerceAtMost(100))
            }

            // Basic training logic
            val sharpnessGain = Random.nextInt(1, 4)
            val fatigueGain = Random.nextInt(1, 4)
            val recovery = if (player.fitness < 90) Random.nextInt(2, 5) else 1
            
            // Growth progress based on age
            val growthBonus = if (player.age < 23) Random.nextInt(5, 15) else Random.nextInt(1, 5)
            
            val updatedPlayer = player.copy(
                sharpness = (player.sharpness + sharpnessGain).coerceAtMost(100),
                fatigue = (player.fatigue + fatigueGain).coerceAtMost(100),
                fitness = (player.fitness + recovery - fatigueGain / 2).coerceIn(0, 100),
                growthProgress = player.growthProgress + growthBonus
            )

            if (updatedPlayer.growthProgress >= 100) {
                updatedPlayer.copy(
                    growthProgress = updatedPlayer.growthProgress - 100,
                    overall = (updatedPlayer.overall + 1).coerceAtMost(updatedPlayer.potential)
                )
            } else {
                updatedPlayer
            }
        }
        repository.updatePlayers(updatedPlayers)
    }
}
