package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.AppMetadataEntity

@Dao
interface MetadataDao {
    @Query("SELECT * FROM app_metadata WHERE `key` = :key LIMIT 1")
    suspend fun getMetadata(key: String): AppMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: AppMetadataEntity)

    @Query("DELETE FROM app_metadata WHERE `key` = :key")
    suspend fun deleteMetadata(key: String)
}
