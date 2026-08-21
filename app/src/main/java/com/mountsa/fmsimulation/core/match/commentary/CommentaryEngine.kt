package com.mountsa.fmsimulation.core.match.commentary

import com.mountsa.fmsimulation.core.match.event.MatchEvent
import kotlin.random.Random

object CommentaryEngine {

    fun generate(
        event: MatchEvent,
        playerName: String = "",
        teamName: String = "",
        playerIn: String = "",
        playerOut: String = "",
        minutesAdded: Int = 0
    ): String {

        // Perbaiki: panggil event.type (bukan event.type.name)
        val templates = CommentaryRepository.get(event.type.toString())

        if (templates.isEmpty()) {
            return ""
        }

        val template = templates.random(Random)

        return template
            .replace("{player}", playerName)
            .replace("{team}", teamName)
            .replace("{score}", "${event.scoreHome}-${event.scoreAway}")
            .replace("{player_in}", playerIn)
            .replace("{player_out}", playerOut)
            .replace("{minutes}", minutesAdded.toString())
    }
}