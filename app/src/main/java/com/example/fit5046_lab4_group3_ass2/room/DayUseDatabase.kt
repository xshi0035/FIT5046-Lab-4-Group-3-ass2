package com.example.fit5046_lab4_group3_ass2.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DayUse::class], version = 1, exportSchema = false)
abstract class DayUseDatabase : RoomDatabase() {
    abstract fun DayUseDAO(): DayUseDAO
    companion object {
        @Volatile
        private var INSTANCE: DayUseDatabase? = null
        fun getDatabase(context: Context): DayUseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DayUseDatabase::class.java,
                    "subject_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}