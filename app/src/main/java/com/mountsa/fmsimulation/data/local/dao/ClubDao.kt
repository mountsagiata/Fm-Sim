package com.mountsa.fmsimulation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clubs: List<ClubEntity>)

    @Query("SELECT * FROM clubs ORDER BY overall DESC")
    fun getAllClubs(): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs ORDER BY overall DESC")
    suspend fun getAllClubsSync(): List<ClubEntity>

    @Query("SELECT * FROM clubs WHERE leagueId = :leagueId ORDER BY overall DESC")
    fun getClubsByLeague(leagueId: Long): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs WHERE id = :clubId")
    suspend fun getClubById(clubId: Long): ClubEntity?

    @Update
    suspend fun updateClub(club: ClubEntity)
}