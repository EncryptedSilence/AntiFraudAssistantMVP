package com.qalqan.antifraud.acceptance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.ui.home.HomeRoute
import com.qalqan.antifraud.ui.home.HomeUiState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §11.5 — pause-before-action modal at critical risk.
 *
 * Stage 8 surfaces the modal on the Home screen when the most-recent-active-campaign
 * band is CRITICAL. At HIGH / MEDIUM / LOW (or null = "All clear") the modal is absent.
 */
@RunWith(RobolectricTestRunner::class)
class PauseBeforeActionAtCriticalTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `§11_5 — CRITICAL band shows the pause modal on Home`() {
        composeRule.setContent {
            HomeRoute(
                state = HomeUiState(currentBand = RiskBand.CRITICAL),
                onSuspiciousCall = {},
                onSuspiciousSms = {},
                onSuspiciousSite = {},
                onOpenCampaign = {},
                onOpenPrivacy = {},
            )
        }
        composeRule.onNodeWithText("Pause — verify before continuing").assertIsDisplayed()
    }

    @Test
    fun `§11_5 — HIGH band does NOT show the pause modal`() {
        composeRule.setContent {
            HomeRoute(
                state = HomeUiState(currentBand = RiskBand.HIGH),
                onSuspiciousCall = {},
                onSuspiciousSms = {},
                onSuspiciousSite = {},
                onOpenCampaign = {},
                onOpenPrivacy = {},
            )
        }
        composeRule.onAllNodesWithText("Pause — verify before continuing")
            .fetchSemanticsNodes()
            .size shouldBe 0
    }
}
