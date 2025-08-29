package com.example.laboratoriodeldolor

import org.junit.Assert.*
import org.junit.Test

class MoodOptionsTest {
    @Test
    fun `moodIconsForPack returns default for unknown index`() {
        val icons = MoodOptions.moodIconsForPack(999)
        assertEquals(5, icons.size)
        // Ensure they are MoodIcon.Emoji instances
        assertTrue(icons.all { it is MoodIcon.Emoji })
    }

    @Test
    fun `emojiToIcon maps legacy emoji to FIVE_LEVEL icons`() {
        val legacy = MoodOptions.FIVE_LEVEL_EMOJI[0]
        val icon = MoodOptions.emojiToIcon(legacy)
        assertTrue(icon is MoodIcon.Emoji)
    }
}
