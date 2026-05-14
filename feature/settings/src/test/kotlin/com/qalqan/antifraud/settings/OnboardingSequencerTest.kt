package com.qalqan.antifraud.settings

import io.kotest.matchers.shouldBe
import org.junit.Test

class OnboardingSequencerTest {
    @Test
    fun `default sequence on API 34 includes full-screen-intent and overlay and battery`() {
        val seq = OnboardingSequencer(sdkInt = 34)
        seq.steps() shouldBe
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
    fun `on API 30 the NOTIFICATIONS and FULL_SCREEN_INTENT steps are dropped`() {
        // API 30 < 33 (POST_NOTIFICATIONS runtime gate) and < 34 (USE_FULL_SCREEN_INTENT
        // user-managed gate), so both runtime-gated steps fall off.
        val seq = OnboardingSequencer(sdkInt = 30)
        seq.steps() shouldBe
            listOf(
                OnboardingStep.PHONE,
                OnboardingStep.CALL_LOG,
                OnboardingStep.SMS,
                OnboardingStep.OVERLAY_WINDOW,
                OnboardingStep.BATTERY_OPTIMIZATION,
            )
    }

    @Test
    fun `on API 33 only the FULL_SCREEN_INTENT step is dropped`() {
        val seq = OnboardingSequencer(sdkInt = 33)
        seq.steps() shouldBe
            listOf(
                OnboardingStep.NOTIFICATIONS,
                OnboardingStep.PHONE,
                OnboardingStep.CALL_LOG,
                OnboardingStep.SMS,
                OnboardingStep.OVERLAY_WINDOW,
                OnboardingStep.BATTERY_OPTIMIZATION,
            )
    }

    @Test
    fun `on API 26 the NOTIFICATIONS step is dropped`() {
        val seq = OnboardingSequencer(sdkInt = 26)
        seq.steps() shouldBe
            listOf(
                OnboardingStep.PHONE,
                OnboardingStep.CALL_LOG,
                OnboardingStep.SMS,
                OnboardingStep.OVERLAY_WINDOW,
                OnboardingStep.BATTERY_OPTIMIZATION,
            )
    }

    @Test
    fun `nextStep skips entries already in granted set`() {
        val seq = OnboardingSequencer(sdkInt = 34)
        seq.nextStep(granted = setOf(OnboardingStep.NOTIFICATIONS, OnboardingStep.PHONE)) shouldBe
            OnboardingStep.CALL_LOG
    }

    @Test
    fun `nextStep returns null when all steps are granted (onboarding complete)`() {
        val seq = OnboardingSequencer(sdkInt = 34)
        seq.nextStep(granted = OnboardingStep.entries.toSet()) shouldBe null
    }
}
