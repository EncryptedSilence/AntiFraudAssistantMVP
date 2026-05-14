package com.qalqan.antifraud.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.scoring.Sensitivity
import com.qalqan.antifraud.settings.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Spec §18 — ViewModel for the Settings screen. Round-trips sensitivity + the 14 module
 * toggles through [UserSettings] and emits `SETTING_CHANGED` on every change.
 */
class SettingsViewModel(
    application: Application,
    private val repos: Repositories,
    private val userSettings: UserSettings,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value =
                SettingsUiState(
                    sensitivity = userSettings.sensitivity,
                    toggles =
                        mapOf(
                            SettingsUiState.SettingKey.CALL_ANALYSIS to userSettings.callAnalysisEnabled,
                            SettingsUiState.SettingKey.SMS_ANALYSIS to userSettings.smsAnalysisEnabled,
                            SettingsUiState.SettingKey.WEB_ANALYSIS to userSettings.webAnalysisEnabled,
                            SettingsUiState.SettingKey.RISK_CAMPAIGNS to userSettings.riskCampaignsEnabled,
                            SettingsUiState.SettingKey.LOCAL_PATTERNS to userSettings.localPatternsEnabled,
                            SettingsUiState.SettingKey.REFERENCE_SYNC to userSettings.referenceSyncEnabled,
                            SettingsUiState.SettingKey.PATTERN_SYNC to userSettings.patternSyncEnabled,
                            SettingsUiState.SettingKey.NOTIFICATIONS to userSettings.notificationsEnabled,
                            SettingsUiState.SettingKey.POST_CALL_Q to userSettings.postCallQuestionsEnabled,
                            SettingsUiState.SettingKey.POST_SMS_Q to userSettings.postSmsQuestionsEnabled,
                            SettingsUiState.SettingKey.POST_SITE_Q to userSettings.postSiteQuestionsEnabled,
                            SettingsUiState.SettingKey.AUTO_ARCHIVING to userSettings.automaticArchivingEnabled,
                            SettingsUiState.SettingKey.ADVANCED_RULES to userSettings.advancedRulesEnabled,
                            SettingsUiState.SettingKey.EDUCATIONAL_CARDS to userSettings.educationalCardsEnabled,
                        ),
                    isLoading = false,
                )
        }
    }

    fun setSensitivity(s: Sensitivity): Job =
        viewModelScope.launch {
            userSettings.sensitivity = s
            repos.actionLogger.log(
                AppAction.SETTING_CHANGED,
                mapOf("setting" to "sensitivity", "state" to s.name),
            )
            refresh()
        }

    @Suppress("CyclomaticComplexMethod")
    fun setToggle(
        key: SettingsUiState.SettingKey,
        value: Boolean,
    ): Job =
        viewModelScope.launch {
            when (key) {
                SettingsUiState.SettingKey.CALL_ANALYSIS -> userSettings.callAnalysisEnabled = value
                SettingsUiState.SettingKey.SMS_ANALYSIS -> userSettings.smsAnalysisEnabled = value
                SettingsUiState.SettingKey.WEB_ANALYSIS -> userSettings.webAnalysisEnabled = value
                SettingsUiState.SettingKey.RISK_CAMPAIGNS -> userSettings.riskCampaignsEnabled = value
                SettingsUiState.SettingKey.LOCAL_PATTERNS -> userSettings.localPatternsEnabled = value
                SettingsUiState.SettingKey.REFERENCE_SYNC -> userSettings.referenceSyncEnabled = value
                SettingsUiState.SettingKey.PATTERN_SYNC -> userSettings.patternSyncEnabled = value
                SettingsUiState.SettingKey.NOTIFICATIONS -> userSettings.notificationsEnabled = value
                SettingsUiState.SettingKey.POST_CALL_Q -> userSettings.postCallQuestionsEnabled = value
                SettingsUiState.SettingKey.POST_SMS_Q -> userSettings.postSmsQuestionsEnabled = value
                SettingsUiState.SettingKey.POST_SITE_Q -> userSettings.postSiteQuestionsEnabled = value
                SettingsUiState.SettingKey.AUTO_ARCHIVING -> userSettings.automaticArchivingEnabled = value
                SettingsUiState.SettingKey.ADVANCED_RULES -> userSettings.advancedRulesEnabled = value
                SettingsUiState.SettingKey.EDUCATIONAL_CARDS -> userSettings.educationalCardsEnabled = value
            }
            repos.actionLogger.log(
                AppAction.SETTING_CHANGED,
                mapOf("setting" to key.name.lowercase(), "state" to value.toString()),
            )
            refresh()
        }
}
