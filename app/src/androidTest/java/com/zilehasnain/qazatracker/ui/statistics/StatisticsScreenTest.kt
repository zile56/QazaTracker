package com.zilehasnain.qazatracker.ui.statistics

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.MonthlyStatsData
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.common.displayName
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatisticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val september = YearMonth.of(2026, 9)
    private val july = YearMonth.of(2026, 7)

    /** 5 x 50 owed = 250; completed 25 + 26 + 25 + 26 + 26 = 128 -> 51%. */
    private fun sampleStats(month: YearMonth = september) = MonthlyStatsData(
        month = month,
        prayerStats = mapOf(
            PrayerType.FAJR to 25, PrayerType.DHUHR to 26, PrayerType.ASR to 25,
            PrayerType.MAGHRIB to 26, PrayerType.ISHA to 26
        ),
        prayerTotals = PrayerType.entries.associateWith { 50 },
        totalCompleted = 128,
        totalOwed = 250,
        completionRate = 51.2f
    )

    private fun setContent(
        stats: MonthlyStatsData = sampleStats(),
        selectedMonth: YearMonth = september,
        earliestMonth: YearMonth = july,
        currentMonth: YearMonth = september,
        onBack: () -> Unit = {},
        onMonthChanged: (YearMonth) -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                StatisticsContent(
                    stats = stats,
                    selectedMonth = selectedMonth,
                    earliestMonth = earliestMonth,
                    currentMonth = currentMonth,
                    onBack = onBack,
                    onMonthChanged = onMonthChanged
                )
            }
        }
    }

    @Test
    fun showsTheMonthAsFullNameAndYear() {
        setContent()

        composeTestRule.onNodeWithText("September 2026").assertExists()
    }

    @Test
    fun showsTheTotalLineWithCountsAndPercent() {
        setContent()

        composeTestRule.onNodeWithText("128 / 250 prayers completed (51%)").assertExists()
    }

    @Test
    fun showsEachPrayerTypeWithItsCountOwedAndPercent() {
        setContent()

        // 25/50 = 50%, 26/50 = 52%. Fajr, Asr share a row text, as do Dhuhr, Maghrib, Isha.
        composeTestRule.onAllNodesWithText("25 / 50 (50%)").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("26 / 50 (52%)").assertCountEquals(3)
    }

    @Test
    fun showsAllFivePrayerNames_inTheChartAndTheList() {
        setContent()

        PrayerType.entries.forEach {
            // Once as a chart label, once as a row heading.
            composeTestRule.onAllNodesWithText(it.displayName()).assertCountEquals(2)
        }
    }

    @Test
    fun aQuietMonth_saysNothingWasLogged() {
        setContent(stats = MonthlyStatsData.empty(september))

        composeTestRule.onNodeWithText("No prayers logged in September 2026.").assertExists()
        composeTestRule.onNodeWithText("0 / 0 prayers completed (0%)").assertExists()
    }

    @Test
    fun aBusyMonth_doesNotShowTheQuietMessage() {
        setContent()

        composeTestRule.onNodeWithText("No prayers logged", substring = true).assertDoesNotExist()
    }

    @Test
    fun previousMonth_button_requestsTheMonthBefore() {
        var requested: YearMonth? = null
        setContent(onMonthChanged = { requested = it })

        composeTestRule.onNodeWithContentDescription("Previous month").performClick()

        assertEquals(YearMonth.of(2026, 8), requested)
    }

    @Test
    fun nextMonth_button_requestsTheMonthAfter() {
        var requested: YearMonth? = null
        setContent(selectedMonth = YearMonth.of(2026, 8), onMonthChanged = { requested = it })

        composeTestRule.onNodeWithContentDescription("Next month").performClick()

        assertEquals(september, requested)
    }

    @Test
    fun onTheCurrentMonth_nextIsDisabled_andPreviousIsEnabled() {
        setContent(selectedMonth = september, currentMonth = september, earliestMonth = july)

        composeTestRule.onNodeWithContentDescription("Next month").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Previous month").assertIsEnabled()
    }

    @Test
    fun onTheEarliestMonth_previousIsDisabled_andNextIsEnabled() {
        setContent(
            stats = sampleStats(july),
            selectedMonth = july,
            currentMonth = september,
            earliestMonth = july
        )

        composeTestRule.onNodeWithContentDescription("Previous month").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsEnabled()
    }

    @Test
    fun whenTrackingBeganThisMonth_bothArrowsAreDisabled() {
        setContent(selectedMonth = september, currentMonth = september, earliestMonth = september)

        composeTestRule.onNodeWithContentDescription("Previous month").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsNotEnabled()
    }

    @Test
    fun backButton_invokesCallback() {
        var backPressed = false
        setContent(onBack = { backPressed = true })

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(backPressed)
    }
}
