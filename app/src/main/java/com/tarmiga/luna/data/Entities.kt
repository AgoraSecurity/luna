package com.tarmiga.luna.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_entries")
data class CycleEntry(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val energy: String,
    val mood: String,
    val symptoms: String, // Comma-separated list
    val note: String,
    val periodStart: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
