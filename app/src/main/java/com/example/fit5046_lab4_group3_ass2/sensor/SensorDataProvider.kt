package com.example.fit5046_lab4_group3_ass2.sensor

import com.example.fit5046_lab4_group3_ass2.CsvRecordsObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class SensorDataProvider(
    private val scope: CoroutineScope
) {
    private val refreshIntervalMs: Long = 1000

    /*fun sensorDataFlow(): Flow<String> {
        val csv_records = CsvRecordsObject.getRecords()
        var i = 0
        val tempFlow: Flow<String> = flow {
            while (true) {
                emit(csv_records!![i].Global_active_power)
                delay(refreshIntervalMs*60) // update every minute, like the actual data does. causes horrible delay when opening app???
                if (i < 260639)
                    i++
                else
                    i = 0
            }
        }
        return tempFlow
    }*/
    private fun tempFlow(): Flow<String> = flow {
        val csv_records = CsvRecordsObject.getRecords()
        var i = 0
        while (true) {
            emit(csv_records!![i].Global_active_power)
            delay(refreshIntervalMs) // update every minute, like the actual data does. causes horrible delay when opening app???
            if (i < 2881)
                i++
            else
                i = 0
        }
    }

    val sensorState: StateFlow<String> = tempFlow()
        .stateIn(scope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = "0")
}
