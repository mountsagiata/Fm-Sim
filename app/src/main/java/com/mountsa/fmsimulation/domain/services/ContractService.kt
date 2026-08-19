package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.core.enums.SquadRole
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ContractService @Inject constructor(
    private val repository: DataRepository
) {
    /**
     * Item 13: Advanced Contract System
     * Processes contract renewals and role changes.
     */
    suspend fun offerExtension(player: PlayerEntity, years: Int, newWage: Long, newRole: SquadRole): Boolean {
        val career = repository.getCareer().first() ?: return false
        val club = repository.getClubById(player.clubId) ?: return false

        // Basic AI logic for player accepting extension
        val marketWage = player.marketValue / 800
        val isAccepting = newWage >= marketWage * 0.9 && years > 0

        if (isAccepting) {
            val updatedPlayer = player.copy(
                contractUntil = player.contractUntil + years,
                wage = newWage,
                squadRole = newRole,
                morale = (player.morale + 10).coerceAtMost(100)
            )
            repository.updatePlayer(updatedPlayer)
            
            repository.addInbox(
                InboxEntity(
                    clubId = player.clubId,
                    sender = "Player Agent",
                    subject = "Contract Signed: ${player.shortName}",
                    message = "${player.name} has signed a new contract until ${updatedPlayer.contractUntil}. He is happy with his role as ${newRole.name}.",
                    category = InboxCategory.CONTRACT,
                    timestamp = career.currentDate
                )
            )
            return true
        }
        return false
    }

    suspend fun checkUnhappyContracts(clubId: Long) {
        val players = repository.getPlayersByClubSync(clubId)
        val career = repository.getCareer().first() ?: return
        
        players.forEach { player ->
            // Unhappy if star player but low wage
            val marketWage = player.marketValue / 800
            if ((player.squadRole == SquadRole.STAR || player.squadRole == SquadRole.IMPORTANT) && player.wage < marketWage * 0.6) {
                if (Random.nextFloat() < 0.1f) {
                    repository.addInbox(
                        InboxEntity(
                            clubId = clubId,
                            sender = "Player Agent",
                            subject = "Contract Concerns: ${player.shortName}",
                            message = "${player.name} feels his current wage of €${player.wage} does not reflect his importance to the team. He expects a raise.",
                            category = InboxCategory.CONTRACT,
                            timestamp = career.currentDate
                        )
                    )
                }
            }
        }
    }
}
