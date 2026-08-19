package com.mountsa.fmsimulation.core.match.event

data class MatchEvent(
    val minute: Int,
    val type: EventType,
    val category: EventCategory,
    val playerId: Long? = null,
    val playerName: String = "",
    val secondaryPlayerId: Long? = null,
    val secondaryPlayerName: String = "",
    val teamId: Long? = null,
    val teamName: String = "",
    val scoreHome: Int = 0,
    val scoreAway: Int = 0,
    val commentary: String = ""
)
