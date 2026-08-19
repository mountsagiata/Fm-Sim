package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.TransferOfferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferOfferDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertOffer(
        offer:
        TransferOfferEntity
    ): Long

    @Update
    suspend fun updateOffer(
        offer:
        TransferOfferEntity
    )

    @Query(
        """
        SELECT * FROM transfer_offers
        WHERE sellerClubId = :clubId
        ORDER BY deadlineDate ASC
        """
    )
    fun getOffersForClub(
        clubId: Long
    ): Flow<List<TransferOfferEntity>>
}