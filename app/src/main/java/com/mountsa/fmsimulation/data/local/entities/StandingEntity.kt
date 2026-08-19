package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "standings",
    indices = [
        Index(value = ["leagueId"]),
        Index(value = ["clubId"])
    ]
)
data class StandingEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val leagueId: Long,

    val clubId: Long,

    val position: Int = 0,

    // =========================
    // MATCH RECORD
    // =========================

    val played: Int = 0,

    val wins: Int = 0,

    val draws: Int = 0,

    val losses: Int = 0,

    // =========================
    // GOALS
    // =========================

    val goalsFor: Int = 0,

    val goalsAgainst: Int = 0,

    val goalDifference: Int = 0,

    // =========================
    // POINTS
    // =========================

    val points: Int = 0,

    // =========================
    // FORM
    // contoh:
    // W D W L W
    // =========================

    val form: String = ""
)