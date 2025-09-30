package com.example.laboratoriodeldolor

/**
 * Centralized emoji -> mood score mapping used across the app.
 * Returns a value in 1..5 where 1 = very negative, 5 = very positive.
 */
object MoodMapping {
    fun emojiToScore(emoji: String): Int {
        return when (emoji) {
            "😥" -> 1
            "😟" -> 2
            "😐" -> 3
            "🙂" -> 4
            "😄" -> 5
            // legacy mappings
            "😍" -> 5
            "😊", "😀" -> 4
            // Treat crying and sad variants as low scores
            "😞", "😢" -> 2
            "😠" -> 1
            else -> 3
        }
    }
}
