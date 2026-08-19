// domain/models/Formation.kt
package com.mountsa.fmsimulation.domain.models

data class Formation(
    val name: String,
    val positions: List<Position>
)

data class Position(
    val id: String,
    val name: String,
    val x: Float, // 0-100 persentase dari lebar
    val y: Float  // 0-100 persentase dari tinggi
)

object Formations {
    val DEFAULT_FORMATIONS = listOf(
        Formation(
            name = "4-3-3",
            positions = listOf(
                Position("GK", "GK", 50f, 85f),
                Position("LB", "LB", 15f, 65f),
                Position("CB", "CB", 35f, 70f),
                Position("CB", "CB", 65f, 70f),
                Position("RB", "RB", 85f, 65f),
                Position("CDM", "CDM", 50f, 55f),
                Position("CM", "CM", 35f, 45f),
                Position("CM", "CM", 65f, 45f),
                Position("LW", "LW", 15f, 25f),
                Position("ST", "ST", 50f, 15f),
                Position("RW", "RW", 85f, 25f)
            )
        ),
        Formation(
            name = "4-4-2",
            positions = listOf(
                Position("GK", "GK", 50f, 85f),
                Position("LB", "LB", 15f, 65f),
                Position("CB", "CB", 35f, 70f),
                Position("CB", "CB", 65f, 70f),
                Position("RB", "RB", 85f, 65f),
                Position("LM", "LM", 15f, 45f),
                Position("CM", "CM", 40f, 50f),
                Position("CM", "CM", 60f, 50f),
                Position("RM", "RM", 85f, 45f),
                Position("ST", "ST", 35f, 20f),
                Position("ST", "ST", 65f, 20f)
            )
        ),
        Formation(
            name = "3-5-2",
            positions = listOf(
                Position("GK", "GK", 50f, 85f),
                Position("CB", "CB", 50f, 70f),
                Position("CB", "CB", 25f, 75f),
                Position("CB", "CB", 75f, 75f),
                Position("LWB", "LWB", 15f, 55f),
                Position("RWB", "RWB", 85f, 55f),
                Position("CDM", "CDM", 50f, 55f),
                Position("CM", "CM", 35f, 40f),
                Position("CM", "CM", 65f, 40f),
                Position("ST", "ST", 35f, 20f),
                Position("ST", "ST", 65f, 20f)
            )
        ),
        Formation(
            name = "4-2-3-1",
            positions = listOf(
                Position("GK", "GK", 50f, 85f),
                Position("LB", "LB", 15f, 65f),
                Position("CB", "CB", 35f, 70f),
                Position("CB", "CB", 65f, 70f),
                Position("RB", "RB", 85f, 65f),
                Position("CDM", "CDM", 35f, 55f),
                Position("CDM", "CDM", 65f, 55f),
                Position("CAM", "CAM", 50f, 40f),
                Position("LW", "LW", 15f, 30f),
                Position("ST", "ST", 50f, 15f),
                Position("RW", "RW", 85f, 30f)
            )
        )
    )
}