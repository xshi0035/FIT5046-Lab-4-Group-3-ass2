package com.example.fit5046_lab4_group3_ass2.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DayUseViewModel(application: Application) : AndroidViewModel(application) {
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
}