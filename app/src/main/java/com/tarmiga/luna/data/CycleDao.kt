package com.tarmiga.luna.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycle_entries ORDER BY date ASC")
    fun getAllCyclesFlow(): Flow<List<CycleEntry>>

    @Query("SELECT * FROM cycle_entries ORDER BY date ASC")
    suspend fun getAllCycles(): List<CycleEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(entry: CycleEntry)

    @Query("DELETE FROM cycle_entries WHERE date = :date")
    suspend fun deleteCycle(date: String)

    @Query("SELECT * FROM log_entries ORDER BY date ASC")
    fun getAllLogsFlow(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries ORDER BY date ASC")
    suspend fun getAllLogs(): List<LogEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entry: LogEntry)

    @Query("DELETE FROM log_entries WHERE date = :date")
    suspend fun deleteLog(date: String)
}
