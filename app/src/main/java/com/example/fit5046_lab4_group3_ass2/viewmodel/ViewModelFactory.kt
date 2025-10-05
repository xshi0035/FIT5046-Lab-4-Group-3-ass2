package com.example.fit5046_lab4_group3_ass2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fit5046_lab4_group3_ass2.data.AppDatabase

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddApplianceViewModel::class.java) -> {
                AddApplianceViewModel(db.applianceDao()) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(db) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
