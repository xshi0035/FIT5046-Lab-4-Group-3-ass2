package com.example.fit5046_lab4_group3_ass2

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fit5046_lab4_group3_ass2.api.ItemsRepository
import com.example.fit5046_lab4_group3_ass2.room.DayUseRepository
import com.example.fit5046_lab4_group3_ass2.sensor.SensorDataProvider
import com.example.sensorslab.SensorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class StorageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.e("WORKER", "Starting work...")

        val sensorRepo = SensorRepository(SensorDataProvider(CoroutineScope(Dispatchers.Default)))
        val itemsRepo = ItemsRepository()
        val dayUseRepo = DayUseRepository(applicationContext as Application)

        val helper = Storage(applicationContext, sensorRepo, itemsRepo, dayUseRepo)

        try {
            helper.storeRecord()
            Log.e("WORKER", "Record stored successfully")
            return Result.success()
        } catch (e: Exception) {
            Log.e("WORKER", "Failed to store record", e)
            return Result.failure()
        }
    }
}