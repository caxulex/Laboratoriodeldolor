package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val emoji: String,
    val note: String,
    val timestamp: Long,
    // New five-level mood score: 1 (very bad) .. 5 (very good)
    val moodScore: Int = 3
)