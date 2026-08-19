package com.mountsa.fmsimulation.ui.navigation

sealed class Screen {
    data object Splash : Screen()
    data object Intro : Screen()
    data object Profile : Screen()
    data object CareerSetup : Screen()
    data object Dashboard : Screen()

    // NEW MATCH FLOW
    data object MatchReveal : Screen()
    data object StartingLineup : Screen()
    data object MatchSimulation : Screen()
    data object MatchResult : Screen()
    data object PostMatch : Screen()
}
