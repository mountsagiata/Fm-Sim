package com.mountsa.fmsimulation.core.match.commentary

import com.mountsa.fmsimulation.core.match.event.MatchEvent
import kotlin.random.Random

object CommentaryEngine {

    fun generate(
        event: MatchEvent,
        playerName: String = "",
        teamName: String = ""
    ): String {

        val templates =
            CommentaryRepository
                .get(event.type.name)

        if (templates.isEmpty()) {
            return ""
        }

        val template =
            templates.random(Random)

        return template
            .replace("{player}", playerName)
            .replace("{team}", teamName)
            .replace(
                "{score}",
                "${event.scoreHome}-${event.scoreAway}"
            )
    }
}