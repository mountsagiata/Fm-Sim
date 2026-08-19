package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_events"
)
data class CalendarEventEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clubId: Long,

    val eventDate: Long,

    val type: String,

    /*
    MATCH
    TRAINING
    REST
    TRANSFER
    BOARD
    MEDIA
    YOUTH
    RECOVERY
    */

    val title: String,

    val description: String = "",

    val relatedId: Long = -1L
)