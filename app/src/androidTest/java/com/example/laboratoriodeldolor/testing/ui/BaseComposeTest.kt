package com.example.laboratoriodeldolor.testing.ui

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule

/**
 * Base class for Compose UI tests.
 * Provides common utilities and setup for testing UI components.
 * 
 * This class follows accessibility-first testing principles, emphasizing
 * semantic matchers over implementation details.
 */
abstract class BaseComposeTest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    /**
     * Find a UI element by its accessibility content description.
     * This is the preferred way to find elements as it ensures accessibility compliance.
     */
    protected fun findByContentDescription(description: String) =
        composeTestRule.onNodeWithContentDescription(description)

    /**
     * Find a UI element by its displayed text.
     * Use this for buttons, labels, and other text-containing elements.
     */
    protected fun findByText(text: String) =
        composeTestRule.onNodeWithText(text)

    /**
     * Find a UI element by its test tag.
     * Use this sparingly, prefer semantic matchers when possible.
     */
    protected fun findByTestTag(tag: String) =
        composeTestRule.onNodeWithTag(tag)

    /**
     * Verify that an element is visible on screen.
     */
    protected fun assertVisible(description: String) {
        findByContentDescription(description).assertIsDisplayed()
    }

    /**
     * Verify that text is visible on screen.
     */
    protected fun assertTextVisible(text: String) {
        findByText(text).assertIsDisplayed()
    }

    /**
     * Verify that an element is clickable.
     */
    protected fun assertClickable(description: String) {
        findByContentDescription(description).assertHasClickAction()
    }

    /**
     * Click on an element by its content description.
     */
    protected fun clickByContentDescription(description: String) {
        findByContentDescription(description).performClick()
    }

    /**
     * Click on an element by its text.
     */
    protected fun clickByText(text: String) {
        findByText(text).performClick()
    }

    /**
     * Wait for the compose to settle before proceeding.
     * Useful when dealing with animations or async operations.
     */
    protected fun waitForIdle() {
        composeTestRule.waitForIdle()
    }

    /**
     * Set the content for the compose test.
     * Should be called in test setup.
     */
    protected fun setContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        composeTestRule.setContent(content)
    }
}

/**
 * Common test tags for UI elements.
 * Using consistent tags across the app helps with testing.
 */
object TestTags {
    // Mood tracking tags
    const val MOOD_EMOJI_BUTTON = "mood_emoji_button"
    const val MOOD_NOTE_INPUT = "mood_note_input"
    const val MOOD_SAVE_BUTTON = "mood_save_button"
    const val MOOD_HISTORY_LIST = "mood_history_list"
    
    // Pain tracking tags
    const val PAIN_INTENSITY_SLIDER = "pain_intensity_slider"
    const val PAIN_BODY_VIEW = "pain_body_view"
    const val PAIN_FRONT_VIEW = "pain_front_view"
    const val PAIN_BACK_VIEW = "pain_back_view"
    const val PAIN_SAVE_BUTTON = "pain_save_button"
    
    // Navigation tags
    const val BOTTOM_NAV_MOOD = "bottom_nav_mood"
    const val BOTTOM_NAV_PAIN = "bottom_nav_pain"
    const val BOTTOM_NAV_EXERCISES = "bottom_nav_exercises"
    const val BOTTOM_NAV_DIARY = "bottom_nav_diary"
    const val BOTTOM_NAV_SETTINGS = "bottom_nav_settings"
    
    // Common UI tags
    const val LOADING_INDICATOR = "loading_indicator"
    const val ERROR_MESSAGE = "error_message"
    const val EMPTY_LIST_MESSAGE = "empty_list_message"
}

/**
 * Common content descriptions for accessibility testing.
 * These should match the actual content descriptions used in the UI.
 */
object ContentDescriptions {
    // Mood tracking descriptions
    const val MOOD_VERY_HAPPY = "Very happy mood"
    const val MOOD_HAPPY = "Happy mood"
    const val MOOD_NEUTRAL = "Neutral mood"
    const val MOOD_SAD = "Sad mood"
    const val MOOD_VERY_SAD = "Very sad mood"
    const val MOOD_NOTE_FIELD = "Mood note input field"
    const val SAVE_MOOD_ENTRY = "Save mood entry"
    
    // Pain tracking descriptions
    const val PAIN_INTENSITY_CONTROL = "Pain intensity slider"
    const val BODY_DIAGRAM_FRONT = "Front view of body diagram for pain tracking"
    const val BODY_DIAGRAM_BACK = "Back view of body diagram for pain tracking"
    const val SWITCH_TO_BACK_VIEW = "Switch to back view"
    const val SWITCH_TO_FRONT_VIEW = "Switch to front view"
    const val SAVE_PAIN_ENTRY = "Save pain entry"
    
    // Navigation descriptions
    const val NAVIGATE_TO_MOOD = "Navigate to mood tracking"
    const val NAVIGATE_TO_PAIN = "Navigate to pain tracking"
    const val NAVIGATE_TO_EXERCISES = "Navigate to exercises"
    const val NAVIGATE_TO_DIARY = "Navigate to diary"
    const val NAVIGATE_TO_SETTINGS = "Navigate to settings"
    
    // Common UI descriptions
    const val LOADING = "Loading content"
    const val ERROR_OCCURRED = "An error occurred"
    const val NO_DATA_AVAILABLE = "No data available"
}

/**
 * Extension functions for common UI testing patterns.
 */
fun ComposeContentTestRule.assertMoodEmojiButtonsVisible() {
    onNodeWithContentDescription(ContentDescriptions.MOOD_VERY_HAPPY).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.MOOD_HAPPY).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.MOOD_NEUTRAL).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.MOOD_SAD).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.MOOD_VERY_SAD).assertIsDisplayed()
}

fun ComposeContentTestRule.assertBottomNavigationVisible() {
    onNodeWithContentDescription(ContentDescriptions.NAVIGATE_TO_MOOD).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.NAVIGATE_TO_PAIN).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.NAVIGATE_TO_EXERCISES).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.NAVIGATE_TO_DIARY).assertIsDisplayed()
    onNodeWithContentDescription(ContentDescriptions.NAVIGATE_TO_SETTINGS).assertIsDisplayed()
}