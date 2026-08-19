package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "matches",
    indices = [
        Index(value = ["leagueId"]),
        Index(value = ["homeClubId"]),
        Index(value = ["awayClubId"]),
        Index(value = ["matchDate"])
    ]
)
data class MatchEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ====================================
    // COMPETITION
    // ====================================

    val leagueId: Long? = null,

    val cupId: Long? = null,

    val season: String = "2025/2026",

    val stage: String = "LEAGUE",

    /*
    LEAGUE
    GROUP
    ROUND16
    QF
    SF
    FINAL
    */

    // ====================================
    // CLUBS
    // ====================================

    val homeClubId: Long,

    val awayClubId: Long,

    // ====================================
    // DATE
    // ====================================

    val matchDate: Long,

    // ====================================
    // RESULT
    // ====================================

    val homeScore: Int = 0,

    val awayScore: Int = 0,

    // ====================================
    // STATUS
    // ====================================

    val isPlayed: Boolean = false,

    val isSimulated: Boolean = false,

    // ====================================
    // EVENTS
    // disimpan json string
    // ====================================

    val matchEvents: String = "",

    // ====================================
    // LINEUPS (Snapshot for immutability)
    // disimpan json string List<PlayerEntity> atau Player ID list
    // ====================================

    val homeLineupJson: String = "",

    val awayLineupJson: String = "",

    // ====================================
    // STATS (Item 19: Match Stats)
    // ====================================

    val possessionHome: Int = 50,

    val possessionAway: Int = 50,

    val shotsHome: Int = 0,

    val shotsAway: Int = 0,

    val shotsOnTargetHome: Int = 0,

    val shotsOnTargetAway: Int = 0,

    val foulsHome: Int = 0,

    val foulsAway: Int = 0,

    val cornersHome: Int = 0,

    val cornersAway: Int = 0,

    val yellowCardsHome: Int = 0,

    val yellowCardsAway: Int = 0,

    val redCardsHome: Int = 0,

    val redCardsAway: Int = 0,

    val xGHome: Float = 0.0f,

    val xGAway: Float = 0.0f,

    // Stats per player (JSON string) for Post Match Report (Item 19)
    val playerMatchStatsJson: String = "",

    // ====================================
    // MAN OF THE MATCH
    // ====================================

    val motmPlayerId: Long = -1L
)
