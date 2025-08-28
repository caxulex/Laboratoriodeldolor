package com.example.laboratoriodeldolor

/**
 * Centralized mood emoji options used across multiple screens.
 * Order: very bad -> very good
 */
object MoodOptions {
    // Order: very bad -> very good
    // Prefer drawable icons for consistent rendering across devices; keep emoji fallbacks
    val FIVE_LEVEL = listOf(
        MoodIcon.DrawableRes(R.drawable.mood_1),
        MoodIcon.DrawableRes(R.drawable.mood_2),
        MoodIcon.DrawableRes(R.drawable.mood_3),
        MoodIcon.DrawableRes(R.drawable.mood_4),
        MoodIcon.DrawableRes(R.drawable.mood_5)
    )

    // Legacy emoji list kept for storage compatibility if needed
    val FIVE_LEVEL_EMOJI = listOf("😢", "😟", "😐", "🙂", "😄")

    fun emojiToIcon(emoji: String): MoodIcon {
        val idx = FIVE_LEVEL_EMOJI.indexOf(emoji)
        return if (idx in FIVE_LEVEL.indices) FIVE_LEVEL[idx] else MoodIcon.Emoji(emoji)
    }
}
