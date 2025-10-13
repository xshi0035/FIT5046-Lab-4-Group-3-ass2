package com.example.fit5046_lab4_group3_ass2.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DayUse(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val date: Long,
    val use: Float,
    val price: Int
)
