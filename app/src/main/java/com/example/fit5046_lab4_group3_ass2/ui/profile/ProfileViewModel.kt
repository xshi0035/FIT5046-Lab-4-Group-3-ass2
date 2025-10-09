package com.example.fit5046_lab4_group3_ass2.ui.profile

import androidx.lifecycle.ViewModel
import com.example.fit5046_lab4_group3_ass2.data.profile.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    fun updateName(v: String) = _profile.update { it.copy(name = v) }
    fun updateEmail(v: String) = _profile.update { it.copy(email = v) }
    fun updateDob(v: String) = _profile.update { it.copy(dob = v) }
    fun updateHouseholdSize(v: String) = _profile.update { it.copy(householdSize = v) }
    fun updateHomeType(v: String) = _profile.update { it.copy(homeType = v) }
    fun updateState(v: String) = _profile.update { it.copy(state = v) }
    fun updateElectricityProvider(v: String) = _profile.update { it.copy(electricityProvider = v) }
    fun updateNotifEnergyTips(v: Boolean) = _profile.update { it.copy(notifEnergyTips = v) }
    fun updateNotifWeeklySummary(v: Boolean) = _profile.update { it.copy(notifWeeklySummary = v) }
    fun updateMotivation(v: String) = _profile.update { it.copy(motivation = v) }
    fun updateDashboardName(v: String) = _profile.update { it.copy(dashboardName = v) }

    fun saveProfile(): UserProfile = _profile.value   // 之后可接 DataStore/Room
}
