package com.qalqan.antifraud.ui.references

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReferencesRouteTabsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `four §17_4 tabs are visible`() {
        composeRule.setContent { ReferencesRoute(state = ReferencesUiState()) }
        composeRule.onNodeWithText("Numbers").assertIsDisplayed()
        composeRule.onNodeWithText("Domains").assertIsDisplayed()
        composeRule.onNodeWithText("SMS categories").assertIsDisplayed()
        composeRule.onNodeWithText("Official").assertIsDisplayed()
    }

    @Test
    fun `empty state shows when no bundle applied`() {
        composeRule.setContent { ReferencesRoute(state = ReferencesUiState(lastBundleAt = null)) }
        composeRule.onNodeWithText("No bundle applied").assertIsDisplayed()
    }
}
