package com.example.fit5046_lab4_group3_ass2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appliances")
data class ApplianceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val watt: Int,
    val hours: Float,
    val category: String
)
