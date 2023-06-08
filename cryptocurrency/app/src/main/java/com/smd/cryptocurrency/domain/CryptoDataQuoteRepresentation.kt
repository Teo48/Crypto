package com.smd.cryptocurrency.domain

data class CryptoDataQuoteRepresentation(
    val price: Double?,
    val volume_24h: Double?,
    val percent_change_1h: Double?,
    val percent_change_24h: Double?,
    val percent_change_7d: Double?
)