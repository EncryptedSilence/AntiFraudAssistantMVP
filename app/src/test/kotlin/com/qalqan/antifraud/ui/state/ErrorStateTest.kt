package com.qalqan.antifraud.ui.state

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ErrorStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the default error message and the Open action-log button`() {
        composeRule.setContent { ErrorState(onOpenActionLog = {}) }
        composeRule.onNodeWithText("Something went wrong.").assertIsDisplayed()
        composeRule.onNodeWithText("Open action log").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Error state").assertIsDisplayed()
    }

    @Test
    fun `invokes the report callback when the button is tapped`() {
        var tapped = false
        composeRule.setContent { ErrorState(onOpenActionLog = { tapped = true }) }
        composeRule.onNodeWithText("Open action log").performClick()
        tapped shouldBe true
    }
}
