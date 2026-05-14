package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.settings.OnboardingSequencer
import com.qalqan.antifraud.settings.OnboardingStep
import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Spec §22 Stage 8 — onboarding step order:
 * notifications → phone → call log → SMS → full-screen intent (Android 14+ only) →
 * overlay window → battery-optimization exemption.
 *
 * This acceptance pins the spec ordering across two SDK targets.
 */
class OnboardingSdkAwareSequenceTest {
    @Test
    fun `§22 Stage 8 — Android 14 includes the full sequence in spec order`() {
        OnboardingSequencer(sdkInt = 34).steps() shouldBe
            listOf(
                OnboardingStep.NOTIFICATIONS,
                OnboardingStep.PHONE,
                OnboardingStep.CALL_LOG,
                OnboardingStep.SMS,
                OnboardingStep.FULL_SCREEN_INTENT,
                OnboardingStep.OVERLAY_WINDOW,
                OnboardingStep.BATTERY_OPTIMIZATION,
            )
    }

    @Test
    fun `§22 Stage 8 — Android 11 drops the FULL_SCREEN_INTENT and NOTIFICATIONS steps`() {
        // API 30 < 33 (POST_NOTIFICATIONS) and < 34 (USE_FULL_SCREEN_INTENT user-managed).
        OnboardingSequencer(sdkInt = 30).steps() shouldBe
            listOf(
                OnboardingStep.PHONE,
                OnboardingStep.CALL_LOG,
                OnboardingStep.SMS,
                OnboardingStep.OVERLAY_WINDOW,
                OnboardingStep.BATTERY_OPTIMIZATION,
            )
    }
}
