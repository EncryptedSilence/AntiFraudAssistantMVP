package com.qalqan.antifraud.settings

/**
 * Spec §22 Stage 8 — computes the ordered list of [OnboardingStep] for the current SDK
 * level, and the next step to prompt given the already-granted set.
 *
 * - [OnboardingStep.NOTIFICATIONS] is a runtime permission only on API 33+
 *   (`POST_NOTIFICATIONS`). On API ≤ 32 it is granted at install time and dropped from
 *   the prompt list.
 * - [OnboardingStep.FULL_SCREEN_INTENT] is a user-managed permission only on API 34+
 *   (`Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT`). On API ≤ 33 it is granted at
 *   install time via the manifest declaration and dropped from the prompt list.
 *
 * All other steps apply on every SDK ≥ minSdk 26.
 */
class OnboardingSequencer(private val sdkInt: Int) {
    fun steps(): List<OnboardingStep> =
        buildList {
            if (sdkInt >= API_NOTIFICATIONS_RUNTIME) add(OnboardingStep.NOTIFICATIONS)
            add(OnboardingStep.PHONE)
            add(OnboardingStep.CALL_LOG)
            add(OnboardingStep.SMS)
            if (sdkInt >= API_FULL_SCREEN_INTENT_USER_MANAGED) add(OnboardingStep.FULL_SCREEN_INTENT)
            add(OnboardingStep.OVERLAY_WINDOW)
            add(OnboardingStep.BATTERY_OPTIMIZATION)
        }

    fun nextStep(granted: Set<OnboardingStep>): OnboardingStep? = steps().firstOrNull { it !in granted }

    companion object {
        const val API_NOTIFICATIONS_RUNTIME: Int = 33
        const val API_FULL_SCREEN_INTENT_USER_MANAGED: Int = 34
    }
}
