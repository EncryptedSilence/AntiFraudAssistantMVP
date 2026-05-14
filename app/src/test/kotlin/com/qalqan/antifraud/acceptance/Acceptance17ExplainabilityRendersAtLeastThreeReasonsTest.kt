package com.qalqan.antifraud.acceptance

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.ui.campaign.CampaignDetailRoute
import com.qalqan.antifraud.ui.campaign.CampaignDetailUiState
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #17 — every warning shows ≥ 3 specific reasons (or all available if fewer).
 *
 * The Campaign detail screen renders reasons via [CampaignDetailRoute]. This test pins
 * that the §17.2 "Reasons" section actually exposes ≥ 3 reason rows when there are at
 * least three. Each reason row is tagged with `contentDescription = "Reason"`.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance17ExplainabilityRendersAtLeastThreeReasonsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `§23 #17 — reasons section has ≥ 3 Reason-tagged nodes`() {
        val state =
            CampaignDetailUiState(
                band = RiskBand.HIGH,
                reasons =
                    listOf(
                        "Unknown caller",
                        "OTP requested within 5 minutes",
                        "Pressure tactics",
                        "Lookalike domain",
                    ),
            )
        composeRule.setContent {
            CampaignDetailRoute(
                state = state,
                onClose = {},
                onFalseAlarm = {},
                onMarkSuspicious = {},
                onExport = {},
                onCreatePattern = {},
            )
        }
        val reasonNodes =
            composeRule
                .onAllNodes(
                    SemanticsMatcher("contentDescription == Reason") { node ->
                        node.config
                            .getOrNull(SemanticsProperties.ContentDescription)
                            ?.contains("Reason") == true
                    },
                    useUnmergedTree = true,
                ).fetchSemanticsNodes()
        reasonNodes.size shouldBeGreaterThanOrEqual 3
    }
}
