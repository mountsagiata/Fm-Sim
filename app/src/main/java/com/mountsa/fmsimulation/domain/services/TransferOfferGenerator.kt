package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.TransferStatus
import com.mountsa.fmsimulation.data.local.entities.TransferOfferEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TransferOfferGenerator @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun generateRandomOffers(playerClubId: Long) {
        val players = repository.getPlayersByClubSync(playerClubId)
        if (players.isEmpty()) return

        // 5% chance to get an offer for a player each day
        if (Random.nextFloat() < 0.05f) {
            val player = players.random()
            val allClubs = repository.getAllClubsSync() // Assuming this exists or I'll use a subset
            val potentialBuyers = allClubs.filter { it.id != playerClubId }
            
            if (potentialBuyers.isNotEmpty()) {
                val buyer = potentialBuyers.random()
                val offerAmount = (player.marketValue * Random.nextDouble(0.8, 1.5)).toLong()
                
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 3) // 3 days to decide

                repository.insertOffer(
                    TransferOfferEntity(
                        playerId = player.id,
                        buyerClubId = buyer.id,
                        sellerClubId = playerClubId,
                        offerAmount = offerAmount,
                        deadlineDate = calendar.timeInMillis,
                        status = TransferStatus.PENDING
                    )
                )
            }
        }
    }
}
