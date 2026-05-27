package com.qalqan.antifraud.acceptance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.ui.add.AddRoute
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Acceptance45ManualEntryReachableHomeTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Spec §23 #45 — the "I had a suspicious call / SMS / site" affordances are reachable in at
     * most one tap (the dedicated Add tab) and do not depend on any auto-capture permission.
     * The Add screen renders all three regardless of permission state.
     */
    @Test
    fun `§23 #45 — three manual-entry actions render on the Add tab`() {
        composeRule.setContent {
            AddRoute(onAddCall = {}, onAddSms = {}, onAddSite = {})
        }
        composeRule.onNodeWithText("I had a suspicious call").assertIsDisplayed()
        composeRule.onNodeWithText("I had a suspicious SMS").assertIsDisplayed()
        composeRule.onNodeWithText("I had a suspicious site").assertIsDisplayed()
    }
}
