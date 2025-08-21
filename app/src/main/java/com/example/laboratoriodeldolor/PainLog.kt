package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pain_logs")
data class PainLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
