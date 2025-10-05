package com.example.fit5046_lab4_group3_ass2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import com.example.fit5046_lab4_group3_ass2.data.ApplianceDao
import com.example.fit5046_lab4_group3_ass2.data.ApplianceEntity
import kotlinx.coroutines.launch

class AddApplianceViewModel(private val dao: ApplianceDao) : ViewModel() {

    fun addAppliance(name: String, watt: Int, hours: Float, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val appliance = ApplianceEntity(
                name = name,
                watt = watt,
                hours = hours,
                category = category
            )
            dao.insertAppliance(appliance)
        }
    }

    fun updateAppliance(entity: ApplianceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateAppliance(entity)
        }
    }

    suspend fun getApplianceById(id: Int): ApplianceEntity? = dao.getById(id)

    fun deleteAppliance(entity: ApplianceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAppliance(entity)
        }
    }
}
