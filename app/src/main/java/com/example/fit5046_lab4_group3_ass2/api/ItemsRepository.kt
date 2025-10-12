package com.example.retrofittesting

class ItemsRepository {
    private val searchService = RetrofitObject.retrofitService
    suspend fun customSearch(): MarketResponse {
        /*val results = searchService.getMarketData(
            dateStart = "2025-10-04T20:00:00",
            dateEnd = "2025-10-04T20:05:00"
        )
        Log.d("ItemsRepository", "Results: $results")
        return results*/
        return searchService.getMarketData()
    }
}