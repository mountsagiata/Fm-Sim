package com.mountsa.fmsimulation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mountsa.fmsimulation.data.local.entities.NationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nations: List<NationEntity>)

    @Query("SELECT * FROM nations ORDER BY name ASC")
    fun getAllNations(): Flow<List<NationEntity>>

    @Query("SELECT * FROM nations ORDER BY name ASC")
    suspend fun getAllNationsSync(): List<NationEntity>

    @Query("SELECT * FROM nations WHERE id = :id")
    suspend fun getNationById(id: Long): NationEntity?
}
