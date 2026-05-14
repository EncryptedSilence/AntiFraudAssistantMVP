package com.qalqan.antifraud.alerts

/**
 * Spec §4.4.1 (2) + §17.0.2 — the overlay fires only when a "relevant foreground app" is
 * detected. Stage 9 ships a minimal KZ-bank + browser allowlist; extending the list is
 * post-MVP. The detector is package-name-based and intentionally coarse: it does NOT
 * inspect Activity stacks, content, or any user data.
 *
 * The caller resolves the foreground package via the best-effort path documented in
 * `OverlayGate.foregroundPackage(...)`. When that returns null, [isRelevant] returns
 * false — the overlay won't fire, but the full-screen alert still does.
 */
class RelevantForegroundAppDetector(
    private val allowlist: Set<String> = DEFAULT_ALLOWLIST,
) {
    fun isRelevant(packageName: String?): Boolean {
        if (packageName == null) return false
        return packageName in allowlist
    }

    companion object {
        val DEFAULT_ALLOWLIST: Set<String> =
            setOf(
                // KZ banks
                "kz.kaspi.mobile",
                "kz.halykbank.halyk",
                "kz.beeline.odp",
                "kz.bcc.bccmobile",
                "kz.forte.forte_business",
                "kz.jusan.bank",
                // browsers
                "com.android.chrome",
                "com.google.android.googlequicksearchbox",
                "org.mozilla.firefox",
                "com.opera.browser",
                "com.microsoft.emmx",
                "com.brave.browser",
                "com.sec.android.app.sbrowser",
            )
    }
}
