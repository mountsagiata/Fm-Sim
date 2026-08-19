package com.mountsa.fmsimulation.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountsa.fmsimulation.core.enums.TransferStatus

@Entity(
    tableName = "transfer_offers",
    indices = [
        Index(value = ["playerId"]),
        Index(value = ["buyerClubId"]),
        Index(value = ["sellerClubId"])
    ]
)
data class TransferOfferEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val playerId: Long,

    val buyerClubId: Long,

    val sellerClubId: Long,

    val offerAmount: Long,

    val wageOffered: Long = 0L,

    val contractYears: Int = 3,

    val deadlineDate: Long,

    val status: TransferStatus = TransferStatus.PENDING
)
