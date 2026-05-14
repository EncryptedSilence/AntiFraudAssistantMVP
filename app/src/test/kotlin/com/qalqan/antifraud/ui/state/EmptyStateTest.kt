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
class EmptyStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the default empty message`() {
        composeRule.setContent { EmptyState() }
        composeRule.onNodeWithText("No data yet.").assertIsDisplayed()
    }

    @Test
    fun `exposes a contentDescription for screen readers per §17_7 accessibility`() {
        composeRule.setContent { EmptyState() }
        composeRule.onNodeWithContentDescription("Empty state").assertIsDisplayed()
    }
}
