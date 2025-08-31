package com.example.laboratoriodeldolor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY title ASC")
    fun getAll(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Routine?

    @Insert
    suspend fun insert(routine: Routine): Long

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    fun getWithSteps(id: Long): Flow<RoutineWithSteps?>
}
