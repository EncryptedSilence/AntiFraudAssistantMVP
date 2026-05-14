package com.qalqan.antifraud.ui.patterns

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PatternsRouteRendersListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty state renders the empty copy`() {
        composeRule.setContent {
            PatternsRoute(
                state = PatternsUiState(),
                onToggle = { _, _ -> },
                onResetDefaults = {},
            )
        }
        composeRule.onNodeWithText("No patterns yet.").assertIsDisplayed()
    }

    @Test
    fun `renders each pattern row with name + version + source + trigger info`() {
        val rows =
            listOf(
                PatternsUiState.PatternRow(
                    patternId = "fast_attack",
                    name = "Fast attack",
                    category = "bank_fraud",
                    version = "1.0.0",
                    source = PatternsUiState.Source.SEED,
                    enabled = true,
                    triggerCount = 3,
                    lastTriggeredAt = java.time.Instant.parse("2026-04-01T12:00:00Z"),
                ),
            )
        composeRule.setContent {
            PatternsRoute(state = PatternsUiState(rows = rows), onToggle = { _, _ -> }, onResetDefaults = {})
        }
        composeRule.onNodeWithText("Fast attack").assertIsDisplayed()
        composeRule.onNodeWithText("1.0.0").assertIsDisplayed()
        composeRule.onNodeWithText("seed").assertIsDisplayed()
        composeRule.onNodeWithText("Triggered 3 times").assertIsDisplayed()
    }
}
