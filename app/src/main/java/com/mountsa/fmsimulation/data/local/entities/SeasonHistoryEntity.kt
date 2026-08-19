package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "season_history")
data class SeasonHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clubId: Long,
    val season: String,
    val leagueId: Long,
    val position: Int,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val points: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val trophy: String = "", // e.g., "League Winner", "Cup Winner"
    val topScorer: String = "",
    val topScorerGoals: Int = 0,
    val bestPlayer: String = "",
    val bestPlayerRating: Float = 0f
)
