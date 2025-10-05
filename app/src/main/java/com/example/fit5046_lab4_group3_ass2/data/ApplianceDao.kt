package com.example.fit5046_lab4_group3_ass2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplianceDao {

    @Query("SELECT * FROM appliances ORDER BY id DESC")
    fun getAllAppliances(): Flow<List<ApplianceEntity>>

    @Query("SELECT * FROM appliances WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ApplianceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliance(appliance: ApplianceEntity)

    @Update
    suspend fun updateAppliance(appliance: ApplianceEntity)

    @Delete
    suspend fun deleteAppliance(appliance: ApplianceEntity)

    @Query("DELETE FROM appliances")
    suspend fun clearAll()
}
