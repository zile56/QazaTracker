package com.zilehasnain.qazatracker.ui.achievements

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zilehasnain.qazatracker.domain.model.AchievementsData
import com.zilehasnain.qazatracker.domain.model.MilestoneAchievement
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.ProgressAchievement
import com.zilehasnain.qazatracker.domain.model.StreakAchievement
import com.zilehasnain.qazatracker.ui.theme.QazaTrackerTheme
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AchievementsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate.of(2026, 9, 24)

    private fun sample() = AchievementsData(
        milestones = listOf(
            MilestoneAchievement(PrayerType.ISHA, LocalDate.of(2026, 9, 20)),
            MilestoneAchievement(PrayerType.FAJR, LocalDate.of(2026, 9, 15))
        ),
        streaks = listOf(
            StreakAchievement(47, today, isCurrent = true),
            StreakAchievement(52, LocalDate.of(2026, 7, 1), isCurrent = false)
        ),
        progressBadges = listOf(
            ProgressAchievement(25, LocalDate.of(2026, 8, 1)),
            ProgressAchievement(50, LocalDate.of(2026, 9, 16))
        )
    )

    private fun setContent(
        data: AchievementsData = sample(),
        hasLoaded: Boolean = true,
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                AchievementsContent(achievements = data, onBack = onBack, hasLoaded = hasLoaded, today = today)
            }
        }
    }

    // ---- Milestones ----

    @Test
    fun milestones_showEachCompletedPrayerWithItsDate() {
        setContent()

        composeTestRule.onNodeWithText("All Isha Prayers Completed 🎉").assertExists()
        composeTestRule.onNodeWithText("All Fajr Prayers Completed 🎉").assertExists()
        composeTestRule.onNodeWithText("Unlocked on: September 20").assertExists()
        composeTestRule.onNodeWithText("Unlocked on: September 15").assertExists()
    }

    // ---- Streaks ----

    @Test
    fun streaks_showCurrentWithBestForComparison() {
        setContent()

        composeTestRule.onNodeWithText("47-day streak 🔥").assertExists()
        composeTestRule.onNodeWithText("Best: 52 days").assertExists()
    }

    @Test
    fun streaks_showTheBestStreakItselfWhenItWasAnEarlierRun() {
        setContent()

        composeTestRule.onNodeWithText("Best streak: 52 days ⭐").assertExists()
        composeTestRule.onNodeWithText("Unlocked on: July 1").assertExists()
    }

    @Test
    fun aStreakThatIsAlsoTheBest_showsBestAsItsOwnLength() {
        setContent(
            AchievementsData(
                milestones = emptyList(),
                streaks = listOf(StreakAchievement(12, today, isCurrent = true)),
                progressBadges = emptyList()
            )
        )

        composeTestRule.onNodeWithText("12-day streak 🔥").assertExists()
        composeTestRule.onNodeWithText("Best: 12 days").assertExists()
        composeTestRule.onNodeWithText("Best streak:", substring = true).assertDoesNotExist()
    }

    @Test
    fun aBrokenStreak_showsJustTheBest() {
        setContent(
            AchievementsData(
                milestones = emptyList(),
                streaks = listOf(StreakAchievement(9, LocalDate.of(2026, 8, 3), isCurrent = false)),
                progressBadges = emptyList()
            )
        )

        composeTestRule.onNodeWithText("Best streak: 9 days ⭐").assertExists()
        composeTestRule.onNodeWithText("-day streak", substring = true).assertDoesNotExist()
    }

    // ---- Progress ----

    @Test
    fun progress_showsEachUnlockedBadgeWithItsDate() {
        setContent()

        composeTestRule.onNodeWithText("25% complete ✓").assertExists()
        composeTestRule.onNodeWithText("50% complete ✓").assertExists()
        composeTestRule.onNodeWithText("Unlocked on: August 1").assertExists()
    }

    @Test
    fun progress_doesNotShowBadgesThatAreNotUnlocked() {
        setContent()

        composeTestRule.onNodeWithText("75% complete", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("100% complete", substring = true).assertDoesNotExist()
    }

    @Test
    fun aFullClear_showsAllFourBadges() {
        setContent(
            AchievementsData(
                milestones = emptyList(),
                streaks = emptyList(),
                progressBadges = listOf(25, 50, 75, 100).map { ProgressAchievement(it, today) }
            )
        )

        listOf(25, 50, 75, 100).forEach {
            composeTestRule.onNodeWithText("$it% complete ✓").assertExists()
        }
    }

    // ---- The three sections and the hero ----

    @Test
    fun allThreeSectionsAreShownWhenEachHasSomething() {
        setContent()

        composeTestRule.onNodeWithText("Milestones", substring = true).assertExists()
        composeTestRule.onNodeWithText("Streaks", substring = true).assertExists()
        composeTestRule.onNodeWithText("Progress", substring = true).assertExists()
    }

    @Test
    fun aSectionWithNothingInItIsLeftOut() {
        setContent(
            AchievementsData(
                milestones = emptyList(),
                streaks = listOf(StreakAchievement(3, today, isCurrent = true)),
                progressBadges = emptyList()
            )
        )

        composeTestRule.onNodeWithText("Streaks", substring = true).assertExists()
        composeTestRule.onNodeWithText("Milestones", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Progress", substring = true).assertDoesNotExist()
    }

    @Test
    fun theHeroCountMatchesTheCardsOnScreen() {
        setContent() // 2 milestones + 2 streaks + 2 progress = 6 cards

        composeTestRule.onNodeWithText("6 achievements unlocked").assertExists()
    }

    @Test
    fun theHeroShowsTheTitle() {
        setContent()

        composeTestRule.onNodeWithText("Achievements").assertExists()
    }

    // ---- Empty and loading ----

    @Test
    fun withNothingUnlocked_showsTheEmptyState() {
        setContent(AchievementsData.Empty)

        composeTestRule.onNodeWithText("No achievements yet").assertExists()
        composeTestRule.onNodeWithText("Nothing unlocked yet").assertExists()
        composeTestRule.onNodeWithText("Milestones", substring = true).assertDoesNotExist()
    }

    @Test
    fun theEmptyStateIsNotShownWhenThereAreAchievements() {
        setContent()

        composeTestRule.onNodeWithText("No achievements yet").assertDoesNotExist()
    }

    @Test
    fun whileLoading_neitherTheEmptyStateNorAnyCardsAreShown() {
        setContent(AchievementsData.Empty, hasLoaded = false)

        composeTestRule.onNodeWithText("No achievements yet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Achievements").assertExists()
    }

    @Test
    fun onceLoaded_theEmptyStateAppears() {
        val loaded = mutableStateOf(false)
        composeTestRule.setContent {
            QazaTrackerTheme {
                AchievementsContent(
                    achievements = AchievementsData.Empty,
                    onBack = {},
                    hasLoaded = loaded.value,
                    today = today
                )
            }
        }
        composeTestRule.onNodeWithText("No achievements yet").assertDoesNotExist()

        loaded.value = true

        composeTestRule.onNodeWithText("No achievements yet").assertExists()
    }

    // ---- Navigation ----

    @Test
    fun backButton_invokesCallback() {
        var backPressed = false
        setContent(onBack = { backPressed = true })

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(backPressed)
    }
}
