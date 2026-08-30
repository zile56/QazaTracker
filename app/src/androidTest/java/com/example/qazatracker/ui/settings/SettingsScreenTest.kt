package com.example.qazatracker.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.qazatracker.domain.model.NotificationFrequency
import com.example.qazatracker.ui.theme.QazaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: SettingsUiState,
        onBack: () -> Unit = {},
        onFrequencySelected: (NotificationFrequency) -> Unit = {},
        onExportClicked: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                SettingsContent(
                    uiState = uiState,
                    onBack = onBack,
                    onFrequencySelected = onFrequencySelected,
                    onExportClicked = onExportClicked
                )
            }
        }
    }

    // ---- Navigating out of Settings ----

    @Test
    fun tappingBack_invokesOnBackCallback() {
        var wentBack = false
        setContent(uiState = SettingsUiState(), onBack = { wentBack = true })

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(wentBack)
    }

    // ---- App version display ----

    @Test
    fun appVersion_displaysVersionNameFromUiState() {
        setContent(uiState = SettingsUiState(versionName = "1.0"))

        composeTestRule.onNodeWithText("1.0").assertExists()
    }

    // ---- Notification frequency selection ----

    @Test
    fun frequencyOptions_showAllFourChoices() {
        setContent(uiState = SettingsUiState())

        composeTestRule.onNodeWithText("Never").assertExists()
        composeTestRule.onNodeWithText("Weekly").assertExists()
        composeTestRule.onNodeWithText("Bi-weekly").assertExists()
        composeTestRule.onNodeWithText("Daily").assertExists()
    }

    @Test
    fun tappingAFrequencyOption_invokesCallbackWithThatFrequency() {
        var selected: NotificationFrequency? = null
        setContent(
            uiState = SettingsUiState(notificationFrequency = NotificationFrequency.WEEKLY),
            onFrequencySelected = { selected = it }
        )

        composeTestRule.onNodeWithText("Daily").performClick()

        assertTrue(selected == NotificationFrequency.DAILY)
    }

    // ---- Data export ----

    @Test
    fun tappingExport_invokesExportCallback() {
        var exported = false
        setContent(uiState = SettingsUiState(), onExportClicked = { exported = true })

        composeTestRule.onNodeWithText("Export data").performClick()

        assertTrue(exported)
    }

    @Test
    fun exportSuccess_showsRecordCountAndPath() {
        setContent(
            uiState = SettingsUiState(
                exportState = ExportState.Success(filePath = "/data/exports/qaza_export.json", recordCount = 42)
            )
        )

        composeTestRule.onNodeWithText("Saved 42 records to /data/exports/qaza_export.json").assertExists()
    }

    @Test
    fun exportFailure_showsErrorMessage() {
        setContent(
            uiState = SettingsUiState(exportState = ExportState.Failure("disk full"))
        )

        composeTestRule.onNodeWithText("Export failed: disk full").assertExists()
    }
}
