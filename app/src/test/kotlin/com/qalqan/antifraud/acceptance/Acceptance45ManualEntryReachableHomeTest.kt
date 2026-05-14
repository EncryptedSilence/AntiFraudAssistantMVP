package com.qalqan.antifraud.acceptance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.sms.SmsObserverPermissions
import com.qalqan.antifraud.ui.home.HomeRoute
import com.qalqan.antifraud.ui.home.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Acceptance45ManualEntryReachableHomeTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Spec §23 #45 — "I had a suspicious call / SMS / site" buttons reachable in at most one
     * tap from any main tab AND work even when all auto-capture permissions are denied.
     */
    @Test
    fun `§23 #45 — three quick-action buttons render with all permissions denied`() {
        composeRule.setContent {
            HomeRoute(
                state =
                    HomeUiState(
                        callPermissionState = CallObserverPermissions.State.DENIED,
                        smsPermissionState = SmsObserverPermissions.State.DENIED,
                    ),
                onSuspiciousCall = {},
                onSuspiciousSms = {},
                onSuspiciousSite = {},
                onOpenCampaign = {},
                onOpenPrivacy = {},
            )
        }
        composeRule.onNodeWithText("I had a suspicious call").assertIsDisplayed()
        composeRule.onNodeWithText("I had a suspicious SMS").assertIsDisplayed()
        composeRule.onNodeWithText("I had a suspicious site").assertIsDisplayed()
    }
}
