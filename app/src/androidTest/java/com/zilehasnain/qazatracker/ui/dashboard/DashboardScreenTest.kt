package com.zilehasnain.qazatracker.ui.dashboard

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun rows(completedEach: Int, remainingEach: Int) =
        PrayerType.entries.map { PrayerRowUiState(it, completed = completedEach, remaining = remainingEach) }

    private fun setContent(
        uiState: DashboardUiState,
        onQuickLog: (PrayerType) -> Unit = {},
        onLogBatchClicked: () -> Unit = {},
        onHistoryClicked: () -> Unit = {},
        onSettingsClicked: () -> Unit = {},
        onAdjustClicked: (PrayerType) -> Unit = {},
        onAdjustmentSignChanged: (Boolean) -> Unit = {},
        onAdjustmentMagnitudeChanged: (String) -> Unit = {},
        onAdjustmentNoteChanged: (String) -> Unit = {},
        onConfirmAdjustment: () -> Unit = {},
        onDismissAdjustmentDialog: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                DashboardContent(
                    uiState = uiState,
                    onQuickLog = onQuickLog,
                    onLogBatchClicked = onLogBatchClicked,
                    onHistoryClicked = onHistoryClicked,
                    onSettingsClicked = onSettingsClicked,
                    onAdjustClicked = onAdjustClicked,
                    onAdjustmentSignChanged = onAdjustmentSignChanged,
                    onAdjustmentMagnitudeChanged = onAdjustmentMagnitudeChanged,
                    onAdjustmentNoteChanged = onAdjustmentNoteChanged,
                    onConfirmAdjustment = onConfirmAdjustment,
                    onDismissAdjustmentDialog = onDismissAdjustmentDialog
                )
            }
        }
    }

    @Test
    fun totalRemaining_sumsAcrossAllFivePrayerTypes() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 10, remainingEach = 20),
                projection = CompletionProjection.InsufficientData
            )
        )

        // 5 x 20 = 100.
        composeTestRule.onNodeWithText("100").assertExists()
    }

    @Test
    fun prayerRow_showsRemainingAndCompletedCounts() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 10, remainingEach = 20),
                projection = CompletionProjection.InsufficientData
            )
        )

        composeTestRule.onNodeWithText("20 left · 10 done").assertExists()
    }

    // ---- The three projection states must render distinct, recognizable UI ----

    @Test
    fun insufficientDataProjection_showsPaceMessage() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 50),
                projection = CompletionProjection.InsufficientData
            )
        )

        composeTestRule.onNodeWithText("Log a few prayers to see your pace").assertExists()
    }

    @Test
    fun alreadyCaughtUpProjection_showsCaughtUpMessage() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 50, remainingEach = 0),
                projection = CompletionProjection.AlreadyCaughtUp
            )
        )

        composeTestRule.onNodeWithText("You are fully caught up").assertExists()
    }

    @Test
    fun estimatedProjection_showsMonthsMessage_forMultiMonthEstimate() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 10, remainingEach = 40),
                projection = CompletionProjection.Estimated(
                    projectedDate = LocalDate.now().plusDays(90),
                    daysRemaining = 90
                )
            )
        )

        composeTestRule.onNodeWithText("At your pace, cleared in ~3 months").assertExists()
    }

    @Test
    fun estimatedProjection_showsUnderAMonthMessage_forShortEstimate() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 45, remainingEach = 5),
                projection = CompletionProjection.Estimated(
                    projectedDate = LocalDate.now().plusDays(10),
                    daysRemaining = 10
                )
            )
        )

        composeTestRule.onNodeWithText("At your pace, cleared in under a month").assertExists()
    }

    @Test
    fun estimatedProjection_usesSingularMonth_forExactlyOneMonth() {
        setContent(
            DashboardUiState(
                rows = rows(completedEach = 10, remainingEach = 30),
                projection = CompletionProjection.Estimated(
                    projectedDate = LocalDate.now().plusDays(30),
                    daysRemaining = 30
                )
            )
        )

        composeTestRule.onNodeWithText("At your pace, cleared in ~1 month").assertExists()
    }

    // ---- Quick single-tap log and batch entry point ----

    @Test
    fun tappingQuickLog_invokesCallbackWithCorrectPrayerType() {
        var logged: PrayerType? = null
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                projection = CompletionProjection.InsufficientData
            ),
            onQuickLog = { logged = it }
        )

        composeTestRule.onNodeWithContentDescription("Log one Asr").performClick()

        assertTrue(logged == PrayerType.ASR)
    }

    @Test
    fun tappingLogMultipleDays_invokesCallback() {
        var clicked = false
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                projection = CompletionProjection.InsufficientData
            ),
            onLogBatchClicked = { clicked = true }
        )

        composeTestRule.onNodeWithText("Log multiple days").performClick()

        assertTrue(clicked)
    }

    @Test
    fun tappingHistoryIcon_invokesCallback() {
        var clicked = false
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                projection = CompletionProjection.InsufficientData
            ),
            onHistoryClicked = { clicked = true }
        )

        composeTestRule.onNodeWithContentDescription("History").performClick()

        assertTrue(clicked)
    }

    @Test
    fun tappingSettingsIcon_invokesCallback() {
        var clicked = false
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                projection = CompletionProjection.InsufficientData
            ),
            onSettingsClicked = { clicked = true }
        )

        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue(clicked)
    }

    // ---- Manual adjustment entry point and dialog validation ----

    @Test
    fun tappingAdjustIcon_invokesCallbackWithCorrectPrayerType() {
        var adjusted: PrayerType? = null
        setContent(
            uiState = DashboardUiState(rows = rows(completedEach = 0, remainingEach = 10)),
            onAdjustClicked = { adjusted = it }
        )

        composeTestRule.onNodeWithContentDescription("Adjust Fajr").performClick()

        assertTrue(adjusted == PrayerType.FAJR)
    }

    @Test
    fun adjustmentDialog_saveIsDisabled_whenMagnitudeIsEmpty() {
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                adjustmentDialog = AdjustmentDialogState(PrayerType.FAJR)
            )
        )

        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun adjustmentDialog_saveIsDisabled_whenDeltaIsZero() {
        // Zero magnitude means delta == 0 regardless of sign, matching
        // ApplyAdjustmentUseCase's own require(delta != 0).
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                adjustmentDialog = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "0")
            )
        )

        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun adjustmentDialog_saveIsEnabled_withNonZeroMagnitude() {
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                adjustmentDialog = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "5")
            )
        )

        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun adjustmentDialog_clickingSave_invokesConfirmCallback() {
        var confirmed = false
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                adjustmentDialog = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "5")
            ),
            onConfirmAdjustment = { confirmed = true }
        )

        composeTestRule.onNodeWithText("Save").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun adjustmentDialog_clickingCancel_invokesDismissCallback() {
        var dismissed = false
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                adjustmentDialog = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "5")
            ),
            onDismissAdjustmentDialog = { dismissed = true }
        )

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun adjustmentDialog_togglingSign_invokesCallbackWithNegativeTrue() {
        var negative: Boolean? = null
        setContent(
            uiState = DashboardUiState(
                rows = rows(completedEach = 0, remainingEach = 10),
                adjustmentDialog = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "5")
            ),
            onAdjustmentSignChanged = { negative = it }
        )

        composeTestRule.onNodeWithText("−").performClick()

        assertTrue(negative == true)
    }
}
