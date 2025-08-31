package com.example.laboratoriodeldolor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TechniqueDao {
    @Query("SELECT * FROM techniques ORDER BY title ASC")
    fun getAll(): Flow<List<Technique>>

    @Query("SELECT * FROM techniques WHERE id = :id")
    suspend fun getById(id: Long): Technique?

    @Insert
    suspend fun insert(technique: Technique): Long
}
