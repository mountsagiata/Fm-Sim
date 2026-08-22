package com.mountsa.fmsimulation

import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.domain.services.LineupSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LineupSelectorTest {
    private fun player(id: Long, position: String, overall: Int = 70) = PlayerEntity(
        id = id,
        clubId = 1,
        nationId = 1,
        name = "Player $id",
        shortName = "P$id",
        position = position,
        overall = overall
    )

    @Test
    fun selectsBalancedElevenWithGoalkeeper() {
        val players = buildList {
            add(player(1, "GK", 65))
            repeat(5) { add(player((it + 2).toLong(), "CB", 70 + it)) }
            repeat(5) { add(player((it + 10).toLong(), "CM", 72 + it)) }
            repeat(5) { add(player((it + 20).toLong(), "ST", 74 + it)) }
        }

        val lineup = LineupSelector.select(players)

        assertEquals(11, lineup.size)
        assertTrue(lineup.any { it.position == "GK" })
        assertEquals(11, lineup.map { it.id }.distinct().size)
    }
}
