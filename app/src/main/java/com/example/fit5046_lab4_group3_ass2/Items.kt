package com.example.retrofittesting

data class Items(
    val network_code: String,
    val metric: String,
    val unit: String,
    val interval: String,
    val date_start: String,
    val date_end: String,
    val groupings: List<String> = emptyList(),
    val results: List<Results> = emptyList(),
    val network_timezone_offset: String
) {
    class Results(
        val name: String,
        val date_start: String,
        val date_end: String,
        val columns: Map<String, Any> = emptyMap(),
        val data: List<List<Any>> = emptyList()
        ) {
    }
}
