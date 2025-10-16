package com.example.fit5046_lab4_group3_ass2.api

import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("v4/market/network/NEM")
    suspend fun getMarketData(
        @Query("metrics") metrics: String = "price",
        @Query("interval") interval: String = "5m",
        //@Query("date_start") dateStart: String,
        //@Query("date_end") dateEnd: String,
        @Query("primary_grouping") primaryGrouping: String = "network"
    ): MarketResponse
}