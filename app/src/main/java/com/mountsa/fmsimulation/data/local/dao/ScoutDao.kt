package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.ScoutAssignmentEntity
import com.mountsa.fmsimulation.data.local.entities.ScoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoutDao {
    @Query("SELECT * FROM scouts WHERE clubId = :clubId")
    fun getScoutsByClub(clubId: Long): Flow<List<ScoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScout(scout: ScoutEntity): Long

    @Update
    suspend fun updateScout(scout: ScoutEntity)

    @Query("SELECT * FROM scout_assignments WHERE scoutId = :scoutId")
    fun getAssignmentsForScout(scoutId: Long): Flow<List<ScoutAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: ScoutAssignmentEntity): Long

    @Query("DELETE FROM scout_assignments WHERE id = :assignmentId")
    suspend fun deleteAssignment(assignmentId: Long)
}
