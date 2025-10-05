package com.example.fit5046_lab4_group3_ass2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplianceDao {

    @Query("SELECT * FROM appliances ORDER BY id DESC")
    fun getAllAppliances(): Flow<List<ApplianceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliance(appliance: ApplianceEntity)

    @Delete
    suspend fun deleteAppliance(appliance: ApplianceEntity)

    @Query("DELETE FROM appliances")
    suspend fun clearAll()
}
