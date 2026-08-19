package com.mountsa.fmsimulation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mountsa.fmsimulation.data.local.entities.LeagueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leagues: List<LeagueEntity>)

    @Query("SELECT * FROM leagues ORDER BY name ASC")
    fun getAllLeagues(): Flow<List<LeagueEntity>>

    @Query("SELECT * FROM leagues ORDER BY name ASC")
    suspend fun getAllLeaguesSync(): List<LeagueEntity>

    @Query("SELECT * FROM leagues WHERE nationId = :nationId ORDER BY reputation DESC")
    fun getLeaguesByNation(nationId: Long): Flow<List<LeagueEntity>>

    @Query("SELECT * FROM leagues WHERE id = :id")
    suspend fun getLeagueById(id: Long): LeagueEntity?
}
