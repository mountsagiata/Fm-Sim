package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.ManagerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagerDao {
    @Query("SELECT * FROM managers")
    fun getAllManagers(): Flow<List<ManagerEntity>>

    @Query("SELECT * FROM managers WHERE id = :id")
    suspend fun getManagerById(id: Long): ManagerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManager(manager: ManagerEntity): Long

    @Update
    suspend fun updateManager(manager: ManagerEntity)

    @Delete
    suspend fun deleteManager(manager: ManagerEntity)

    @Query("DELETE FROM managers")
    suspend fun deleteAllManagers()
}
