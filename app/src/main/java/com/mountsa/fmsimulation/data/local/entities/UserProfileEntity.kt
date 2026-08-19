package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarUri: String? = null,
    val title: String = "NEW MANAGER",
    val createdAt: Long = System.currentTimeMillis()
)
