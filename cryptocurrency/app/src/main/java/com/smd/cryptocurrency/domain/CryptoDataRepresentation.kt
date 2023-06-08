package com.smd.cryptocurrency.domain

data class CryptoDataRepresentation(
    val name: String?,
    val symbol: String?,
    val id: Int?,
    val quote: Map<String, CryptoDataQuoteRepresentation>?
)