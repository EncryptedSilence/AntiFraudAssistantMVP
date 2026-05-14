package com.qalqan.antifraud

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.ui.nav.AntifraudDestination
import com.qalqan.antifraud.ui.nav.AntifraudNavGraph

/**
 * Spec §17 — root Compose entry point. Constructs a process-scoped [Repositories] keyed by
 * the application context. The optional [repos] override exists for tests; production callers
 * never pass it.
 */
@Composable
fun AntifraudApp(
    startDestination: String = AntifraudDestination.Home.route,
    repos: Repositories? = null,
) {
    val context = LocalContext.current
    val resolved =
        repos ?: remember(context) { Repositories.build(context.applicationContext) }
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AntifraudNavGraph(repos = resolved, startDestination = startDestination)
        }
    }
}
