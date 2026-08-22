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
        ),
        Formation(
            name = "4-1-4-1",
            positions = listOf(
                Position("GK", "GK", 50f, 88f), Position("LB", "LB", 14f, 68f),
                Position("LCB", "CB", 37f, 72f), Position("RCB", "CB", 63f, 72f),
                Position("RB", "RB", 86f, 68f), Position("DM", "CDM", 50f, 56f),
                Position("LM", "LM", 15f, 40f), Position("LCM", "CM", 38f, 44f),
                Position("RCM", "CM", 62f, 44f), Position("RM", "RM", 85f, 40f),
                Position("ST", "ST", 50f, 17f)
            )
        ),
        Formation(
            name = "4-3-2-1",
            positions = listOf(
                Position("GK", "GK", 50f, 88f), Position("LB", "LB", 14f, 68f),
                Position("LCB", "CB", 37f, 72f), Position("RCB", "CB", 63f, 72f),
                Position("RB", "RB", 86f, 68f), Position("LCM", "CM", 30f, 50f),
                Position("DM", "CDM", 50f, 57f), Position("RCM", "CM", 70f, 50f),
                Position("LAM", "CAM", 36f, 31f), Position("RAM", "CAM", 64f, 31f),
                Position("ST", "ST", 50f, 14f)
            )
        ),
        Formation(
            name = "5-3-2",
            positions = listOf(
                Position("GK", "GK", 50f, 88f), Position("LWB", "LWB", 10f, 56f),
                Position("LCB", "CB", 28f, 72f), Position("CB", "CB", 50f, 75f),
                Position("RCB", "CB", 72f, 72f), Position("RWB", "RWB", 90f, 56f),
                Position("LCM", "CM", 30f, 43f), Position("DM", "CDM", 50f, 51f),
                Position("RCM", "CM", 70f, 43f), Position("LST", "ST", 37f, 18f),
                Position("RST", "ST", 63f, 18f)
            )
        ),
        Formation(
            name = "3-4-3",
            positions = listOf(
                Position("GK", "GK", 50f, 88f), Position("LCB", "CB", 27f, 72f),
                Position("CB", "CB", 50f, 76f), Position("RCB", "CB", 73f, 72f),
                Position("LM", "LM", 12f, 49f), Position("LCM", "CM", 40f, 51f),
                Position("RCM", "CM", 60f, 51f), Position("RM", "RM", 88f, 49f),
                Position("LW", "LW", 18f, 24f), Position("ST", "ST", 50f, 15f),
                Position("RW", "RW", 82f, 24f)
            )
        )
    )
}
