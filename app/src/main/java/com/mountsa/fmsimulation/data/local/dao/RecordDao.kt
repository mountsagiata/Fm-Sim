package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)

    @Query("SELECT * FROM records WHERE clubId = :clubId")
    fun getRecordsByClub(clubId: Long): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE recordKey = :key AND clubId = :clubId")
    suspend fun getRecordByKey(key: String, clubId: Long): RecordEntity?
}
