package com.example.fit5046_lab4_group3_ass2.room

import android.app.Application
import kotlinx.coroutines.flow.Flow

class DayUseRepository(application: Application) {
    private var dayUseDao: DayUseDAO =
        DayUseDatabase.getDatabase(application).DayUseDAO()
    val allDayUses: Flow<List<DayUse>> = dayUseDao.getAllDayUses()

    suspend fun insert(dayUse: DayUse) {
        dayUseDao.insertDayUse(dayUse)
    }
    suspend fun delete(dayUse: DayUse) {
        dayUseDao.deleteDayUse(dayUse)
    }
    suspend fun update(dayUse: DayUse) {
        dayUseDao.updateDayUse(dayUse)
    }
}