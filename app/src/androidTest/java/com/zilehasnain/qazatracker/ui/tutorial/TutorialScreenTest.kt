package com.zilehasnain.qazatracker.ui.tutorial

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TutorialScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        onGetStarted: () -> Unit = {},
        onSkip: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                TutorialContent(onGetStarted = onGetStarted, onSkip = onSkip)
            }
        }
    }

    @Test
    fun showsTheWelcomeHeader() {
        setContent()

        composeTestRule.onNodeWithText("Welcome to Qaza Tracker").assertExists()
    }

    @Test
    fun showsAllFourTips_coveringBaselineLoggingProgressAndNotifications() {
        setContent()

        composeTestRule.onNodeWithText("Calculate your baseline").assertExists()
        composeTestRule.onNodeWithText("Log prayers as you go").assertExists()
        composeTestRule.onNodeWithText("Watch your progress").assertExists()
        composeTestRule.onNodeWithText("Gentle reminders").assertExists()
    }

    @Test
    fun tappingLetsGetStarted_invokesOnGetStartedOnly() {
        var started = 0
        var skipped = 0
        setContent(onGetStarted = { started++ }, onSkip = { skipped++ })

        composeTestRule.onNodeWithText("Let's get started").performClick()

        assertEquals(1, started)
        assertEquals(0, skipped)
    }

    @Test
    fun tappingSkip_invokesOnSkipOnly() {
        var started = 0
        var skipped = 0
        setContent(onGetStarted = { started++ }, onSkip = { skipped++ })

        composeTestRule.onNodeWithText("Skip").performClick()

        assertEquals(0, started)
        assertEquals(1, skipped)
    }
}
