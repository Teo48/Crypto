package com.smd.cryptocurrency.domain

import androidx.room.*

@Dao
interface CryptoDao {
    @Query("SELECT * FROM crypto_table")
    suspend fun getAllCryptos(): List<Crypto>

    @Query("SELECT symbol FROM crypto_table")
    suspend fun getAllCryptoSymbols(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrypto(crypto: Crypto)

    @Delete
    suspend fun deleteCrypto(crypto: Crypto)

    @Query("SELECT logoUrl FROM crypto_table WHERE symbol = :symbol")
    suspend fun getLogoUrlBySymbol(symbol: String): String

    @Query("SELECT price FROM crypto_table where symbol = :symbol")
    suspend fun getPriceBySymbol(symbol: String): Double
}