package com.qalqan.antifraud.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.settings.OnboardingStep
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §22 Stage 8 — Onboarding screen. Renders one step at a time with title +
 * justification + Grant / Skip. When the sequencer reports no remaining step, the route
 * shows the Finish CTA which writes `UserSettings.onboardingCompleted = true` and routes
 * to Home.
 */
@Composable
fun OnboardingRoute(
    state: OnboardingUiState,
    onGrant: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.titleLarge)
        val step = state.currentStep
        if (step == null) {
            Button(onClick = onFinish, modifier = Modifier.accessibleTouchTarget()) {
                Text(stringResource(R.string.onboarding_action_finish))
            }
            return@Column
        }
        Text(stringResource(R.string.onboarding_step_n_of_m, state.stepIndex, state.totalSteps))
        Text(stringResource(stepTitleResId(step)), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(stepWhyResId(step)))
        Button(onClick = onGrant, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.onboarding_action_grant))
        }
        Button(onClick = onSkip, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.onboarding_action_skip))
        }
    }
}

private fun stepTitleResId(step: OnboardingStep): Int =
    when (step) {
        OnboardingStep.NOTIFICATIONS -> R.string.onboarding_step_notifications_title
        OnboardingStep.PHONE -> R.string.onboarding_step_phone_title
        OnboardingStep.CALL_LOG -> R.string.onboarding_step_call_log_title
        OnboardingStep.SMS -> R.string.onboarding_step_sms_title
        OnboardingStep.FULL_SCREEN_INTENT -> R.string.onboarding_step_full_screen_title
        OnboardingStep.OVERLAY_WINDOW -> R.string.onboarding_step_overlay_title
        OnboardingStep.BATTERY_OPTIMIZATION -> R.string.onboarding_step_battery_title
    }

private fun stepWhyResId(step: OnboardingStep): Int =
    when (step) {
        OnboardingStep.NOTIFICATIONS -> R.string.onboarding_step_notifications_why
        OnboardingStep.PHONE -> R.string.onboarding_step_phone_why
        OnboardingStep.CALL_LOG -> R.string.onboarding_step_call_log_why
        OnboardingStep.SMS -> R.string.onboarding_step_sms_why
        OnboardingStep.FULL_SCREEN_INTENT -> R.string.onboarding_step_full_screen_why
        OnboardingStep.OVERLAY_WINDOW -> R.string.onboarding_step_overlay_why
        OnboardingStep.BATTERY_OPTIMIZATION -> R.string.onboarding_step_battery_why
    }
