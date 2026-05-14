package com.qalqan.antifraud.ui.campaign

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CampaignListRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty state renders the §17_1_3 empty copy`() {
        composeRule.setContent {
            CampaignListRoute(state = CampaignsUiState(), onOpenCampaign = {})
        }
        composeRule.onNodeWithText("No campaigns yet.").assertIsDisplayed()
    }

    @Test
    fun `four tabs are visible per §17_2`() {
        composeRule.setContent {
            CampaignListRoute(state = CampaignsUiState(), onOpenCampaign = {})
        }
        composeRule.onNodeWithText("Active").assertIsDisplayed()
        composeRule.onNodeWithText("Closed").assertIsDisplayed()
        composeRule.onNodeWithText("Archived").assertIsDisplayed()
        composeRule.onNodeWithText("False alarm").assertIsDisplayed()
    }
}
