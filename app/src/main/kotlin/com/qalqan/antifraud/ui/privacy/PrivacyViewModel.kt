package com.qalqan.antifraud.ui.privacy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.calls.BatteryOptimizationPrompt
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.settings.RetentionDisplay
import com.qalqan.antifraud.settings.UserSettings
import com.qalqan.antifraud.sms.SmsObserverPermissions
import com.qalqan.antifraud.sync.BundleStore
import com.qalqan.antifraud.sync.SyncSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Spec §17.6 — ViewModel for the Privacy screen. Drives Delete-all (via
 * [Repositories.wipeAll]), Disable-sync (via [SyncSettings]), Reset-permissions (via
 * [UserSettings.onboardingCompleted]); every change logs `SETTING_CHANGED` /
 * `DATA_DELETED` through [Repositories.actionLogger].
 */
class PrivacyViewModel(
    application: Application,
    private val repos: Repositories,
    private val userSettings: UserSettings,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PrivacyUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            _state.value =
                PrivacyUiState(
                    modulesEnabled =
                        listOfNotNull(
                            "Calls".takeIf { userSettings.callAnalysisEnabled },
                            "SMS".takeIf { userSettings.smsAnalysisEnabled },
                            "Web".takeIf { userSettings.webAnalysisEnabled },
                            "Patterns".takeIf { userSettings.localPatternsEnabled },
                            "Sync".takeIf {
                                userSettings.referenceSyncEnabled || userSettings.patternSyncEnabled
                            },
                        ),
                    permissionsGranted = collectPermissions(),
                    retentionRows =
                        RetentionDisplay.rows().map { PrivacyUiState.RetentionRow(it.key, it.days) },
                    syncStatus =
                        if (SyncSettings(getApplication()).enabled) {
                            PrivacyUiState.SyncStatus.IDLE
                        } else {
                            PrivacyUiState.SyncStatus.PAUSED
                        },
                    isLoading = false,
                )
        }
    }

    fun deleteAll(): Job =
        viewModelScope.launch {
            repos.wipeAll(bundleStore = BundleStore(getApplication()))
            repos.actionLogger.log(AppAction.DATA_DELETED, mapOf("source" to "privacy_screen"))
            refresh()
        }

    fun disableSync(): Job =
        viewModelScope.launch {
            val s = SyncSettings(getApplication())
            s.enabled = false
            repos.actionLogger.log(
                AppAction.SETTING_CHANGED,
                mapOf("setting" to "sync_enabled", "state" to "off"),
            )
            refresh()
        }

    fun resetPermissions(): Job =
        viewModelScope.launch {
            userSettings.onboardingCompleted = false
            repos.actionLogger.log(
                AppAction.SETTING_CHANGED,
                mapOf("setting" to "onboarding_completed", "state" to "false"),
            )
        }

    private fun collectPermissions(): List<String> {
        val app = getApplication<Application>()
        val perms = mutableListOf<String>()
        if (CallObserverPermissions(app).state() == CallObserverPermissions.State.GRANTED) {
            perms += "Phone + Call log"
        }
        if (SmsObserverPermissions(app).state() == SmsObserverPermissions.State.GRANTED) {
            perms += "SMS"
        }
        if (BatteryOptimizationPrompt.isExempt(app)) {
            perms += "Battery exemption"
        }
        return perms
    }
}
