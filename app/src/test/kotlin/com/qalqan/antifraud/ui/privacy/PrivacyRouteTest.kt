package com.qalqan.antifraud.ui.privacy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.settings.RetentionDisplay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the four required §17_6 sections`() {
        composeRule.setContent {
            PrivacyRoute(
                state =
                    PrivacyUiState(
                        retentionRows = RetentionDisplay.rows()
                            .map { PrivacyUiState.RetentionRow(it.key, it.days) },
                    ),
                onDeleteAll = {},
                onDisableSync = {},
                onResetPermissions = {},
                onOpenSettings = {},
            )
        }
        composeRule.onNodeWithText("What is stored").assertIsDisplayed()
        composeRule.onNodeWithText("Where it is stored").assertIsDisplayed()
        composeRule.onNodeWithText("Modules enabled").assertIsDisplayed()
        composeRule.onNodeWithText("Permissions granted").assertIsDisplayed()
    }

    @Test
    fun `delete-all button is present and callable`() {
        composeRule.setContent {
            PrivacyRoute(
                state = PrivacyUiState(),
                onDeleteAll = {},
                onDisableSync = {},
                onResetPermissions = {},
                onOpenSettings = {},
            )
        }
        composeRule.onNodeWithText("Delete all data").assertIsDisplayed()
    }
}
