// File: domain/services/TransferNegotiationService.kt
package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.TransferOfferEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TransferNegotiationService @Inject constructor(
    private val repository: DataRepository
) {
    suspend fun evaluateOffer(offer: TransferOfferEntity): String {
        val player = repository.getPlayerById(offer.playerId) ?: return "REJECTED"
        val sellerClub = repository.getClubById(offer.sellerClubId) ?: return "REJECTED"

        val marketValue = player.marketValue
        if (marketValue <= 0L) return "REJECTED"

        // Faktor Reputasi: Semakin tinggi reputasi klub, semakin enggan mereka menjual murah (Multiplier beban up to 1.4x)
        val reputationPremium = 1.0f + (sellerClub.reputation / 250f)
        val adjustedTargetValue = marketValue * reputationPremium
        val offerRatio = offer.offerAmount.toFloat() / adjustedTargetValue

        return when {
            offerRatio >= 1.25f -> "ACCEPTED"
            offerRatio >= 0.95f && Random.nextFloat() > 0.4f -> "ACCEPTED"
            offerRatio >= 0.75f -> "NEGOTIATING"
            else -> "REJECTED"
        }
    }
}