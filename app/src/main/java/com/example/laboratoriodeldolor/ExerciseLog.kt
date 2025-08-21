package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a timestamp when the user completes their daily exercises.
 */
@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long
)
