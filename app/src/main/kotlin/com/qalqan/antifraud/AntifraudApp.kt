package com.qalqan.antifraud

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import com.qalqan.antifraud.ui.nav.AntifraudDestination
import com.qalqan.antifraud.ui.nav.AntifraudNavGraph

/**
 * Spec §17 + §22 Stage 8 — root Compose entry point. Constructs a process-scoped
 * [Repositories] and routes to either [AntifraudDestination.Onboarding] (first launch)
 * or [AntifraudDestination.Home] (returning users) based on
 * [UserSettings.onboardingCompleted].
 */
@Composable
fun AntifraudApp(repos: Repositories? = null) {
    val context = LocalContext.current
    val resolvedRepos =
        repos ?: remember(context) { Repositories.build(context.applicationContext) }
    val userSettings = remember(context) { UserSettings(context.applicationContext) }
    val startDestination =
        if (userSettings.onboardingCompleted) {
            AntifraudDestination.Home.route
        } else {
            AntifraudDestination.Onboarding.route
        }
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AntifraudNavGraph(repos = resolvedRepos, startDestination = startDestination)
        }
    }
}
