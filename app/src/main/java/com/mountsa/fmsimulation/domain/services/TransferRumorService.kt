package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TransferRumorService @Inject constructor(
    private val repository: DataRepository
) {
    private val rumorTemplates = listOf(
        "is being linked with a move to",
        "is reportedly unhappy and looking for a way out to",
        "has caught the eye of scouts from",
        "is the top target for",
        "could be set for a sensational switch to"
    )

    suspend fun generateRumors(clubId: Long) {
        if (Random.nextFloat() > 0.2f) return // 20% chance of a rumor day

        val allClubs = repository.getAllClubsSync()
        val allPlayers = repository.getPlayersByClubSync(clubId) // Just rumors about our players for now

        if (allPlayers.isEmpty() || allClubs.size < 2) return

        val player = allPlayers.random()
        val interestedClub = allClubs.filter { it.id != clubId }.random()
        val template = rumorTemplates.random()

        repository.addInbox(
            InboxEntity(
                clubId = clubId,
                sender = "Transfer Insider",
                subject = "Transfer Rumor: ${player.shortName}",
                message = "${player.name} $template ${interestedClub.name}. The fee is rumored to be around €${(player.marketValue * Random.nextDouble(0.9, 1.4) / 1_000_000).toInt()}M.",
                category = InboxCategory.TRANSFER,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
