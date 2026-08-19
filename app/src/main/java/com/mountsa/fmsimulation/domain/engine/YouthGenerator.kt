// File: domain/engine/YouthGenerator.kt
package com.mountsa.fmsimulation.domain.engine

import com.mountsa.fmsimulation.core.enums.PlayerStatus
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class YouthGenerator @Inject constructor(
    private val repository: DataRepository
) {
    private val firstNames = listOf("James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles")
    private val lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez")
    private val positions = listOf("GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LW", "RW", "ST")

    suspend fun generateYouthIntake(clubId: Long, count: Int = 3) {
        val club = repository.getClubById(clubId) ?: return
        val youthPlayers = mutableListOf<PlayerEntity>()

        repeat(count) {
            val firstName = firstNames.random()
            val lastName = lastNames.random()
            val fullName = "$firstName $lastName"

            val overall = Random.nextInt(45, 62)
            val potential = Random.nextInt(overall + 12, 94).coerceAtMost(99)

            // Kalkulasi finansial pemain muda yang realistis berbasis potensi
            val calculatedValue = (overall * 65_000L) + (potential * 120_000L)
            val calculatedWage = (overall * 150L) + (potential * 50L)

            // Menggunakan bitwise mask untuk menjamin ID positif dan unik
            val uniqueId = (Random.nextLong(1, Long.MAX_VALUE) xor System.nanoTime()) and Long.MAX_VALUE

            youthPlayers.add(
                PlayerEntity(
                    id = uniqueId,
                    clubId = clubId,
                    nationId = club.nationId,
                    name = fullName,
                    shortName = lastName,
                    age = 16,
                    position = positions.random(),
                    overall = overall,
                    potential = potential,
                    marketValue = calculatedValue,
                    wage = calculatedWage,
                    status = PlayerStatus.FIT,
                    fatigue = 0,
                    sharpness = 75,
                    startingIndex = -1 // Masuk ke dalam daftar cadangan/reserves awal
                )
            )
        }

        repository.insertPlayers(youthPlayers)
    }
}
