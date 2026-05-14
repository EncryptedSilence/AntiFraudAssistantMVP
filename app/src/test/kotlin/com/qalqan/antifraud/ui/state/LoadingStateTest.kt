package com.qalqan.antifraud.ui.state

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoadingStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the default loading message and a progress indicator`() {
        composeRule.setContent { LoadingState() }
        composeRule.onNodeWithText("Loading…").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Loading state").assertIsDisplayed()
    }
}
