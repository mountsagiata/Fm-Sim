package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.PlayerStatus
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class InjuryService @Inject constructor(
    private val repository: DataRepository
) {
    private val injuries = listOf(
        "Pulled Hamstring" to 14,
        "Ankle Sprain" to 21,
        "Knee Micro-fracture" to 60,
        "Bruised Ribs" to 7,
        "Gashed Leg" to 5,
        "Torn ACL" to 180,
        "Dead Leg" to 3
    )

    suspend fun processDailyInjuries(clubId: Long) {
        val players = repository.getPlayersByClubSync(clubId)
        if (players.isEmpty()) return

        val updatedPlayers = players.map { player ->
            if (player.status == PlayerStatus.INJURED) {
                val remaining = player.injuryDaysRemaining - 1
                if (remaining <= 0) {
                    player.copy(
                        status = PlayerStatus.FIT,
                        injuryName = "",
                        injuryDaysRemaining = 0,
                        sharpness = (player.sharpness - 15).coerceAtLeast(30)
                    )
                } else {
                    player.copy(injuryDaysRemaining = remaining)
                }
            } else {
                val fatigueFloat = player.fatigue.toFloat()
                val injuryChance = 0.0005f + ((fatigueFloat * fatigueFloat) / 150_000f)
                if (Random.nextFloat() < injuryChance) {
                    val injury = injuries.random()
                    val fatiguePenalty = (player.fatigue / 20).coerceAtMost(10)
                    val baseDuration = injury.second + Random.nextInt(-2, 5) + fatiguePenalty

                    player.copy(
                        status = PlayerStatus.INJURED,
                        injuryName = injury.first,
                        injuryDaysRemaining = baseDuration.coerceAtLeast(1)
                    )
                } else {
                    player
                }
            }
        }

        repository.updatePlayers(updatedPlayers)
    }
}
