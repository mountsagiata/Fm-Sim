package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.ObjectivePriority

@Entity(
    tableName = "objectives",
    indices = [
        Index(value = ["clubId"])
    ]
)
data class ObjectiveEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clubId: Long,

    val title: String,

    val description: String,

    val targetValue: Int = 0,

    val currentProgress: Int = 0,

    val priority: ObjectivePriority = ObjectivePriority.MEDIUM,

    val reward: Long = 0L,

    val deadlineDate: Long,

    val completed: Boolean = false
)
