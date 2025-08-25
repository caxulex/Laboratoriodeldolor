package com.example.laboratoriodeldolor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PainLogDao {
    @Insert
    suspend fun insertLog(log: PainLog): Long

    @Query("SELECT * FROM pain_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<PainLog>
}
