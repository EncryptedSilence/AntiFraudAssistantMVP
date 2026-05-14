package com.qalqan.antifraud.ui.campaign

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.QuestionPromptKind
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CampaignDetailQuestionPromptTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `pending prompt renders inside the Campaign detail screen`() {
        composeRule.setContent {
            CampaignDetailRoute(
                state =
                    CampaignDetailUiState(
                        band = RiskBand.HIGH,
                        pendingPrompt = QuestionPromptKind.CALLER_IDENTITY,
                    ),
                onClose = {},
                onFalseAlarm = {},
                onMarkSuspicious = {},
                onExport = {},
                onCreatePattern = {},
            )
        }
        composeRule
            .onNodeWithText("Did the caller claim to be from a bank, government, or other official service?")
            .assertIsDisplayed()
    }
}
