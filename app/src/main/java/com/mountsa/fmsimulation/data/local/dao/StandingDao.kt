package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.StandingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StandingDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertStandings(
        standings: List<StandingEntity>
    )

    @Update
    suspend fun updateStanding(
        standing: StandingEntity
    )

    @Query(
        """
        SELECT * FROM standings
        WHERE leagueId = :leagueId
        ORDER BY
        points DESC,
        goalDifference DESC,
        goalsFor DESC
        """
    )
    fun getLeagueStandings(
        leagueId: Long
    ): Flow<List<StandingEntity>>

    @Query(
        """
        DELETE FROM standings
        WHERE leagueId = :leagueId
        """
    )
    suspend fun clearLeague(
        leagueId: Long
    )

}