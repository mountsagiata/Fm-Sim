package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertInbox(
        inbox: InboxEntity
    )

    @Update
    suspend fun updateInbox(inbox: InboxEntity)

    @Query(
        """
        SELECT * FROM inbox
        WHERE clubId = :clubId
        ORDER BY timestamp DESC
        """
    )
    fun getInbox(
        clubId: Long
    ): Flow<List<InboxEntity>>

    @Query(
        """
        UPDATE inbox
        SET isRead = 1
        WHERE id = :id
        """
    )
    suspend fun markAsRead(
        id: Long
    )

    @Query(
        """
        SELECT COUNT(*)
        FROM inbox
        WHERE
        clubId = :clubId
        AND isRead = 0
        """
    )
    fun unreadCount(
        clubId: Long
    ): Flow<Int>

    @Query("DELETE FROM inbox WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM inbox")
    suspend fun deleteAllInbox()
}
