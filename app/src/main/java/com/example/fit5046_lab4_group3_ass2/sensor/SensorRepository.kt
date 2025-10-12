package com.example.sensorslab

import kotlinx.coroutines.flow.Flow

class SensorRepository(private val dataProvider: SensorDataProvider) {

    //fun getSensorData(): Flow<Int> = dataProvider.sensorDataFlow()

    fun getSensorData(): Flow<String> {
        return dataProvider.sensorDataFlow()
    }

}
