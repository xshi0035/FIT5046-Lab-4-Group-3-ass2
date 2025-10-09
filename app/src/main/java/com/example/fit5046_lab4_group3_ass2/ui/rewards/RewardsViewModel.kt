package com.example.fit5046_lab4_group3_ass2.ui.rewards

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class Badge(
    val title: String,
    val subtitle: String,
    val date: String
)

data class MonthlyProgress(
    val pointsThisMonth: Int,
    val badgesEarned: Int,
    val daysActive: Int,
    val daysInMonth: Int,
    val monthlyGoal: Int
)

data class RewardsUiState(
    val totalPoints: Int = 0,
    val electricityPoints: Int = 0,
    val badges: List<Badge> = emptyList(),
    val monthly: MonthlyProgress = MonthlyProgress(0, 0, 0, 30, 1000)
)

class RewardsViewModel : ViewModel() {
    private val _ui = MutableStateFlow(
        RewardsUiState(
            totalPoints = 2_847,
            electricityPoints = 1_523,
            badges = listOf(
                Badge("Peak Shaver", "Avoided peak-hour usage for 7 days", "Jan 15"),
                Badge("100 kWh Saved", "Reduced electricity consumption", "Jan 10"),
                Badge("30-day Streak", "Consistent daily logging", "Dec 28"),
            ),
            monthly = MonthlyProgress(
                pointsThisMonth = 847,
                badgesEarned = 3,
                daysActive = 18,
                daysInMonth = 31,
                monthlyGoal = 1000
            )
        )
    )
    val ui: StateFlow<RewardsUiState> = _ui

    fun addElectricityPoints(delta: Int) = _ui.update {
        it.copy(
            electricityPoints = (it.electricityPoints + delta).coerceAtLeast(0),
            totalPoints = (it.totalPoints + delta).coerceAtLeast(0)
        )
    }
}
