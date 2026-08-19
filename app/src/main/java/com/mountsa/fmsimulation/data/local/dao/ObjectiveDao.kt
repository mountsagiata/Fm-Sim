package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.ObjectiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectiveDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertObjectives(
        objectives:
        List<ObjectiveEntity>
    )

    @Update
    suspend fun updateObjective(
        objective:
        ObjectiveEntity
    )

    @Query(
        """
        SELECT * FROM objectives
        WHERE clubId = :clubId
        """
    )
    fun getObjectives(
        clubId: Long
    ): Flow<List<ObjectiveEntity>>

    @Query(
        """
        DELETE FROM objectives
        WHERE clubId = :clubId
        """
    )
    suspend fun clearObjectives(
        clubId: Long
    )
}