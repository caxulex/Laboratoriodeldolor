package com.example.laboratoriodeldolor

import org.junit.Assert.assertEquals
import org.junit.Test

class MoodMappingTest {
    @Test
    fun testEmojiToScore_defaultsAndLegacy() {
        // Core five-level
    assertEquals(1, MoodMapping.emojiToScore("�"))
        assertEquals(2, MoodMapping.emojiToScore("😟"))
        assertEquals(3, MoodMapping.emojiToScore("😐"))
        assertEquals(4, MoodMapping.emojiToScore("🙂"))
        assertEquals(5, MoodMapping.emojiToScore("😄"))

        // Legacy variants map to reasonable scores
        assertEquals(5, MoodMapping.emojiToScore("😍"))
        assertEquals(4, MoodMapping.emojiToScore("😊"))
        assertEquals(2, MoodMapping.emojiToScore("😢"))
        assertEquals(1, MoodMapping.emojiToScore("😠"))

        // Unknown emoji should default to neutral (3)
        assertEquals(3, MoodMapping.emojiToScore("🌀"))
    }
}
