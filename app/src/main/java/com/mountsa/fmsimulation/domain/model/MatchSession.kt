package com.mountsa.fmsimulation.domain.model

import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity

data class MatchSession(
    val match: MatchEntity,
    val homeLineup: List<PlayerEntity>,
    val awayLineup: List<PlayerEntity>,
    val homeBench: List<PlayerEntity> = emptyList(),
    val awayBench: List<PlayerEntity> = emptyList(),
    val homeClubName: String,
    val awayClubName: String,
    val homeShortName: String,
    val awayShortName: String,
    val competitionName: String,
    val stadiumName: String,
    val weather: String = "Clear",
    val kickoffTime: String = "20:00"
)
