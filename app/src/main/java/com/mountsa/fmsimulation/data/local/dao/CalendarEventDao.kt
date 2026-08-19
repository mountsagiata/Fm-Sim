package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Query("SELECT * FROM calendar_events WHERE clubId = :clubId ORDER BY eventDate ASC")
    fun getCalendar(clubId: Long): Flow<List<CalendarEventEntity>>

    @Query("DELETE FROM calendar_events WHERE clubId = :clubId")
    suspend fun deleteEventsByClub(clubId: Long)

    @Query("DELETE FROM calendar_events WHERE clubId = :clubId AND type = :type")
    suspend fun deleteEventsByType(clubId: Long, type: String)
}
