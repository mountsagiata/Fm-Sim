package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.SaveCareerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveCareerDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun saveCareer(
        career:
        SaveCareerEntity
    )

    @Query(
        """
        SELECT * FROM save_career
        LIMIT 1
        """
    )
    fun getCareer():
            Flow<SaveCareerEntity?>

    @Query("DELETE FROM save_career")
    suspend fun deleteCareer()
}