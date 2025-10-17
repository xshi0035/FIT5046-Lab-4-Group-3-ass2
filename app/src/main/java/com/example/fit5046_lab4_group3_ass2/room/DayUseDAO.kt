package com.example.fit5046_lab4_group3_ass2.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayUseDAO {
    @Query("SELECT * FROM DayUse")
    fun getAllDayUses(): Flow<List<DayUse>>

    @Insert
    suspend fun insertDayUse(subject: DayUse)

    @Update
    suspend fun updateDayUse(subject: DayUse)

    @Delete
    suspend fun deleteDayUse(subject: DayUse)
}
