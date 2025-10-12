package com.example.sensorslab

import android.content.Context
import com.example.fit5046_lab4_group3_ass2.CsvRecordsObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class SensorDataProvider() {
    private val refreshIntervalMs: Long = 1000

    fun sensorDataFlow(): Flow<String> {
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
    }
}
