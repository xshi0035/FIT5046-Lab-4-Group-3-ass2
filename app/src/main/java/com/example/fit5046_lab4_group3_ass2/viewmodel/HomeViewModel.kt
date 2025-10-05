package com.example.fit5046_lab4_group3_ass2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_lab4_group3_ass2.data.AppDatabase
import com.example.fit5046_lab4_group3_ass2.data.ApplianceEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val db: AppDatabase) : ViewModel() {

    // ✅ Backing property (MutableStateFlow)
    private val _allAppliances = MutableStateFlow<List<ApplianceEntity>>(emptyList())

    // ✅ Exposed immutable Flow for composables
    val allAppliances: StateFlow<List<ApplianceEntity>> = _allAppliances.asStateFlow()

    init {
        loadAppliances()
    }

    private fun loadAppliances() {
        viewModelScope.launch {
            db.applianceDao().getAllAppliances().collect { appliances ->
                _allAppliances.value = appliances
            }
        }
    }
}
