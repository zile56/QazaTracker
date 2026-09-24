package com.zilehasnain.qazatracker.ui.baseline

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BaselineSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun defaultState() = BaselineSummaryUiState(
        method = CalculationMethod.AGE_ESTIMATE,
        missedDays = 100,
        counts = PrayerType.entries.associateWith { "100" }
    )

    private fun setContent(
        uiState: BaselineSummaryUiState,
        onBack: () -> Unit = {},
        onCountChanged: (PrayerType, String) -> Unit = { _, _ -> },
        onConfirmClicked: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                BaselineSummaryContent(
                    uiState = uiState,
                    onBack = onBack,
                    onCountChanged = onCountChanged,
                    onConfirmClicked = onConfirmClicked
                )
            }
        }
    }

    @Test
    fun total_reflectsSumOfAllFiveRows() {
        setContent(uiState = defaultState())

        // 5 prayer types x 100 each.
        composeTestRule.onNodeWithText("500").assertExists()
    }

    @Test
    fun backButton_invokesCallback() {
        var backPressed = false
        setContent(uiState = defaultState(), onBack = { backPressed = true })

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(backPressed)
    }

    @Test
    fun startTrackingButton_isEnabled_beforeConfirmation() {
        setContent(uiState = defaultState())

        composeTestRule.onNodeWithText("Start tracking").assertIsEnabled()
    }

    @Test
    fun clickingStartTracking_invokesConfirmCallback() {
        var confirmed = false
        setContent(uiState = defaultState(), onConfirmClicked = { confirmed = true })

        composeTestRule.onNodeWithText("Start tracking").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun explainerText_reflectsAgeEstimateMethod() {
        setContent(uiState = defaultState())

        composeTestRule.onNodeWithText(
            "Based on 100 days since prayer became obligatory for you, here's our estimate. Adjust any row before you begin."
        ).assertExists()
    }
}
