package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.Mentality
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class TacticEngine @Inject constructor() {

    fun calculateTeamChemistry(players: List<PlayerEntity>): Int {
        if (players.isEmpty()) return 0
        val XI = players.filter { it.startingIndex in 0..10 }
        if (XI.isEmpty()) return 0

        var score = 0
        XI.forEach { p ->
            val posFit = isPositionFit(p.startingIndex, p.position)
            score += if (posFit) 7 else 2
        }

        val natGroups = XI.groupBy { it.nationId }
        natGroups.forEach { (_, g) -> if (g.size >= 2) score += (g.size * 2) }

        return score.coerceIn(10, 100)
    }

    private fun isPositionFit(index: Int, pos: String): Boolean = when (index) {
        0 -> pos == "GK"
        1, 4 -> pos in listOf("RB", "LB", "RWB", "LWB", "CB")
        2, 3 -> pos == "CB"
        5 -> pos in listOf("CDM", "CM")
        6, 7 -> pos in listOf("CM", "CAM", "LM", "RM")
        10 -> pos in listOf("CAM", "ST", "CF")
        8, 9 -> pos in listOf("RW", "LW", "ST", "CF")
        else -> false
    }

    fun calculateTacticImpact(club: ClubEntity): TacticImpact {
        var att = 1.0f; var mid = 1.0f; var def = 1.0f

        when (club.mentality) {
            Mentality.ATTACKING -> { att += 0.15f; def -= 0.10f }
            Mentality.POSITIVE -> { att += 0.08f; def -= 0.04f }
            Mentality.DEFENSIVE -> { att -= 0.10f; def += 0.15f }
            Mentality.CAUTIOUS -> { att -= 0.04f; def += 0.08f }
            Mentality.BALANCED -> { /* No change */ }
        }

        val tempo = (club.tempo - 50) / 100f
        att += tempo * 0.1f

        val pressing = (club.pressing - 50) / 100f
        def += pressing * 0.12f 
        mid += pressing * 0.05f

        if (club.possessionStyle) mid += 0.1f
        if (club.counterAttack) att += 0.1f

        return TacticImpact(att, mid, def)
    }

    fun calculateRoleImpact(players: List<PlayerEntity>, roles: Map<Long, String>): TacticImpact {
        var att = 1f
        var mid = 1f
        var def = 1f
        players.forEach { player ->
            when (roles[player.id]) {
                "Finisher", "Poacher", "Inside Forward" -> att += .012f
                "Target Man", "Pressing Forward", "Winger" -> att += .007f
                "Advanced Playmaker", "Deep Lying Playmaker", "Mezzala" -> mid += .012f
                "Box to Box", "Ball Winning Midfielder" -> {
                    mid += .007f
                    def += .005f
                }
                "Ball Playing Defender", "Sweeper Keeper" -> mid += .006f
                "Central Defender", "Stopper", "Cover", "Full Back" -> def += .012f
                "Wing Back" -> {
                    att += .005f
                    def += .006f
                }
                "Goalkeeper" -> def += .008f
            }
        }
        return TacticImpact(att.coerceAtMost(1.12f), mid.coerceAtMost(1.12f), def.coerceAtMost(1.12f))
    }

    data class TacticImpact(val att: Float, val mid: Float, val def: Float)
}
