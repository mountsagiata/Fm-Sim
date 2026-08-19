package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.PlayerHappiness
import com.mountsa.fmsimulation.core.enums.PlayerPersonality
import com.mountsa.fmsimulation.core.enums.PlayerStatus
import com.mountsa.fmsimulation.core.enums.SquadRole

@Entity(
    tableName = "players",
    indices = [
        Index(value = ["clubId"]),
        Index(value = ["nationId"])
    ]
)
data class PlayerEntity(
    @PrimaryKey val id: Long,
    val clubId: Long,
    val nationId: Long,
    val name: String,
    val shortName: String,
    val shirtNumber: Int = 0,
    val age: Int = 18,
    val dob: String = "",
    val height: Int = 175,
    val weight: Int = 70,
    val preferredFoot: String = "Right",
    val position: String = "CM",
    val secondaryPosition: String = "",
    val overall: Int = 50,
    val potential: Int = 70,
    
    // Item 28/29: Hidden potential (only visible to scouts or fully scouted)
    val hiddenPotential: Int = 70,
    val scoutingKnowledge: Int = 0, // 0-100%
    
    // Core Attributes
    val pace: Int = 50,
    val shooting: Int = 50,
    val passing: Int = 50,
    val dribbling: Int = 50,
    val defending: Int = 50,
    val physical: Int = 50,
    val finishing: Int = 50,
    val volleys: Int = 50,
    val crossing: Int = 50,
    val headingAccuracy: Int = 50,
    val shortPassing: Int = 50,
    val skillDribbling: Int = 50,
    val curve: Int = 50,
    val fkAccuracy: Int = 50,
    val ballControl: Int = 50,
    val longPassing: Int = 50,
    val agility: Int = 50,
    val sprintSpeed: Int = 50,
    val reactions: Int = 50,
    val acceleration: Int = 50,
    val balance: Int = 50,
    val shotPower: Int = 50,
    val jumping: Int = 50,
    val stamina: Int = 50,
    val strength: Int = 50,
    val longShots: Int = 50,
    val aggression: Int = 50,
    val interceptions: Int = 50,
    val positioning: Int = 50,
    val vision: Int = 50,
    val penalties: Int = 50,
    val composure: Int = 50,
    val markingAwareness: Int = 50,
    val standingTackle: Int = 50,
    val slidingTackle: Int = 50,
    val gkDiving: Int = 50,
    val gkHandling: Int = 50,
    val gkKicking: Int = 50,
    val gkPositioning: Int = 50,
    val gkReflexes: Int = 50,
    
    // Contract & Role
    val marketValue: Long = 0L,
    val wage: Long = 0L,
    val contractUntil: Int = 2025,
    val releaseClause: Long = 0L,
    val squadRole: SquadRole = SquadRole.SQUAD_PLAYER,
    
    // Stats
    val goals: Int = 0,
    val assists: Int = 0,
    val appearances: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val averageRating: Float = 6.0f,
    
    // Dynamic States
    val status: PlayerStatus = PlayerStatus.FIT,
    val fitness: Int = 100,
    val fatigue: Int = 0,
    val sharpness: Int = 50,
    val morale: Int = 75, // 0-100
    val happiness: PlayerHappiness = PlayerHappiness.HAPPY,
    val personality: PlayerPersonality = PlayerPersonality.BALANCED,
    val form: Int = 50,

    val startingIndex: Int = -1,
    val growthProgress: Int = 0,
    val injuryName: String = "",
    val injuryDaysRemaining: Int = 0,
    val suspensionGamesRemaining: Int = 0,
    val avatarAsset: String = "",
    val flagAsset: String = ""
)
