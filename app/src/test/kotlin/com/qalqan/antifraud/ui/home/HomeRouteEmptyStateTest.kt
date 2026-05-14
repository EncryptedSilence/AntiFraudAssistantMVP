package com.qalqan.antifraud.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeRouteEmptyStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty state renders the §17_1_3 watching copy`() {
        composeRule.setContent {
            HomeRoute(
                state = HomeUiState(),
                onSuspiciousCall = {},
                onSuspiciousSms = {},
                onSuspiciousSite = {},
                onOpenCampaign = {},
                onOpenPrivacy = {},
            )
        }
        composeRule.onNodeWithText("Watching for signals — none yet.").assertIsDisplayed()
        composeRule.onNodeWithText("Current risk: All clear").assertIsDisplayed()
    }

    @Test
    fun `empty state renders three §17_1_2 quick-action buttons`() {
        composeRule.setContent {
            HomeRoute(
                state = HomeUiState(),
                onSuspiciousCall = {},
                onSuspiciousSms = {},
                onSuspiciousSite = {},
                onOpenCampaign = {},
                onOpenPrivacy = {},
            )
        }
        composeRule.onNodeWithText("I had a suspicious call").assertIsDisplayed()
        composeRule.onNodeWithText("I had a suspicious SMS").assertIsDisplayed()
        composeRule.onNodeWithText("I had a suspicious site").assertIsDisplayed()
    }
}
