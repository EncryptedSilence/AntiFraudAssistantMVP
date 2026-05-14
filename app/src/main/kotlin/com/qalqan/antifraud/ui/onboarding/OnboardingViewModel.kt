package com.qalqan.antifraud.ui.onboarding

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.alerts.AlertPermissionResultLogger
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.settings.OnboardingSequencer
import com.qalqan.antifraud.settings.OnboardingStep
import com.qalqan.antifraud.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Spec §22 Stage 8 — Onboarding orchestrator.
 *
 * Stage 9 wiring contract: the FULL_SCREEN_INTENT and OVERLAY_WINDOW step intents are
 * sourced through [com.qalqan.antifraud.alerts.OnboardingPermissionSteps.nextRequiredStep]
 * inside the host activity (Stage 9 owns the ActivityResultLauncher). Grants / denials
 * are funneled back into [grantCurrent] / [skipCurrent] which delegate to
 * [AlertPermissionResultLogger.log] for the alert-managed permissions and to a plain
 * `PERMISSION_GRANTED / PERMISSION_DENIED` entry for the Stage 3 / 4 / 8 permissions.
 */
class OnboardingViewModel(
    application: Application,
    private val repos: Repositories,
    private val userSettings: UserSettings,
    private val sequencer: OnboardingSequencer = OnboardingSequencer(sdkInt = Build.VERSION.SDK_INT),
) : AndroidViewModel(application) {
    private val granted = mutableSetOf<OnboardingStep>()
    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    init {
        advance()
    }

    fun grantCurrent() {
        viewModelScope.launch {
            state.value.currentStep?.let { step ->
                granted += step
                logResult(step, isGrant = true)
            }
            advance()
        }
    }

    fun skipCurrent() {
        viewModelScope.launch {
            state.value.currentStep?.let { step ->
                granted += step
                logResult(step, isGrant = false)
            }
            advance()
        }
    }

    fun finish() {
        viewModelScope.launch {
            userSettings.onboardingCompleted = true
            repos.actionLogger.log(
                AppAction.SETTING_CHANGED,
                mapOf("setting" to "onboarding_completed", "state" to "true"),
            )
        }
    }

    private suspend fun logResult(
        step: OnboardingStep,
        isGrant: Boolean,
    ) {
        when (step) {
            // Stage 9-owned permissions: use AlertPermissionResultLogger so the action-log
            // detail carries the canonical permission name string.
            OnboardingStep.FULL_SCREEN_INTENT ->
                AlertPermissionResultLogger.log(
                    repos.actionLogger,
                    "android.permission.USE_FULL_SCREEN_INTENT",
                    granted = isGrant,
                )
            OnboardingStep.OVERLAY_WINDOW ->
                AlertPermissionResultLogger.log(
                    repos.actionLogger,
                    "android.permission.SYSTEM_ALERT_WINDOW",
                    granted = isGrant,
                )
            // Stage 3 / 4 / 8 permissions: log the step name as a generic permission.
            else ->
                repos.actionLogger.log(
                    if (isGrant) AppAction.PERMISSION_GRANTED else AppAction.PERMISSION_DENIED,
                    mapOf("step" to step.name.lowercase()),
                )
        }
    }

    private fun advance() {
        val steps = sequencer.steps()
        val next = sequencer.nextStep(granted)
        _state.value =
            OnboardingUiState(
                currentStep = next,
                stepIndex = if (next != null) steps.indexOf(next) + 1 else 0,
                totalSteps = steps.size,
            )
    }
}
