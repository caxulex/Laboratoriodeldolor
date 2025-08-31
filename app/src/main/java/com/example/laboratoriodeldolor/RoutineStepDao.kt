package com.example.laboratoriodeldolor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineStepDao {
    @Query("SELECT * FROM routine_steps WHERE routineId = :routineId ORDER BY stepOrder ASC")
    fun getForRoutine(routineId: Long): Flow<List<RoutineStep>>

    @Insert
    suspend fun insert(step: RoutineStep): Long
}
