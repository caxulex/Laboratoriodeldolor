package com.example.laboratoriodeldolor

/**
 * Centralized mood emoji options used across multiple screens.
 * Order: very bad -> very good
 */
object MoodOptions {
    // Order: very bad -> very good. Updated to use crying face as the lowest mood.
    val FIVE_LEVEL = listOf("😥", "😟", "😐", "🙂", "😄")

    // Legacy mapping array used by some tests
    val FIVE_LEVEL_EMOJI = FIVE_LEVEL

    // Backwards-compatible helpers used only in tests: return raw emoji strings
    fun moodIconsForPack(packIndex: Int): List<String> = FIVE_LEVEL

    fun emojiToIcon(emoji: String): String = if (FIVE_LEVEL_EMOJI.contains(emoji)) emoji else FIVE_LEVEL[2]
}
