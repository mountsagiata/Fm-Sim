package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.data.local.entities.SaveCareerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PressService @Inject constructor(
    private val repository: DataRepository,
    private val moraleService: MoraleService,
    private val managerRatingService: ManagerRatingService
) {
    /**
     * Item 14: Manager Answer impact on Morale and Rating
     */
    suspend fun processPressAnswer(clubId: Long, moraleImpact: Int, ratingImpact: Int) {
        // 1. Apply Morale Impact to the squad
        val players = repository.getPlayersByClubSync(clubId)
        val updatedPlayers = players.map { 
            it.copy(morale = (it.morale + moraleImpact).coerceIn(0, 100))
        }
        repository.updatePlayers(updatedPlayers)

        // 2. Apply Manager Rating Impact
        val career = repository.getCareer().firstOrNull() ?: return
        val newRating = (career.managerRating + ratingImpact).coerceIn(0, 100)
        repository.saveCareer(career.copy(managerRating = newRating))
    }
}
