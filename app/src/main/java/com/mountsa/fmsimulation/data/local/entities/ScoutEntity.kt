package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scouts",
    indices = [
        Index(value = ["clubId"])
    ]
)
data class ScoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clubId: Long,
    val name: String,
    val nationalityId: Long,
    val judgmentAbility: Int = 50, // 0-100
    val judgmentPotential: Int = 50, // 0-100
    val adaptability: Int = 50,
    val assignmentId: Long? = null
)
