package com.qalqan.antifraud.ui.campaign

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.domain.RiskBand
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CampaignDetailRouteRendersFieldsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val sampleState =
        CampaignDetailUiState(
            campaignId = "c1",
            startedAt = java.time.Instant.parse("2026-01-01T10:00:00Z"),
            lastEventAt = java.time.Instant.parse("2026-01-02T11:00:00Z"),
            band = RiskBand.HIGH,
            linkedEvents = listOf("Call @ 2026-01-01 10:00:00Z", "SMS @ 2026-01-01 10:05:00Z"),
            triggeredPatterns = listOf("fast_attack"),
            reasons = listOf("Unknown caller", "OTP requested within 5 minutes", "Pressure tactics"),
            pendingQuestions = emptyList(),
            recommendations = emptyList(),
            advancedRulesEnabled = false,
        )

    @Test
    fun `renders all §17_2 sections`() {
        composeRule.setContent {
            CampaignDetailRoute(
                state = sampleState,
                onClose = {},
                onFalseAlarm = {},
                onMarkSuspicious = {},
                onExport = {},
                onCreatePattern = {},
            )
        }
        composeRule.onNodeWithText("Linked events").assertIsDisplayed()
        composeRule.onNodeWithText("Triggered patterns").assertIsDisplayed()
        composeRule.onNodeWithText("Reasons").assertIsDisplayed()
        composeRule.onAllNodesWithText("Unknown caller").onFirst().assertIsDisplayed()
    }
}
