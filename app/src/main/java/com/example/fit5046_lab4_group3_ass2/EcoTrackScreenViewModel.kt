package com.example.fit5046_lab4_group3_ass2

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fit5046_lab4_group3_ass2.api.ItemsRepository
import com.example.fit5046_lab4_group3_ass2.api.MarketResponse
import com.example.fit5046_lab4_group3_ass2.room.DayUse
import com.example.fit5046_lab4_group3_ass2.room.DayUseRepository
import com.example.fit5046_lab4_group3_ass2.sensor.SensorDataProvider
import com.example.sensorslab.SensorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EcoTrackScreenViewModel(application: Application) : AndroidViewModel(application) {
    //API stuff
    private val apiRepository = ItemsRepository()
    val retrofitResponse = mutableStateOf(MarketResponse())
    fun customSearch() {
        viewModelScope.launch {
            try {
                retrofitResponse.value = apiRepository.customSearch()
                Log.i("Error", "NO ERROR???")
            } catch (e: Exception) {
                Log.i("Error", "Response failed", e)
            }
        }
    }

    // Sensor stuff
    private val sensorRepository = SensorRepository(SensorDataProvider(viewModelScope))

    private val _sensorData = MutableStateFlow("")
    val sensorData: StateFlow<String> = _sensorData.asStateFlow()

    init {
        viewModelScope.launch {
            //collect starts the flow, and for every value the flow emits,
            // it runs the block of code.
            sensorRepository.getSensorData().collect { data ->
                //_sensorData.value = data = updates StateFlow so the UI can react.
                //how update happens: ViewModel → _sensorData → sensorData → UI)
                _sensorData.value = data
            }
        }
    }

    // Room database stuff
    private val cRepository: DayUseRepository

    init {
        cRepository = DayUseRepository(application)
    }

    val allDayUses: Flow<List<DayUse>> = cRepository.allDayUses

    fun insertDayUse(dayUse: DayUse) = viewModelScope.launch(Dispatchers.IO) {
        cRepository.insert(dayUse)
    }

    fun updateDayUse(dayUse: DayUse) = viewModelScope.launch(Dispatchers.IO) {
        cRepository.update(dayUse)
    }

    fun deleteDayUse(dayUse: DayUse) = viewModelScope.launch(Dispatchers.IO) {
        cRepository.delete(dayUse)
    }

    // complicated functionality
    // this is for setting up the work to store records in the background
    fun storeARecord() {
        //print the records for testing:
        val records = cRepository.allDayUses
        viewModelScope.launch {
                // Trigger the flow and consume its elements using collect
                cRepository.allDayUses.collect { day ->
                    Log.e("DayUse", day.toString())
                }
            }
        val workRequest = PeriodicWorkRequestBuilder<StorageWorker>(
            15, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(application).enqueue(workRequest)
        return
    }
}
