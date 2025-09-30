package com.example.laboratoriodeldolor

import org.junit.Assert.assertEquals
import org.junit.Test

class MoodMappingTest {
    @Test
    fun emojiToScore_mapsKnownEmojis() {
        assertEquals(1, MoodMapping.emojiToScore("😥"))
        assertEquals(2, MoodMapping.emojiToScore("😟"))
        assertEquals(3, MoodMapping.emojiToScore("😐"))
        assertEquals(4, MoodMapping.emojiToScore("🙂"))
        assertEquals(5, MoodMapping.emojiToScore("😄"))
        // legacy/aliases
        assertEquals(5, MoodMapping.emojiToScore("😍"))
        assertEquals(4, MoodMapping.emojiToScore("😊"))
        assertEquals(4, MoodMapping.emojiToScore("😀"))
        assertEquals(2, MoodMapping.emojiToScore("😞"))
        assertEquals(2, MoodMapping.emojiToScore("😢"))
        assertEquals(1, MoodMapping.emojiToScore("😠"))
    }

    @Test
    fun emojiToScore_unknownEmoji_returnsNeutral() {
        assertEquals(3, MoodMapping.emojiToScore("🤖"))
        assertEquals(3, MoodMapping.emojiToScore("🌀"))
    }
}
