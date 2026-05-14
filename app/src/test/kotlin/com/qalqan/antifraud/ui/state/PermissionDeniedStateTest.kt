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
class PermissionDeniedStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the default permission-denied message and an Open-settings button`() {
        composeRule.setContent { PermissionDeniedState(onOpenSettings = {}) }
        composeRule.onNodeWithText("Permission required.").assertIsDisplayed()
        composeRule.onNodeWithText("Open settings").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Permission-denied state").assertIsDisplayed()
    }

    @Test
    fun `invokes the open-settings callback when tapped`() {
        var tapped = false
        composeRule.setContent { PermissionDeniedState(onOpenSettings = { tapped = true }) }
        composeRule.onNodeWithText("Open settings").performClick()
        tapped shouldBe true
    }
}
