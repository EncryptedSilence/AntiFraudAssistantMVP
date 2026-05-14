package com.qalqan.antifraud.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.scoring.Sensitivity
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §18 — Settings screen. Sensitivity radio group + 14 toggle switches.
 */
@Composable
fun SettingsRoute(
    state: SettingsUiState,
    onSensitivityChange: (Sensitivity) -> Unit,
    onToggleChange: (SettingsUiState.SettingKey, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
        SensitivityChooser(state.sensitivity, onSensitivityChange)
        SettingsUiState.SettingKey.entries.forEach { key ->
            ToggleRow(key, state.toggles[key] ?: key.defaultValue, onToggleChange)
        }
    }
}

@Composable
private fun SensitivityChooser(
    selected: Sensitivity,
    onChange: (Sensitivity) -> Unit,
) {
    Text(stringResource(R.string.settings_sensitivity_label), style = MaterialTheme.typography.titleMedium)
    val rows =
        listOf(
            Sensitivity.LOW to R.string.settings_sensitivity_low,
            Sensitivity.STANDARD to R.string.settings_sensitivity_standard,
            Sensitivity.HIGH to R.string.settings_sensitivity_high,
            Sensitivity.MAXIMUM to R.string.settings_sensitivity_maximum,
        )
    rows.forEach { (s, label) ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.accessibleTouchTarget(),
        ) {
            RadioButton(selected = selected == s, onClick = { onChange(s) })
            Text(stringResource(label))
        }
    }
}

@Composable
private fun ToggleRow(
    key: SettingsUiState.SettingKey,
    enabled: Boolean,
    onToggle: (SettingsUiState.SettingKey, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().accessibleTouchTarget(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(labelResId(key)), modifier = Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = { onToggle(key, it) })
    }
}

@Suppress("CyclomaticComplexMethod")
private fun labelResId(key: SettingsUiState.SettingKey): Int =
    when (key) {
        SettingsUiState.SettingKey.CALL_ANALYSIS -> R.string.settings_toggle_call_analysis
        SettingsUiState.SettingKey.SMS_ANALYSIS -> R.string.settings_toggle_sms_analysis
        SettingsUiState.SettingKey.WEB_ANALYSIS -> R.string.settings_toggle_web_analysis
        SettingsUiState.SettingKey.RISK_CAMPAIGNS -> R.string.settings_toggle_risk_campaigns
        SettingsUiState.SettingKey.LOCAL_PATTERNS -> R.string.settings_toggle_local_patterns
        SettingsUiState.SettingKey.REFERENCE_SYNC -> R.string.settings_toggle_reference_sync
        SettingsUiState.SettingKey.PATTERN_SYNC -> R.string.settings_toggle_pattern_sync
        SettingsUiState.SettingKey.NOTIFICATIONS -> R.string.settings_toggle_notifications
        SettingsUiState.SettingKey.POST_CALL_Q -> R.string.settings_toggle_post_call_q
        SettingsUiState.SettingKey.POST_SMS_Q -> R.string.settings_toggle_post_sms_q
        SettingsUiState.SettingKey.POST_SITE_Q -> R.string.settings_toggle_post_site_q
        SettingsUiState.SettingKey.AUTO_ARCHIVING -> R.string.settings_toggle_auto_archiving
        SettingsUiState.SettingKey.ADVANCED_RULES -> R.string.settings_toggle_advanced_rules
        SettingsUiState.SettingKey.EDUCATIONAL_CARDS -> R.string.settings_toggle_educational_cards
    }
