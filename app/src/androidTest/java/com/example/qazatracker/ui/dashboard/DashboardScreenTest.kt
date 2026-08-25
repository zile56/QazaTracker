package com.example.qazatracker.ui.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.qazatracker.domain.model.CompletionProjection
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.ui.theme.QazaTrackerTheme
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
        onHistoryClicked: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                DashboardContent(
                    uiState = uiState,
                    onQuickLog = onQuickLog,
                    onLogBatchClicked = onLogBatchClicked,
                    onHistoryClicked = onHistoryClicked
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
}
