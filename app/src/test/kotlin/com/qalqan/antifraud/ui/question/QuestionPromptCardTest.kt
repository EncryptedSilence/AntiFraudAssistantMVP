package com.qalqan.antifraud.ui.question

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qalqan.antifraud.settings.QuestionPromptKind
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QuestionPromptCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the §5_5_2 Q1 text and three answer buttons`() {
        composeRule.setContent {
            QuestionPromptCard(
                kind = QuestionPromptKind.CALLER_IDENTITY,
                onAnswerYes = {},
                onAnswerNo = {},
                onAnswerNotSure = {},
                onDontAskAgain = {},
            )
        }
        composeRule
            .onNodeWithText("Did the caller claim to be from a bank, government, or other official service?")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Yes").assertIsDisplayed()
        composeRule.onNodeWithText("No").assertIsDisplayed()
        composeRule.onNodeWithText("Not sure").assertIsDisplayed()
    }

    @Test
    fun `each answer button invokes its callback`() {
        var yes = 0
        var no = 0
        var notSure = 0
        var dontAsk = 0
        composeRule.setContent {
            QuestionPromptCard(
                kind = QuestionPromptKind.PRESSURE,
                onAnswerYes = { yes++ },
                onAnswerNo = { no++ },
                onAnswerNotSure = { notSure++ },
                onDontAskAgain = { dontAsk++ },
            )
        }
        composeRule.onNodeWithText("Yes").performClick()
        composeRule.onNodeWithText("No").performClick()
        composeRule.onNodeWithText("Not sure").performClick()
        composeRule.onNodeWithText("Don't ask again for this campaign").performClick()
        yes shouldBe 1
        no shouldBe 1
        notSure shouldBe 1
        dontAsk shouldBe 1
    }
}
