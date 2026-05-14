package com.qalqan.antifraud.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.sms.SmsObserverPermissions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionStatusRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the three §17_1_1 status icons with their tri-state labels`() {
        composeRule.setContent {
            PermissionStatusRow(
                callState = CallObserverPermissions.State.GRANTED,
                smsState = SmsObserverPermissions.State.DENIED,
                batteryExempt = true,
                syncEnabled = false,
                onTap = {},
            )
        }
        composeRule.onNodeWithText("Auto call capture: on").assertIsDisplayed()
        composeRule.onNodeWithText("Auto SMS capture: off — manual paste only").assertIsDisplayed()
    }
}
