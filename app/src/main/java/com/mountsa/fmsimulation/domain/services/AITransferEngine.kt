package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.PlayerHappiness
import com.mountsa.fmsimulation.core.enums.TransferStatus
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.local.entities.TransferOfferEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AITransferEngine @Inject constructor(
    private val repository: DataRepository
) {
    /**
     * AI Clubs analyze their squad and decide whether to buy or sell players.
     *
     * Only a random sample of clubs is processed each day (instead of every club
     * in the database). Scanning every club's full squad every day was the main
     * cause of "Advancing to next day" feeling slow during transfer windows.
     */
    suspend fun processAIClubsTransfers() {
        val career = repository.getCareer().firstOrNull() ?: return
        val userClubId = career.selectedClubId
        val clubs = repository.getAllClubsSync()
            .filter { it.id != userClubId }
            .shuffled()
            .take(25)

        for (club in clubs) {
            val players = repository.getPlayersByClubSync(club.id)
            
            // Item 12: AI Logic - Buy weak position or replace aging player
            if (players.size < 20 || Random.nextFloat() < 0.08f) {
                val weakPosition = identifyWeakPosition(players)
                if (weakPosition != null && club.budget > 5_000_000) {
                    lookForPlayerToBuy(club, weakPosition)
                }
            }

            // AI Logic: Sell aging or unhappy players
            players.forEach { player ->
                if ((player.happiness == PlayerHappiness.UNHAPPY || player.age > 33) && Random.nextFloat() < 0.01f) {
                    // TODO: Implement logic to make player available for transfer (e.g. transfer list)
                }
            }
        }
    }

    private fun identifyWeakPosition(players: List<PlayerEntity>): String? {
        val positions = listOf("GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LW", "RW", "ST")
        val posScores = positions.associateWith { pos ->
            players.filter { it.position == pos }.maxOfOrNull { it.overall } ?: 0
        }
        
        return posScores.minByOrNull { it.value }?.key
    }

    private suspend fun lookForPlayerToBuy(club: ClubEntity, position: String) {
        val allClubs = repository.getAllClubsSync()
        val potentialSellers = allClubs.filter { it.id != club.id }

        if (potentialSellers.isEmpty()) return
        
        val randomSeller = potentialSellers.random()
        val targets = repository.getPlayersByClubSync(randomSeller.id)
            .filter { it.position == position && it.overall >= club.overall - 5 }

        if (targets.isNotEmpty()) {
            val target = targets.random()
            val offerAmount = (target.marketValue * Random.nextDouble(1.0, 1.4)).toLong()
            
            if (club.budget >= offerAmount) {
                repository.insertOffer(
                    TransferOfferEntity(
                        playerId = target.id,
                        buyerClubId = club.id,
                        sellerClubId = randomSeller.id,
                        offerAmount = offerAmount,
                        deadlineDate = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000),
                        status = TransferStatus.PENDING
                    )
                )
            }
        }
    }
}
