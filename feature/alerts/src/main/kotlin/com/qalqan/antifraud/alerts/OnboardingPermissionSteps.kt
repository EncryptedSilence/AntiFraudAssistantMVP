package com.qalqan.antifraud.alerts

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Spec §22 Stage 8 — Stage 9 contribution to the onboarding flow. The Stage 8
 * `OnboardingSequencer` calls into [nextRequiredStep] after the user has cleared
 * notifications + phone + call log + SMS. Steps fire in the spec-§22 order:
 *
 *   1. [Step.FullScreenIntent]   — Android 14+ only; pre-14 is a normal permission.
 *   2. [Step.SystemAlertWindow]  — Settings deep-link via [Settings.ACTION_MANAGE_OVERLAY_PERMISSION].
 *   3. [Step.None]               — both granted; remaining onboarding steps (battery exemption) are
 *                                  Stage 8's responsibility.
 *
 * `:feature:alerts` does NOT depend on `:app`; the Stage 8 sequencer (in `:app`) calls
 * [nextRequiredStep] from `MainActivity.onResume` and uses the returned [Intent] (if any)
 * with its own `ActivityResultLauncher`. The launcher result is forwarded back to
 * [AlertPermissionResultLogger.log] for action-log bookkeeping.
 */
object OnboardingPermissionSteps {
    sealed interface Step {
        val intent: Intent?

        data class FullScreenIntent(override val intent: Intent) : Step

        data class SystemAlertWindow(override val intent: Intent) : Step

        data object None : Step {
            override val intent: Intent? = null
        }
    }

    fun nextRequiredStep(context: Context): Step {
        val pkg = context.packageName
        val fsi = AlertPermissionRequester.fullScreenIntentSettingsIntent(pkg)
        val fsGate = FullScreenIntentPermissionGate(context)
        if (fsi != null && !fsGate.fullScreenAllowed()) {
            return Step.FullScreenIntent(fsi)
        }
        if (!Settings.canDrawOverlays(context)) {
            return Step.SystemAlertWindow(AlertPermissionRequester.overlaySettingsIntent(pkg))
        }
        return Step.None
    }
}
