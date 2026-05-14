package com.qalqan.antifraud.ui.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccessibleTouchTargetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `accessibleTouchTarget enforces 48 dp minimum per §17_7 accessibility`() {
        composeRule.setContent {
            Box(
                modifier =
                    Modifier
                        .testTag("target")
                        .accessibleTouchTarget()
                        .size(8.dp),
            ) { Text("x") }
        }
        composeRule.onNodeWithTag("target").assertWidthIsAtLeast(48.dp)
        composeRule.onNodeWithTag("target").assertHeightIsAtLeast(48.dp)
    }
}
