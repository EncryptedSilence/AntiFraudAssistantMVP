package com.qalqan.antifraud.ui.add

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `tapping each add action invokes the corresponding callback`() {
        var call = 0
        var sms = 0
        var site = 0
        composeRule.setContent {
            AddRoute(
                onAddCall = { call++ },
                onAddSms = { sms++ },
                onAddSite = { site++ },
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
