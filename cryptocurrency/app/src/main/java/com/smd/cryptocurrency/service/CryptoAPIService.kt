package com.smd.cryptocurrency.service

import com.smd.cryptocurrency.domain.CryptoApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CryptoAPIService {
    @GET("v1/cryptocurrency/listings/latest")
    fun getLatestListings(@Header("X-CMC_PRO_API_KEY") apiKey: String,
                          @Query("limit") limit: Int,
                          @Query("convert") convert: String
    ): Call<CryptoApiResponse>
}