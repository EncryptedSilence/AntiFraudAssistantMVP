package com.qalqan.antifraud.ui.onboarding

import com.qalqan.antifraud.settings.OnboardingStep

/**
 * Spec §22 Stage 8 — Onboarding-screen state. `currentStep == null` means the sequencer
 * has nothing left to ask; the route renders the "Finish" CTA.
 */
data class OnboardingUiState(
    val currentStep: OnboardingStep? = null,
    val stepIndex: Int = 0,
    val totalSteps: Int = 0,
)
