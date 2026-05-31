package com.tarmiga.luna.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CycleEntry::class, LogEntry::class], version = 1, exportSchema = false)
abstract class LunaDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao

    companion object {
        @Volatile
        private var INSTANCE: LunaDatabase? = null

        fun getDatabase(context: Context): LunaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LunaDatabase::class.java,
                    "luna_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
