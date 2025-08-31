package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val bodyRegion: String?,
    val summary: String?
)
