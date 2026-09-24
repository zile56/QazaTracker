package com.zilehasnain.qazatracker.ui.history

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.HistoryEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fixedInstant = Instant.parse("2026-03-01T12:00:00Z")

    private fun setContent(uiState: HistoryUiState, onBack: () -> Unit = {}) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                HistoryContent(uiState = uiState, onBack = onBack)
            }
        }
    }

    @Test
    fun emptyState_showsPlaceholderMessage() {
        setContent(uiState = HistoryUiState(entries = emptyList()))

        composeTestRule.onNodeWithText("Nothing logged yet.").assertExists()
    }

    @Test
    fun backButton_invokesCallback() {
        var backPressed = false
        setContent(uiState = HistoryUiState(), onBack = { backPressed = true })

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(backPressed)
    }

    @Test
    fun singleCompletion_rendersCompletedTagAndTitle() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(HistoryEntry.SingleCompletion(PrayerType.FAJR, fixedInstant))
            )
        )

        composeTestRule.onNodeWithText("Fajr completed").assertExists()
        composeTestRule.onNodeWithText("Completed").assertExists()
    }

    // ---- Batch-collapsing must be visible as ONE row, not N ----

    @Test
    fun batchCompletion_rendersAsOneRowWithCorrectSummary() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(
                    HistoryEntry.BatchCompletion(
                        batchId = "batch-1",
                        prayerTypes = PrayerType.entries.toSet(),
                        days = 5,
                        totalCount = 25,
                        timestamp = fixedInstant
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("5 days × all prayers logged").assertExists()
        composeTestRule.onNodeWithText("Batch").assertExists()
    }

    @Test
    fun batchCompletion_withPartialPrayerTypes_listsThemByName() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(
                    HistoryEntry.BatchCompletion(
                        batchId = "batch-2",
                        prayerTypes = setOf(PrayerType.FAJR, PrayerType.ISHA),
                        days = 3,
                        totalCount = 6,
                        timestamp = fixedInstant
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("3 days × Fajr, Isha logged").assertExists()
    }

    @Test
    fun singleDayBatch_usesSingularDayWord() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(
                    HistoryEntry.BatchCompletion(
                        batchId = "batch-3",
                        prayerTypes = setOf(PrayerType.DHUHR),
                        days = 1,
                        totalCount = 1,
                        timestamp = fixedInstant
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("1 day × Dhuhr logged").assertExists()
    }

    // ---- Adjustments must show their reason correctly ----

    @Test
    fun adjustment_showsManualCorrectionReasonAsTag() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(
                    HistoryEntry.Adjustment(
                        prayerType = PrayerType.MAGHRIB,
                        delta = -5,
                        reason = AdjustmentReason.MANUAL_CORRECTION,
                        note = null,
                        timestamp = fixedInstant
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("Maghrib adjusted -5").assertExists()
        composeTestRule.onNodeWithText("Manual correction").assertExists()
    }

    @Test
    fun adjustment_showsExemptionReasonAsTag() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(
                    HistoryEntry.Adjustment(
                        prayerType = PrayerType.ASR,
                        delta = -3,
                        reason = AdjustmentReason.EXEMPTION,
                        note = null,
                        timestamp = fixedInstant
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("Exemption").assertExists()
    }

    @Test
    fun adjustment_showsRecalculationReasonAsTag_andPositiveDeltaWithSign() {
        setContent(
            uiState = HistoryUiState(
                entries = listOf(
                    HistoryEntry.Adjustment(
                        prayerType = PrayerType.ISHA,
                        delta = 10,
                        reason = AdjustmentReason.RECALCULATION,
                        note = null,
                        timestamp = fixedInstant
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("Isha adjusted +10").assertExists()
        composeTestRule.onNodeWithText("Recalculation").assertExists()
    }
}
