package com.example.fit5046_lab4_group3_ass2.data

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val dob: String = "",
    val householdSize: Int = 1,
    val homeType: String = "",
    val state: String = "",
    val electricityProvider: String = "",
    val energyTips: Boolean = true,
    val weeklySummary: Boolean = true,
    val motivationStyle: String = "Balanced",
    val dashboardName: String = "",
    val avatar: String = ""
)

