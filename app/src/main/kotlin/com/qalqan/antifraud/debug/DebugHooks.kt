package com.qalqan.antifraud.debug

import com.qalqan.antifraud.database.Repositories

/**
 * Seam for debug-only behaviour injected into the main source set. Components in `src/debug`
 * populate these hooks at startup (see DebugInitProvider); they stay null in release builds, where
 * the debug source set — and the seed logic it carries — is never compiled in.
 *
 * The Campaigns screen gates its "seed demo chains" affordance on [seedDemoChains] being non-null,
 * so the button is present only in debug builds without any `BuildConfig` plumbing.
 */
object DebugHooks {
    /**
     * Seeds synthetic risk chains into [Repositories] and returns the number created. Non-null only
     * in debug builds. Suspends so the caller can await completion before refreshing the UI.
     */
    @Volatile
    var seedDemoChains: (suspend (Repositories) -> Int)? = null
}
