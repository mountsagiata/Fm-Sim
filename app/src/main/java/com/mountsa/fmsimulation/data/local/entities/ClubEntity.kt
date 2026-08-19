package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.ManagerPersonality
import com.mountsa.fmsimulation.core.enums.Mentality

@Entity(
    tableName = "clubs",
    indices = [
        Index(value = ["leagueId"]),
        Index(value = ["nationId"])
    ]
)
data class ClubEntity(
    @PrimaryKey val id: Long,
    val nationId: Long,
    val leagueId: Long,
    val name: String,
    val shortName: String,
    val logoAsset: String = "",
    val stadium: String = "",
    val foundedYear: Int = 1900,
    val budget: Long = 0L,
    val wageBudget: Long = 0L,
    val rivalClubId: Long? = null,
    val formation: String = "4-3-3",

    // Item 33: AI Manager Personality
    val managerPersonality: ManagerPersonality = ManagerPersonality.BALANCED,
    
    // Item 29: Youth Academy Depth
    val academyQuality: Int = 50, // 0-100
    val localNationBias: Int = 70, // Preference for local players in youth intake

    // Item 34: Dynamic World Simulation
    val boardSatisfaction: Int = 100, // 0-100
    val fanSatisfaction: Int = 100, // 0-100

    // Advanced Tactical System
    val mentality: Mentality = Mentality.BALANCED,
    val tempo: Int = 50,
    val pressing: Int = 50,
    val defensiveLine: Int = 50,
    val attackingWidth: Int = 50,
    val passingDirectness: Int = 50,
    val counterAttack: Boolean = true,
    val possessionStyle: Boolean = false,

    val overall: Int = 60,
    val attack: Int = 60,
    val midfield: Int = 60,
    val defense: Int = 60,
    val reputation: Int = 50,
    val playerCount: Int = 0,
    val managerId: Long = -1L
)
