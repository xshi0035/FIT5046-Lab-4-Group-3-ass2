package com.example.retrofittesting

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
class RetrofitViewModel : ViewModel() {
    private val repository = ItemsRepository()
    val retrofitResponse = mutableStateOf(MarketResponse())
    fun customSearch() {
        viewModelScope.launch {
            try {
                retrofitResponse.value = repository.customSearch()
                Log.i("Error", "NO ERROR???")
            } catch (e: Exception) {
                Log.i("Error", "Response failed", e)
            }
        }
    }
}