package com.zilehasnain.qazatracker.ui.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.CompletionEstimate
import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import com.zilehasnain.qazatracker.domain.model.EstimateConfidence
import com.zilehasnain.qazatracker.domain.model.MilestoneData
import com.zilehasnain.qazatracker.domain.model.PrayerMilestone
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.StreakData
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
        onStatisticsClicked: () -> Unit = {},
        onAchievementsClicked: () -> Unit = {},
        onAdjustClicked: (PrayerType) -> Unit = {},
        onAdjustmentSignChanged: (Boolean) -> Unit = {},
        onAdjustmentMagnitudeChanged: (String) -> Unit = {},
        onAdjustmentNoteChanged: (String) -> Unit = {},
        onConfirmAdjustment: () -> Unit = {},
        onDismissAdjustmentDialog: () -> Unit = {},
        onMilestoneDismissed: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                DashboardContent(
                    uiState = uiState,
                    onQuickLog = onQuickLog,
                    onLogBatchClicked = onLogBatchClicked,
                    onHistoryClicked = onHistoryClicked,
                    onSettingsClicked = onSettingsClicked,
                    onStatisticsClicked = onStatisticsClicked,
                    onAchievementsClicked = onAchievementsClicked,
                    onAdjustClicked = onAdjustClicked,
                    onAdjustmentSignChanged = onAdjustmentSignChanged,
                    onAdjustmentMagnitudeChanged = onAdjustmentMagnitudeChanged,
                    onAdjustmentNoteChanged = onAdjustmentNoteChanged,
                    onConfirmAdjustment = onConfirmAdjustment,
                    onDismissAdjustmentDialog = onDismissAdjustmentDialog,
                    onMilestoneDismissed = onMilestoneDismissed
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

    // ---- The estimate card (a real projection) ----

    private fun estimate(
        daysRemaining: Int = 45,
        confidence: EstimateConfidence = EstimateConfidence.HIGH,
        estimatedDate: LocalDate = LocalDate.now().plusDays(daysRemaining.toLong()),
        pace: Float = 8.5f
    ) = CompletionEstimate(
        estimatedDate = estimatedDate,
        daysRemaining = daysRemaining,
        averagePrayersPerDay = pace,
        confidence = confidence,
        trackingStartedOn = LocalDate.now().minusDays(30),
        daysTracked = 30,
        daysLogged = 12,
        prayersCompleted = 255,
        prayersRemaining = 3287
    )

    private fun estimatedState(estimate: CompletionEstimate = estimate()) = DashboardUiState(
        rows = rows(completedEach = 10, remainingEach = 40),
        projection = CompletionProjection.Estimated(estimate)
    )

    @Test
    fun estimatedProjection_showsHeadlineRemainingTimeAndPace() {
        setContent(estimatedState(estimate(daysRemaining = 45, pace = 8.5f)))

        composeTestRule.onNodeWithText("You'll catch up by", substring = true).assertExists()
        composeTestRule.onNodeWithText("~45 days remaining at current pace", substring = true).assertExists()
        composeTestRule.onNodeWithText("Logging ~8.5 prayers/day", substring = true).assertExists()
    }

    @Test
    fun estimatedProjection_showsTheTimelineWithTodayStartAndPercentPassed() {
        setContent(estimatedState())

        composeTestRule.onNodeWithText("Today", substring = true).assertExists()
        composeTestRule.onNodeWithText("Started ", substring = true).assertExists()
        composeTestRule.onNodeWithText("of estimated time passed", substring = true).assertExists()
    }

    @Test
    fun farOffDate_showsTheYear() {
        setContent(estimatedState(estimate(daysRemaining = 5000, estimatedDate = LocalDate.of(2099, 12, 15))))

        composeTestRule.onNodeWithText("December 15, 2099", substring = true).assertExists()
    }

    @Test
    fun lowConfidence_showsTheLimitedHistoryDisclaimer() {
        setContent(estimatedState(estimate(confidence = EstimateConfidence.LOW)))

        composeTestRule.onNodeWithText("Based on limited history", substring = true).assertExists()
    }

    @Test
    fun mediumConfidence_showsNoDisclaimer() {
        setContent(estimatedState(estimate(confidence = EstimateConfidence.MEDIUM)))
        composeTestRule.onNodeWithText("Based on limited history", substring = true).assertDoesNotExist()
    }

    @Test
    fun highConfidence_showsNoDisclaimer() {
        setContent(estimatedState(estimate(confidence = EstimateConfidence.HIGH)))

        composeTestRule.onNodeWithText("Based on limited history", substring = true).assertDoesNotExist()
    }

    @Test
    fun tappingTheEstimate_opensTheBreakdown_andCloseDismissesIt() {
        setContent(estimatedState())
        composeTestRule.onNodeWithText("Pace breakdown").assertDoesNotExist()

        composeTestRule.onNodeWithText("You'll catch up by", substring = true).performClick()

        composeTestRule.onNodeWithText("Pace breakdown").assertExists()
        composeTestRule.onNodeWithText("Days logged so far").assertExists()
        composeTestRule.onNodeWithText("Average prayers per day").assertExists()
        composeTestRule.onNodeWithText("Remaining prayers").assertExists()
        composeTestRule.onNodeWithText("3287").assertExists()
        composeTestRule.onNodeWithText("Confidence").assertExists()

        composeTestRule.onNodeWithText("Close").performClick()
        composeTestRule.onNodeWithText("Pace breakdown").assertDoesNotExist()
    }

    @Test
    fun theBreakdown_explainsHowTheDateWasWorkedOut() {
        setContent(estimatedState(estimate(daysRemaining = 45, pace = 8.5f)))

        composeTestRule.onNodeWithText("You'll catch up by", substring = true).performClick()

        composeTestRule.onNodeWithText("At 8.5 prayers a day", substring = true).assertExists()
        composeTestRule.onNodeWithText("3287 remaining prayers", substring = true).assertExists()
    }

    @Test
    fun theDateOnScreenUpdatesWhenTheEstimateChanges() {
        val state = mutableStateOf(estimatedState(estimate(daysRemaining = 5000, estimatedDate = LocalDate.of(2099, 12, 15))))
        composeTestRule.setContent {
            QazaTrackerTheme { DashboardContent(uiState = state.value) }
        }
        composeTestRule.onNodeWithText("December 15, 2099", substring = true).assertExists()

        // Logging prayers raises the pace, which pulls the finish date in.
        state.value = estimatedState(estimate(daysRemaining = 4000, estimatedDate = LocalDate.of(2099, 6, 1)))

        composeTestRule.onNodeWithText("June 1, 2099", substring = true).assertExists()
        composeTestRule.onNodeWithText("December 15, 2099", substring = true).assertDoesNotExist()
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

    @Test
    fun tappingAchievementsIcon_invokesCallback() {
        var clicked = false
        setContent(
            uiState = DashboardUiState(rows = rows(completedEach = 0, remainingEach = 10)),
            onAchievementsClicked = { clicked = true }
        )

        composeTestRule.onNodeWithContentDescription("Achievements").performClick()

        assertTrue(clicked)
    }

    @Test
    fun tappingStatisticsIcon_invokesCallback() {
        var clicked = false
        setContent(
            uiState = DashboardUiState(rows = rows(completedEach = 0, remainingEach = 10)),
            onStatisticsClicked = { clicked = true }
        )

        composeTestRule.onNodeWithContentDescription("Statistics").performClick()

        assertTrue(clicked)
    }

    // ---- Streak display ----

    private fun streakState(current: Int, longest: Int) = DashboardUiState(
        rows = rows(completedEach = 0, remainingEach = 10),
        streak = StreakData(
            currentStreak = current,
            longestStreak = longest,
            lastLoggedDate = LocalDate.now()
        )
    )

    @Test
    fun streak_showsCurrentAndLongest_whenActive() {
        setContent(streakState(current = 15, longest = 47))

        composeTestRule.onNodeWithText("15-day streak! 🔥").assertExists()
        composeTestRule.onNodeWithText("Longest streak: 47 days").assertExists()
    }

    @Test
    fun streak_ofOne_usesSingularLongestDay() {
        setContent(streakState(current = 1, longest = 1))

        composeTestRule.onNodeWithText("1-day streak! 🔥").assertExists()
        composeTestRule.onNodeWithText("Longest streak: 1 day").assertExists()
    }

    @Test
    fun brokenStreak_invitesANewOne_andStillShowsTheRecord() {
        setContent(streakState(current = 0, longest = 12))

        composeTestRule.onNodeWithText("Log a prayer today to start a streak").assertExists()
        composeTestRule.onNodeWithText("Longest streak: 12 days").assertExists()
    }

    // ---- Milestone celebration ----

    private fun fajrMilestone() = MilestoneData(
        milestone = PrayerMilestone.COMPLETE_FAJR,
        milestoneAchievedDate = LocalDate.of(2026, 3, 10),
        isNewToday = true
    )

    private fun celebrating() = DashboardUiState(
        rows = rows(completedEach = 0, remainingEach = 10),
        celebrations = listOf(fajrMilestone())
    )

    @Test
    fun milestoneCard_showsTheCelebrationWithPrayerAndDate() {
        setContent(celebrating())

        composeTestRule.onNodeWithText("You completed all Fajr prayers! 🎉").assertExists()
        composeTestRule.onNodeWithText("Achieved", substring = true).assertExists()
        composeTestRule.onNodeWithText("2026", substring = true).assertExists()
    }

    @Test
    fun noCelebration_showsNoMilestoneCard() {
        setContent(DashboardUiState(rows = rows(completedEach = 0, remainingEach = 10)))

        composeTestRule.onNodeWithText("You completed all", substring = true).assertDoesNotExist()
    }

    @Test
    fun tappingTheMilestoneCard_dismissesIt() {
        var dismissed = 0
        setContent(celebrating(), onMilestoneDismissed = { dismissed++ })

        composeTestRule.onNodeWithText("You completed all Fajr prayers! 🎉").performClick()

        assertTrue(dismissed == 1)
    }

    @Test
    fun milestoneCard_dismissesItselfAfterFiveSeconds_notBefore() {
        var dismissed = 0
        setContent(celebrating(), onMilestoneDismissed = { dismissed++ })

        composeTestRule.mainClock.advanceTimeBy(4_500)
        assertTrue("still showing at 4.5s", dismissed == 0)

        composeTestRule.mainClock.advanceTimeBy(1_000)
        assertTrue("auto-dismissed once past 5s", dismissed == 1)
    }

    @Test
    fun noHistoryYet_showsOnlyTheInvitation() {
        setContent(DashboardUiState(rows = rows(completedEach = 0, remainingEach = 10)))

        composeTestRule.onNodeWithText("Log a prayer today to start a streak").assertExists()
        composeTestRule.onNodeWithText("Longest streak: 0 days").assertDoesNotExist()
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
