package com.qalqan.antifraud.ui.settings

import com.qalqan.antifraud.scoring.Sensitivity

/**
 * Spec §18 — Settings screen state. The 14 module toggles match the §18 list. The
 * `defaultValue` field on [SettingKey] mirrors the defaults in
 * [com.qalqan.antifraud.settings.UserSettings].
 */
data class SettingsUiState(
    val sensitivity: Sensitivity = Sensitivity.STANDARD,
    val toggles: Map<SettingKey, Boolean> = SettingKey.entries.associateWith { it.defaultValue },
    val isLoading: Boolean = false,
) {
    enum class SettingKey(val defaultValue: Boolean) {
        CALL_ANALYSIS(true),
        SMS_ANALYSIS(true),
        WEB_ANALYSIS(true),
        RISK_CAMPAIGNS(true),
        LOCAL_PATTERNS(true),
        REFERENCE_SYNC(true),
        PATTERN_SYNC(true),
        NOTIFICATIONS(true),
        POST_CALL_Q(true),
        POST_SMS_Q(true),
        POST_SITE_Q(true),
        AUTO_ARCHIVING(true),
        ADVANCED_RULES(false),
        EDUCATIONAL_CARDS(true),
    }
}
