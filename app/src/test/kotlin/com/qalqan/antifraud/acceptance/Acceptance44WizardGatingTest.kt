package com.qalqan.antifraud.acceptance

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.ui.campaign.CampaignDetailRoute
import com.qalqan.antifraud.ui.campaign.CampaignDetailUiState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #44 — with the "Enable advanced rules" toggle off (default), the campaign
 * screen has NO "Create pattern from this campaign" action.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance44WizardGatingTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val matcher =
        SemanticsMatcher("contentDescription == CreatePattern") { node ->
            node.config
                .getOrNull(SemanticsProperties.ContentDescription)
                ?.contains("CreatePattern") == true
        }

    @Test
    fun `§23 #44 — Create-pattern action is hidden when advancedRulesEnabled is false`() {
        composeRule.setContent {
            CampaignDetailRoute(
                state = CampaignDetailUiState(advancedRulesEnabled = false, band = RiskBand.HIGH),
                onClose = {},
                onFalseAlarm = {},
                onMarkSuspicious = {},
                onExport = {},
                onCreatePattern = {},
            )
        }
        val created = composeRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes()
        created.size shouldBe 0
    }

    @Test
    fun `§23 #44 — Create-pattern action appears when advancedRulesEnabled is true`() {
        composeRule.setContent {
            CampaignDetailRoute(
                state = CampaignDetailUiState(advancedRulesEnabled = true, band = RiskBand.HIGH),
                onClose = {},
                onFalseAlarm = {},
                onMarkSuspicious = {},
                onExport = {},
                onCreatePattern = {},
            )
        }
        val created = composeRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes()
        created.size shouldBe 1
    }
}
