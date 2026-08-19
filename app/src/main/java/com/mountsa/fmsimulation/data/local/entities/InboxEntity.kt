package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.InboxCategory

@Entity(
    tableName = "inbox",
    indices = [
        Index(value = ["clubId"])
    ]
)
data class InboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clubId: Long,
    val sender: String,
    val subject: String,
    val message: String,
    val category: InboxCategory,
    val actionData: String = "", // JSON string for interactive options (Press answers, etc.)
    val timestamp: Long,
    val isRead: Boolean = false,
    val isActioned: Boolean = false
)
