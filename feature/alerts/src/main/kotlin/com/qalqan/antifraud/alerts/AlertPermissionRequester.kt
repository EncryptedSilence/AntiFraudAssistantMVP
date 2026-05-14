package com.qalqan.antifraud.alerts

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Spec §22 Stage 8 onboarding — requests USE_FULL_SCREEN_INTENT and SYSTEM_ALERT_WINDOW
 * with a one-line justification each. The actual `ActivityResultLauncher` plumbing lives
 * in `:app.MainActivity` (wired by Stage 8's `OnboardingSequencer`); this class supplies
 * the deep-link intents and the justification strings.
 */
object AlertPermissionRequester {
    val justifications: Map<String, String> =
        mapOf(
            "android.permission.USE_FULL_SCREEN_INTENT" to
                "Lets us show a full-screen warning while you are on a call.",
            "android.permission.SYSTEM_ALERT_WINDOW" to
                "Lets us show a thin warning banner on top of banking apps.",
        )

    fun overlaySettingsIntent(packageName: String): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )

    fun fullScreenIntentSettingsIntent(packageName: String): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:$packageName"),
            )
        } else {
            null
        }
}
