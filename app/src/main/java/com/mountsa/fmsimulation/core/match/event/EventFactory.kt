package com.mountsa.fmsimulation.core.match.event

import com.mountsa.fmsimulation.core.match.commentary.CommentaryEngine
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity

object EventFactory {

    fun create(
        minute: Int,
        type: EventType,
        category: EventCategory,
        homeScore: Int,
        awayScore: Int,
        player: PlayerEntity? = null,
        teamId: Long? = null,
        teamName: String = "",
        playerIn: PlayerEntity? = null,
        playerOut: PlayerEntity? = null,
        minutesAdded: Int = 0
    ): MatchEvent {
        val event = MatchEvent(
            minute = minute,
            type = type,
            category = category,
            playerId = player?.id,
            teamId = teamId ?: player?.clubId,
            playerName = player?.shortName ?: "",
            teamName = teamName,
            scoreHome = homeScore,
            scoreAway = awayScore
        )
        
        return event.copy(
            commentary = CommentaryEngine.generate(
                event = event,
                playerName = event.playerName,
                teamName = event.teamName,
                playerIn = playerIn?.shortName ?: "",
                playerOut = playerOut?.shortName ?: "",
                minutesAdded = minutesAdded
            )
        )
    }
}