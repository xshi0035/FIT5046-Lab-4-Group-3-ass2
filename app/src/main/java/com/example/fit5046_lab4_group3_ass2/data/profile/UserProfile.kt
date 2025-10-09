package com.example.fit5046_lab4_group3_ass2.data.profile

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val dob: String = "",                 // formatted date string
    val householdSize: String = "",
    val homeType: String = "Apartment",
    val state: String = "VIC",
    val electricityProvider: String = "",
    val notifEnergyTips: Boolean = true,
    val notifWeeklySummary: Boolean = true,
    val motivation: String = "Balanced",
    val dashboardName: String = ""
)
