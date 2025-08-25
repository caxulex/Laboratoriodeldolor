package com.example.laboratoriodeldolor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(log: ExerciseLog)

    @Query("SELECT * FROM exercise_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ExerciseLog>>

    @Query("DELETE FROM exercise_logs WHERE id = (SELECT id FROM exercise_logs ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteMostRecent()
}
