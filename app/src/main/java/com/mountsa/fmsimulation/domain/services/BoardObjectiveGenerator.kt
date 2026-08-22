// File: domain/services/BoardObjectiveGenerator.kt
package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.ObjectivePriority
import com.mountsa.fmsimulation.data.local.entities.ObjectiveEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class BoardObjectiveGenerator @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun generateSeasonObjectives(clubId: Long) {
        val club = repository.getClubById(clubId) ?: return
        val career = repository.getCareer().first() ?: return
        val leagueName = repository.getLeagueName(club.leagueId)

        // Ekstrak tahun akhir kompetisi dari format string "2027/2028" -> 2028
        val currentSeasonName = career.season
        val endYear = currentSeasonName.split("/").lastOrNull()?.toIntOrNull() ?: 2028

        val objectives = mutableListOf<ObjectiveEntity>()
        val calendar = Calendar.getInstance()

        // Target penyelesaian adalah akhir musim kompetisi berjalan (30 Mei tahun berjalan)
        calendar.set(endYear, Calendar.MAY, 30, 23, 59, 59)
        val deadline = calendar.timeInMillis

        // 1. Obyektif Posisi Klasemen Akhir Liga (Sesuai Kekuatan Squad)
        val targetPosition = when {
            club.overall > 85 -> 1  // Target Juara
            club.overall > 80 -> 4  // Zona Champions
            club.overall > 75 -> 7  // Zona Eropa
            club.overall > 70 -> 12 // Papan Tengah
            else -> 17              // Bertahan dari Degradasi
        }

        objectives.add(ObjectiveEntity(
            clubId = clubId,
            title = if (targetPosition == 1) "Win $leagueName" else "$leagueName: Top $targetPosition",
            description = if (targetPosition == 1) "Win the $leagueName title." else "Finish the season in position $targetPosition or higher.",
            targetValue = targetPosition,
            priority = ObjectivePriority.HIGH,
            deadlineDate = deadline
        ))

        // 2. Obyektif Manajemen Finansial Klub
        val targetBudgetInMillions = (club.budget / 2_000_000).toInt()
        objectives.add(ObjectiveEntity(
            clubId = clubId,
            title = "Financial Profit",
            description = "Maintain a transfer budget of at least €${(club.budget / 1_000_000) / 2}M.",
            targetValue = if (targetBudgetInMillions > 0) targetBudgetInMillions else 1,
            priority = ObjectivePriority.MEDIUM,
            deadlineDate = deadline
        ))

        // 3. Obyektif Pengembangan Pemain Muda (Youth Development)
        objectives.add(ObjectiveEntity(
            clubId = clubId,
            title = "Youth Development",
            description = "Give at least 5 appearances to players under 21 years old.",
            targetValue = 5,
            priority = ObjectivePriority.LOW,
            deadlineDate = deadline
        ))

        // Hapus objektif lama sebelum memasukkan objektif musim baru agar tidak menumpuk
        repository.clearObjectives(clubId)
        repository.insertObjectives(objectives)
    }
}
