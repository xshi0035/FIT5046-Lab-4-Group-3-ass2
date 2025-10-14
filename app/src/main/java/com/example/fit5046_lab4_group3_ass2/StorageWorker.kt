package com.example.fit5046_lab4_group3_ass2

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
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

        //send test notification
        createNotificationChannel()
        var builder = NotificationCompat.Builder(applicationContext, "EcoTrack")
            .setSmallIcon(R.drawable.outline_info_24)
            .setContentTitle("EcoTrack Info")
            .setContentText("Notification from EcoTrack")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())

        try {
            helper.storeRecord()
            Log.e("WORKER", "Record stored successfully")
            return Result.success()
        } catch (e: Exception) {
            Log.e("WORKER", "Failed to store record", e)
            return Result.failure()
        }
    }

    //notifications
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("EcoTrack", "EcoTrack", importance).apply {
                description = "EcoTrack"
            }
            // Register the channel with the system.
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}