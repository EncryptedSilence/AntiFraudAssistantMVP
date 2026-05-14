package com.qalqan.antifraud.ui.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.settings.OnboardingStep
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the current step title plus the Grant and Skip buttons`() {
        composeRule.setContent {
            OnboardingRoute(
                state =
                    OnboardingUiState(
                        currentStep = OnboardingStep.NOTIFICATIONS,
                        stepIndex = 1,
                        totalSteps = 7,
                    ),
                onGrant = {},
                onSkip = {},
                onFinish = {},
            )
        }
        composeRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeRule.onNodeWithText("Step 1 of 7").assertIsDisplayed()
        composeRule.onNodeWithText("Grant").assertIsDisplayed()
        composeRule.onNodeWithText("Skip").assertIsDisplayed()
    }

    @Test
    fun `finish-screen renders the Finish button when currentStep is null`() {
        composeRule.setContent {
            OnboardingRoute(
                state = OnboardingUiState(currentStep = null, stepIndex = 0, totalSteps = 7),
                onGrant = {},
                onSkip = {},
                onFinish = {},
            )
        }
        composeRule.onNodeWithText("Finish").assertIsDisplayed()
    }
}
