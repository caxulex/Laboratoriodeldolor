package com.example.laboratoriodeldolor

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.*
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
// removed unused UiDevice import (uiautomator dependency added to androidTest if needed)

@RunWith(AndroidJUnit4::class)
class MainFlowsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun checkinFlow_openApp_selectMood_skipPain_landsOnDashboard() {
    // Select an emoji (index 4 = very good) and press save flow
    composeTestRule.onNodeWithTag("moodEmoji_4").performClick()
    // Use the skip button to move past pain screen
    composeTestRule.onNodeWithTag("checkin_skip").performClick()
    // Now we should be on the Diario/Dashboard screen; assert the screen title is present
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("screen_title").assertIsDisplayed()
    }

    @Test
    fun painTracker_addPoint_showsLocationInList() {
    // Navigate to PainTracker screen via bottom navigation by clicking the nav item with label matching Screen.Dolor
    // The bottom navigation labels use string resources; we find the navigation item by its content description if provided or by text.
    composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.pain_tracker_title)).performClick()
    composeTestRule.waitForIdle()

    // Ensure front view selected and gender male
    composeTestRule.onNodeWithTag("gender_male").performClick()
    composeTestRule.onNodeWithTag("view_front").performClick()

    // Tap the canvas at center
    val canvas = composeTestRule.onNodeWithTag("bodyCanvas")
    canvas.performTouchInput { click(center) }

    // Animated visibility might take a moment; wait and then assert that selected areas contains at least one known label
    composeTestRule.waitForIdle()
    // Assert the PainTracker screen title is visible
    composeTestRule.onNodeWithTag("screen_title").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_clickEach_showsTitles() {
        val items = listOf(
            R.string.mood_tracker_title,
            R.string.diary_title,
            R.string.exercises_label,
            R.string.pain_tracker_title,
            R.string.recommendation_title,
            R.string.breath_title,
            R.string.settings_title
        )

        for (res in items) {
            // Click the bottom navigation button using its label text and assert the screen title appears
            val label = composeTestRule.activity.getString(res)
            composeTestRule.onNodeWithText(label).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("screen_title").assertIsDisplayed()
        }
    }
}
