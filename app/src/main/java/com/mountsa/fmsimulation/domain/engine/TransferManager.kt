package com.mountsa.fmsimulation.domain.engine

import com.mountsa.fmsimulation.core.enums.TransferStatus
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.local.entities.TransferOfferEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferManager @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun processTransfer(offer: TransferOfferEntity) {
        if (offer.status != TransferStatus.ACCEPTED) return

        val player = repository.getPlayerById(offer.playerId) ?: return
        val usedNumbers = repository.getPlayersByClubSync(offer.buyerClubId)
            .map { it.shirtNumber }
            .filter { it > 0 }
            .toSet()
        val newShirtNumber = (1..99).firstOrNull { it !in usedNumbers } ?: 0
        
        // Update player's club and contract details
        val updatedPlayer = player.copy(
            clubId = offer.buyerClubId,
            wage = offer.wageOffered,
            shirtNumber = newShirtNumber,
            startingIndex = -1 // Move to reserves of new club initially
        )
        
        repository.updatePlayer(updatedPlayer)
        
        // Update budgets
        val buyerClub = repository.getClubById(offer.buyerClubId)
        val sellerClub = repository.getClubById(offer.sellerClubId)
        
        buyerClub?.let {
            repository.updateClub(it.copy(budget = it.budget - offer.offerAmount))
        }
        sellerClub?.let {
            repository.updateClub(it.copy(budget = it.budget + offer.offerAmount))
        }
    }
}
