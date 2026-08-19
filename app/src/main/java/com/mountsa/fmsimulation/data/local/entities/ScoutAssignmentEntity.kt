package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.ScoutAssignmentType

@Entity(
    tableName = "scout_assignments",
    indices = [
        Index(value = ["scoutId"]),
        Index(value = ["targetId"]) // Can be NationId or LeagueId
    ]
)
data class ScoutAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scoutId: Long,
    val type: ScoutAssignmentType,
    val targetId: Long? = null, // Nation ID or League ID
    val position: String? = null, // If type is POSITION
    val minAge: Int = 15,
    val maxAge: Int = 50,
    val minPotential: Int = 0,
    val progress: Int = 0, // 0-100
    val startDate: Long,
    val endDate: Long? = null
)
