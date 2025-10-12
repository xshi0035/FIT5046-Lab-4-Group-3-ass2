package com.example.sensorslab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {
    private val repository = SensorRepository(SensorDataProvider())

    private val _sensorData = MutableStateFlow("")
    val sensorData: StateFlow<String> = _sensorData.asStateFlow()

    init {
        viewModelScope.launch {
            //collect starts the flow, and for every value the flow emits,
            // it runs the block of code.
        repository.getSensorData().collect { data ->
            //_sensorData.value = data = updates StateFlow so the UI can react.
            //how update happens: ViewModel → _sensorData → sensorData → UI)
            _sensorData.value = data
        }
        }
    }
}
