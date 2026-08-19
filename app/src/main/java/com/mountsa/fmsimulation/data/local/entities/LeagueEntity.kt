package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leagues",
    indices = [
        Index(value = ["nationId"])
    ]
)
data class LeagueEntity(

    @PrimaryKey
    val id: Long,

    val nationId: Long,

    val name: String,

    val shortName: String,

    val logoAsset: String = "",

    val division: Int = 1,

    val reputation: Int = 50,

    val transferBudgetMultiplier: Float = 1f
)
