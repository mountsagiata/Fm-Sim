// File: domain/services/FixtureGenerator.kt
package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.engine.MatchScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FixtureGenerator @Inject constructor(
    private val repository: DataRepository,
    private val matchScheduler: MatchScheduler
) {
    /**
     * Membangun rangkaian kalender pertandingan liga untuk pertama kalinya (saat seed data baru).
     * Logika utama didelegasikan ke MatchScheduler agar sinkron dengan pergantian tahun kompetisi otomatis.
     */
    suspend fun generateLeagueFixtures(leagueId: Long) {
        // Menggunakan delegasi MatchScheduler yang telah diperbaiki untuk menghindari bentrok logic dan redundansi tahun
        matchScheduler.scheduleSeason(leagueId)
    }
}