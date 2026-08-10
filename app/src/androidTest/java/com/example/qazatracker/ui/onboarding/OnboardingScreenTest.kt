package com.example.qazatracker.ui.onboarding

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.qazatracker.domain.model.BaselineCalculation
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.ui.theme.QazaTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: OnboardingUiState,
        onSelectMethod: (CalculationMethod) -> Unit = {},
        onContinueClicked: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QazaTrackerTheme {
                OnboardingContent(
                    uiState = uiState,
                    onSelectMethod = onSelectMethod,
                    onDateFromSelected = {},
                    onDateToSelected = {},
                    onAgeNowChanged = {},
                    onAgeObligatoryChanged = {},
                    onContinueClicked = onContinueClicked
                )
            }
        }
    }

    @Test
    fun continueButton_isDisabled_whenNoMethodSelected() {
        setContent(uiState = OnboardingUiState())

        composeTestRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun selectingAgeEstimateCard_notifiesCallbackWithCorrectMethod() {
        var selected: CalculationMethod? = null
        setContent(
            uiState = OnboardingUiState(),
            onSelectMethod = { selected = it }
        )

        composeTestRule.onNodeWithText("Estimate by age").performClick()

        assertEquals(CalculationMethod.AGE_ESTIMATE, selected)
    }

    @Test
    fun continueButton_isEnabled_withValidAgeEstimateInput() {
        setContent(
            uiState = OnboardingUiState(
                method = CalculationMethod.AGE_ESTIMATE,
                ageNowInput = "30",
                ageObligatoryInput = "12"
            )
        )

        composeTestRule.onNodeWithText("Continue").assertIsEnabled()
    }

    @Test
    fun continueButton_isDisabled_whenObligatoryAgeNotLessThanCurrentAge() {
        setContent(
            uiState = OnboardingUiState(
                method = CalculationMethod.AGE_ESTIMATE,
                ageNowInput = "10",
                ageObligatoryInput = "12"
            )
        )

        composeTestRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun clickingContinue_invokesCallback_whenEnabled() {
        var clicked = false
        setContent(
            uiState = OnboardingUiState(
                method = CalculationMethod.AGE_ESTIMATE,
                ageNowInput = "30",
                ageObligatoryInput = "12"
            ),
            onContinueClicked = { clicked = true }
        )

        composeTestRule.onNodeWithText("Continue").performClick()

        assertTrue(clicked)
    }

    @Test
    fun calculatedResult_isShownOnScreen() {
        val result = BaselineCalculation(
            missedDays = 100,
            countsByPrayerType = PrayerType.entries.associateWith { 100 },
            method = CalculationMethod.AGE_ESTIMATE
        )
        setContent(
            uiState = OnboardingUiState(
                method = CalculationMethod.AGE_ESTIMATE,
                ageNowInput = "30",
                ageObligatoryInput = "12",
                result = result
            )
        )

        composeTestRule.onNodeWithText("Estimated total").assertExists()
        composeTestRule.onNodeWithText("500 prayers across 100 days").assertExists()
    }
}
