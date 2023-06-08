package com.smd.cryptocurrency.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_table")
data class Crypto(
    @PrimaryKey val id: Int?,
    val symbol: String?,
    val logoUrl: String?,
    val price: Double?
)