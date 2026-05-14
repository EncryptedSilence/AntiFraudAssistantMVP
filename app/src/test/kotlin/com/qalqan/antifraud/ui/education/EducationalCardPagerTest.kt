package com.qalqan.antifraud.ui.education

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EducationalCardPagerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `pager renders the first card by default`() {
        composeRule.setContent {
            EducationalCardPager(visible = true, onDismiss = {})
        }
        composeRule.onNodeWithText("Never share an SMS code").assertIsDisplayed()
    }

    @Test
    fun `pager hidden when visible is false`() {
        composeRule.setContent {
            EducationalCardPager(visible = false, onDismiss = {})
        }
        composeRule.onAllNodesWithText("Never share an SMS code").fetchSemanticsNodes().size shouldBe 0
    }
}
