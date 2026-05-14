package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.AntifraudApp
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #2 — first launch reaches the home screen without any account prompt.
 *
 * Concretely: no Compose node anywhere in the first-launch tree carries account-related
 * copy ("Sign in", "Log in", "Register", "Account"). The onboarding flow is the
 * intermediate stop, but it is permission-grants only — no account creation.
 *
 * Stage 8 wiring contract adaptation: the FULL_SCREEN_INTENT and OVERLAY_WINDOW
 * onboarding-step justifications mirror the Stage 9 AlertPermissionRequester
 * justifications and add no account-related copy.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance2NoRegistrationOnFirstLaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var repos: Repositories

    @Before
    fun setUp() {
        repos = Repositories.inMemory(context)
    }

    @After
    fun tearDown() {
        composeRule.waitForIdle()
        repos.close()
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `§23 #2 — onboarding has none of the forbidden account-related strings`() {
        UserSettings(context).onboardingCompleted = false
        composeRule.setContent { AntifraudApp(repos = repos) }
        composeRule.waitForIdle()
        listOf("Sign in", "Log in", "Register", "Account").forEach { forbidden ->
            val nodes =
                composeRule.onAllNodesWithText(forbidden, useUnmergedTree = true)
                    .fetchSemanticsNodes()
            check(nodes.isEmpty()) { "Forbidden first-launch copy found: $forbidden" }
        }
    }
}
