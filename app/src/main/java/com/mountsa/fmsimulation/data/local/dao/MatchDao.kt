package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches WHERE isPlayed = 0 ORDER BY matchDate ASC")
    fun getUpcomingMatches(): Flow<List<MatchEntity>>

    @Query("""
        SELECT * FROM matches 
        WHERE homeClubId = :clubId OR awayClubId = :clubId 
        ORDER BY matchDate ASC
    """)
    fun getMatchesForClub(clubId: Long): Flow<List<MatchEntity>>

    @Query("""
        SELECT * FROM matches 
        WHERE (homeClubId = :clubId OR awayClubId = :clubId) AND isPlayed = 0 
        ORDER BY matchDate ASC 
        LIMIT 1
    """)
    fun getNextMatchFlow(clubId: Long): Flow<MatchEntity?>

    @Query("""
        SELECT * FROM matches 
        WHERE (homeClubId = :clubId OR awayClubId = :clubId) AND isPlayed = 0 
        ORDER BY matchDate ASC 
        LIMIT 1
    """)
    suspend fun getNextMatch(clubId: Long): MatchEntity?

    @Query("SELECT * FROM matches WHERE matchDate = :date")
    suspend fun getMatchesForDate(date: Long): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Query("DELETE FROM matches WHERE leagueId = :leagueId")
    suspend fun deleteMatchesByLeague(leagueId: Long)

    @Query("DELETE FROM matches WHERE leagueId = :leagueId AND season = :season")
    suspend fun deleteMatchesByLeagueAndSeason(leagueId: Long, season: String)
}
