package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey
    val recordKey: String, // e.g., "MOST_GOALS_PLAYER", "LONGEST_UNBEATEN_STREAK"
    val clubId: Long,
    val holderName: String,
    val value: Float,
    val dateAchieved: Long,
    val description: String
)
