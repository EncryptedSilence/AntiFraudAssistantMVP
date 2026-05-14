package com.qalqan.antifraud.ui.pause

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PauseBeforeActionModalTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders title body and the two buttons when shown`() {
        composeRule.setContent {
            PauseBeforeActionModal(visible = true, onPause = {}, onShowDetails = {})
        }
        composeRule.onNodeWithText("Pause — verify before continuing").assertIsDisplayed()
        composeRule.onNodeWithText("I'll pause").assertIsDisplayed()
        composeRule.onNodeWithText("Show details").assertIsDisplayed()
    }

    @Test
    fun `pause callback fires when the pause button is tapped`() {
        var paused = 0
        composeRule.setContent {
            PauseBeforeActionModal(visible = true, onPause = { paused++ }, onShowDetails = {})
        }
        composeRule.onNodeWithText("I'll pause").performClick()
        paused shouldBe 1
    }

    @Test
    fun `not rendered when visible is false`() {
        composeRule.setContent {
            PauseBeforeActionModal(visible = false, onPause = {}, onShowDetails = {})
        }
        composeRule.onAllNodesWithText("Pause — verify before continuing").fetchSemanticsNodes().size shouldBe 0
    }
}
