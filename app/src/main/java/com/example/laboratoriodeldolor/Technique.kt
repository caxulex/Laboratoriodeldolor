package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "techniques")
data class Technique(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val videoUrl: String? = null
)
