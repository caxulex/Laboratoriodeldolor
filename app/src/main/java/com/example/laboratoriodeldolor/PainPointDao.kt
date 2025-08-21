package com.example.laboratoriodeldolor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PainPointDao {
    @Query("SELECT * FROM pain_points ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PainPoint>>

    @Query("SELECT * FROM pain_points ORDER BY timestamp DESC")
    suspend fun getAllSnapshot(): List<PainPoint>

    @Insert
    suspend fun insertAll(points: List<PainPoint>)

    @Query("DELETE FROM pain_points")
    suspend fun deleteAll()

    @Query("DELETE FROM pain_points WHERE id = :id")
    suspend fun deleteById(id: Long)

    // New PainLog management
    @Insert
    suspend fun insertLog(log: PainLog): Long

    @Query("SELECT * FROM pain_points WHERE logId = :logId ORDER BY timestamp DESC")
    suspend fun getByLogId(logId: Long): List<PainPoint>
}
