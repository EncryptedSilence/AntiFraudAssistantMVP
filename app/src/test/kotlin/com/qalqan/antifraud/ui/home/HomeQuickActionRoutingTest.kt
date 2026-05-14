package com.qalqan.antifraud.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeQuickActionRoutingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `tapping each quick-action button invokes the corresponding callback`() {
        var call = 0
        var sms = 0
        var site = 0
        composeRule.setContent {
            HomeRoute(
                state = HomeUiState(),
                onSuspiciousCall = { call++ },
                onSuspiciousSms = { sms++ },
                onSuspiciousSite = { site++ },
                onOpenCampaign = {},
                onOpenPrivacy = {},
            )
        }
        composeRule.onNodeWithText("I had a suspicious call").performClick()
        composeRule.onNodeWithText("I had a suspicious SMS").performClick()
        composeRule.onNodeWithText("I had a suspicious site").performClick()
        call shouldBe 1
        sms shouldBe 1
        site shouldBe 1
    }
}
