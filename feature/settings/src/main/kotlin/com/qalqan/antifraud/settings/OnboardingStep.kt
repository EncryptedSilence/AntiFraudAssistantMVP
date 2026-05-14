package com.qalqan.antifraud.settings

/**
 * Spec §22 Stage 8 — ordered onboarding permission prompts. The ordering here is the
 * exact spec ordering; [OnboardingSequencer] drops steps that don't apply on a given
 * SDK level.
 */
enum class OnboardingStep {
    NOTIFICATIONS,
    PHONE,
    CALL_LOG,
    SMS,
    FULL_SCREEN_INTENT,
    OVERLAY_WINDOW,
    BATTERY_OPTIMIZATION,
}
