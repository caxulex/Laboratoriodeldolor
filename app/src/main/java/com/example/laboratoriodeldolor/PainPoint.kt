package com.example.laboratoriodeldolor

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a normalized pain point (x,y in 0..1 range) stored in the DB.
 * Now includes the `view` ("front" | "back") and a `logId` linking to a PainLog session.
 */
@Entity(tableName = "pain_points")
data class PainPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val x: Float,
    val y: Float,
    val view: String = "front",
    val logId: Long = 0,
    val intensity: Int = 1, // 1 moderate (yellow), 2 high (orange), 3 severe (red)
    val timestamp: Long = System.currentTimeMillis()
)

