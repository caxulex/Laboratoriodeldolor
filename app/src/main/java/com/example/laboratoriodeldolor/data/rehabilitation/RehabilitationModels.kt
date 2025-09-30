package com.example.laboratoriodeldolor.data.rehabilitation

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

/**
 * Represents a rehabilitation exercise category
 */
@Entity(tableName = "rehabilitation_categories")
data class RehabilitationCategory(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconResource: String,
    val colorHex: String,
    val displayOrder: Int = 0
)

/**
 * Represents an individual rehabilitation exercise
 */
@Entity(
    tableName = "rehabilitation_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RehabilitationCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class RehabilitationExercise(
    @PrimaryKey
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val instructions: String,
    val durationSeconds: Int,
    val repetitions: Int,
    val difficultyLevel: Int, // 1-5 scale
    val imageResource: String? = null,
    val videoUrl: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

/**
 * Represents a user's session with rehabilitation exercises
 */
@Entity(
    tableName = "rehabilitation_sessions",
    foreignKeys = [
        ForeignKey(
            entity = RehabilitationExercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("completedAt")]
)
data class RehabilitationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: String,
    val completedAt: LocalDateTime,
    val durationSeconds: Int,
    val repetitionsCompleted: Int,
    val difficultyRating: Int? = null, // User-rated difficulty 1-5
    val painLevel: Int? = null, // Pain level during exercise 0-10
    val notes: String? = null
)

/**
 * User preferences for rehabilitation exercises
 */
@Entity(tableName = "rehabilitation_preferences")
data class RehabilitationPreferences(
    @PrimaryKey
    val userId: String = "default", // Single user app
    val preferredDifficulty: Int = 2, // 1-5 scale
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "09:00", // HH:mm format
    val autoProgressEnabled: Boolean = true,
    val painThreshold: Int = 6, // Stop exercises if pain exceeds this level
    val sessionDurationMinutes: Int = 15
)

/**
 * Progress tracking for rehabilitation categories
 */
@Entity(
    tableName = "rehabilitation_progress",
    foreignKeys = [
        ForeignKey(
            entity = RehabilitationCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class RehabilitationProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: String,
    val currentLevel: Int = 1,
    val totalSessions: Int = 0,
    val lastSessionDate: LocalDateTime? = null,
    val averagePainLevel: Float = 0f,
    val averageDifficulty: Float = 0f,
    val streakDays: Int = 0
)

// Data classes for UI states and communication
data class ExerciseStep(
    val stepNumber: Int,
    val instruction: String,
    val durationSeconds: Int,
    val imageResource: String? = null
)

data class SessionSummary(
    val exerciseTitle: String,
    val categoryName: String,
    val completedAt: LocalDateTime,
    val durationSeconds: Int,
    val repetitionsCompleted: Int,
    val painLevel: Int?,
    val difficultyRating: Int?
)

data class CategoryProgress(
    val categoryId: String,
    val categoryName: String,
    val categoryDescription: String,
    val iconResource: String,
    val colorHex: String,
    val currentLevel: Int,
    val totalSessions: Int,
    val completedToday: Boolean
)

data class RehabilitationStats(
    val totalSessions: Int,
    val weeklyAverage: Float,
    val currentStreak: Int
)