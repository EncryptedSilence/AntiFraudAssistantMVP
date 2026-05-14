package com.qalqan.antifraud.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.scoring.Sensitivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRouteRendersTogglesTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders four sensitivity levels and all §18 toggles`() {
        composeRule.setContent {
            SettingsRoute(
                state = SettingsUiState(sensitivity = Sensitivity.STANDARD),
                onSensitivityChange = {},
                onToggleChange = { _, _ -> },
            )
        }
        // The screen is a tall verticalScroll; assertExists checks node presence
        // independent of viewport visibility.
        composeRule.onNodeWithText("Low").assertExists()
        composeRule.onNodeWithText("Standard").assertExists()
        composeRule.onNodeWithText("High").assertExists()
        composeRule.onNodeWithText("Maximum").assertExists()
        composeRule.onNodeWithText("Call analysis").assertExists()
        composeRule.onNodeWithText("Enable advanced rules").assertExists()
    }
}
