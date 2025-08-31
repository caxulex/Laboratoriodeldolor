package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_steps")
data class RoutineStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val stepOrder: Int,
    val description: String,
    val techniqueId: Long? = null
)
