package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "nations"
)
data class NationEntity(

    @PrimaryKey
    val id: Long,

    val name: String,

    val shortName: String,

    val flagAsset: String = "",

    val continent: String = "",

    val fifaRanking: Int = 0,

    val reputation: Int = 50
)