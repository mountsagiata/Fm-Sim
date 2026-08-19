package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "save_career"
)
data class SaveCareerEntity(

    @PrimaryKey
    val id: Long = 1L,

    val managerId: Long,

    val selectedClubId: Long,

    val selectedLeagueId: Long,

    val selectedNationId: Long,

    val season: String, // Changed from Long to String to support "2027/2028" format

    val currentDate: Long,

    val transferBudget: Long = 0L,

    val managerRating: Int = 75,

    val createdAt: Long,

    val updatedAt: Long
)
