package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.SeasonHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SeasonHistoryEntity)

    @Query("SELECT * FROM season_history WHERE clubId = :clubId ORDER BY season DESC")
    fun getHistoryByClub(clubId: Long): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT * FROM season_history WHERE leagueId = :leagueId ORDER BY season DESC")
    fun getHistoryByLeague(leagueId: Long): Flow<List<SeasonHistoryEntity>>
}
