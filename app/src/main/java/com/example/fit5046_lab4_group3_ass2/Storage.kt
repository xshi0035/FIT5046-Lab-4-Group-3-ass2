package com.example.fit5046_lab4_group3_ass2

import android.content.Context
import com.example.fit5046_lab4_group3_ass2.api.ItemsRepository
import com.example.fit5046_lab4_group3_ass2.api.MarketResponse
import com.example.fit5046_lab4_group3_ass2.room.DayUse
import com.example.fit5046_lab4_group3_ass2.room.DayUseRepository
import com.example.sensorslab.SensorRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

class Storage(
    private val context: Context,
    private val sensorRepository: SensorRepository,
    private val itemsRepository: ItemsRepository,
    private val dayUseRepository: DayUseRepository
) {
    suspend fun storeRecord() {
        val latest = withTimeoutOrNull(5_000L) {
            sensorRepository.getSensorData().first { it != "0" }
        } ?: sensorRepository.getSensorData().value

        val marketResponse = try {
            itemsRepository.customSearch()
        } catch (e: Exception) {
            MarketResponse()
        }

        val price = if (marketResponse.data.isNotEmpty()) {
            marketResponse.data[0].results[0].data
                .last { it.size > 1 && it[1] is Number }[1].toString().toFloat()
        } else {
            0f
        }

        val dayUse = DayUse(
            date = (Date().time / 1000),
            use = latest.toFloat(),
            price = price.toInt()
        )

        dayUseRepository.insert(dayUse)
    }

    suspend fun getNotificationInfo(): Array<Float> {
        val latest = withTimeoutOrNull(5_000L) {
            sensorRepository.getSensorData().first { it != "0" }
        } ?: sensorRepository.getSensorData().value

        val marketResponse = try {
            itemsRepository.customSearch()
        } catch (e: Exception) {
            MarketResponse()
        }

        val price = if (marketResponse.data.isNotEmpty()) {
            marketResponse.data[0].results[0].data
                .last { it.size > 1 && it[1] is Number }[1].toString().toFloat()
        } else {
            0f
        }

        return arrayOf(latest.toFloat(), price)
    }
}