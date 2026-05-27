package com.qalqan.antifraud.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeRouteEmptyStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty state renders the §17_1_3 watching copy and all-clear band`() {
        composeRule.setContent {
            HomeRoute(
                state = HomeUiState(),
                onOpenCampaign = {},
            )
        }
        composeRule.onNodeWithText("All clear").assertIsDisplayed()
        composeRule.onNodeWithText("Watching for signals — none yet.")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
