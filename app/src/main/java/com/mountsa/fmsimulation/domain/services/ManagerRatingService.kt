package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.ObjectivePriority
import com.mountsa.fmsimulation.data.local.entities.ObjectiveEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ManagerRatingService @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun updateManagerRating(clubId: Long) {
        val career = repository.getCareer().first() ?: return
        // Explicitly define type to fix inference and forEach ambiguity
        val objectives: List<ObjectiveEntity> = repository.getObjectives(clubId).first()
        val club = repository.getClubById(clubId) ?: return

        var ratingChange = 0

        // 1. Objectives Impact
        objectives.forEach { obj ->
            if (obj.completed) {
                ratingChange += when (obj.priority) {
                    ObjectivePriority.HIGH, ObjectivePriority.CRITICAL -> 5
                    ObjectivePriority.MEDIUM -> 3
                    else -> 1
                }
            }
        }

        // 2. Performance Impact (Simulated)
        val standing = repository.getStandingByClub(club.leagueId, clubId)
        standing?.let {
            if (it.position <= 3) ratingChange += 2
            if (it.position >= 15) ratingChange -= 3
        }

        val newRating = (career.managerRating + ratingChange).coerceIn(0, 100)
        repository.saveCareer(career.copy(managerRating = newRating))
    }
}
