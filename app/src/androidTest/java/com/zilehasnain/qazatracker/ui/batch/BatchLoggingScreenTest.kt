package com.zilehasnain.qazatracker.ui.batch

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BatchLoggingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: BatchLoggingUiState,
        onBack: () -> Unit = {},
        onIncrementDays: () -> Unit = {},
        onDecrementDays: () -> Unit = {},
        onDaysChanged: (Int) -> Unit = {},
        onTogglePrayerType: (PrayerType) -> Unit = {},
        onConfirmClicked: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                BatchLoggingContent(
                    uiState = uiState,
                    onBack = onBack,
                    onIncrementDays = onIncrementDays,
                    onDecrementDays = onDecrementDays,
                    onDaysChanged = onDaysChanged,
                    onTogglePrayerType = onTogglePrayerType,
                    onConfirmClicked = onConfirmClicked
                )
            }
        }
    }

    @Test
    fun defaultState_showsAllFivePrayerTypesAndSummary() {
        setContent(uiState = BatchLoggingUiState(days = 7))

        composeTestRule.onNodeWithText("7").assertExists()
        composeTestRule.onNodeWithText("7 days × 5 prayer types = 35 completions logged.").assertExists()
    }

    @Test
    fun backButton_invokesCallback() {
        var backPressed = false
        setContent(uiState = BatchLoggingUiState(), onBack = { backPressed = true })

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(backPressed)
    }

    @Test
    fun incrementButton_invokesCallback() {
        var incremented = false
        setContent(uiState = BatchLoggingUiState(), onIncrementDays = { incremented = true })

        composeTestRule.onNodeWithText("+").performClick()

        assertTrue(incremented)
    }

    @Test
    fun decrementButton_invokesCallback() {
        var decremented = false
        setContent(uiState = BatchLoggingUiState(), onDecrementDays = { decremented = true })

        composeTestRule.onNodeWithText("−").performClick()

        assertTrue(decremented)
    }

    @Test
    fun togglingPrayerTypeChip_invokesCallbackWithCorrectType() {
        var toggled: PrayerType? = null
        setContent(uiState = BatchLoggingUiState(), onTogglePrayerType = { toggled = it })

        composeTestRule.onNodeWithText("Asr").performClick()

        assertTrue(toggled == PrayerType.ASR)
    }

    @Test
    fun confirmButton_isDisabled_whenNoPrayerTypesSelected() {
        setContent(uiState = BatchLoggingUiState(selectedPrayerTypes = emptySet()))

        composeTestRule.onNodeWithText("Select at least one prayer type to continue.").assertExists()
        composeTestRule.onNodeWithText("Log entries").assertIsNotEnabled()
    }

    @Test
    fun confirmButton_isEnabled_withValidSelection() {
        setContent(uiState = BatchLoggingUiState(days = 5, selectedPrayerTypes = setOf(PrayerType.FAJR)))

        composeTestRule.onNodeWithText("Log entries").assertIsEnabled()
    }

    @Test
    fun clickingConfirm_invokesCallback() {
        var confirmed = false
        setContent(
            uiState = BatchLoggingUiState(days = 5, selectedPrayerTypes = setOf(PrayerType.FAJR)),
            onConfirmClicked = { confirmed = true }
        )

        composeTestRule.onNodeWithText("Log entries").performClick()

        assertTrue(confirmed)
    }
}
