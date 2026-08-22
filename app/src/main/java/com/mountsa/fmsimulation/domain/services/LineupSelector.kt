package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.PlayerStatus
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity

object LineupSelector {
    fun select(players: List<PlayerEntity>, count: Int = 11): List<PlayerEntity> {
        val available = players
            .filter { it.status == PlayerStatus.FIT && it.fitness > 45 }
            .sortedWith(compareByDescending<PlayerEntity> { it.overall }.thenByDescending { it.fitness })
            .toMutableList()
        if (available.size <= count) return available

        val selected = mutableListOf<PlayerEntity>()
        fun takeMatching(amount: Int, predicate: (PlayerEntity) -> Boolean) {
            repeat(amount) {
                available.firstOrNull(predicate)?.let { selected += it; available.remove(it) }
            }
        }

        takeMatching(1) { it.position == "GK" }
        takeMatching(4) { it.position in setOf("CB", "LB", "RB", "LWB", "RWB") }
        takeMatching(3) { it.position in setOf("CDM", "CM", "CAM", "LM", "RM") }
        takeMatching(3) { it.position in setOf("LW", "RW", "CF", "ST") }
        selected += available.take((count - selected.size).coerceAtLeast(0))
        return selected.take(count)
    }
}
