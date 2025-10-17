package com.example.sensorslab

import com.example.fit5046_lab4_group3_ass2.sensor.SensorDataProvider
import kotlinx.coroutines.flow.StateFlow

class SensorRepository(private val dataProvider: SensorDataProvider) {

    //fun getSensorData(): Flow<Int> = dataProvider.sensorDataFlow()

    fun getSensorData(): StateFlow<String> = dataProvider.sensorState

}
