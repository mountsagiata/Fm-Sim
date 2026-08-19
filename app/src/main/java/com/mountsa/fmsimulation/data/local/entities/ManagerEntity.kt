package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.ManagerPersonality

@Entity(
    tableName = "managers",
    indices = [
        Index(value = ["clubId"]),
        Index(value = ["nationalityId"])
    ]
)
data class ManagerEntity(

    @PrimaryKey
    val id: Long,

    val name: String,

    val avatarUri: String = "",

    val nationalityId: Long,

    val clubId: Long = -1L,

    val reputation: Int = 50,

    val managerRating: Int = 75,

    val level: Int = 1,

    val experience: Int = 0,

    val salary: Long = 0L,

    // Item 33: AI Manager Personality
    val personality: ManagerPersonality = ManagerPersonality.BALANCED
)
