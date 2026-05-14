package com.qalqan.antifraud.ui.privacy

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.state.LoadingState
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §17.6 — Privacy screen.
 */
@Composable
@Suppress("LongParameterList")
fun PrivacyRoute(
    state: PrivacyUiState,
    onDeleteAll: () -> Unit,
    onDisableSync: () -> Unit,
    onResetPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (state.isLoading) {
        LoadingState()
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleLarge)
        SectionHeader(R.string.privacy_what_is_stored, R.string.privacy_what_is_stored_body)
        SectionHeader(R.string.privacy_where_is_stored, R.string.privacy_where_is_stored_body)
        Text(stringResource(R.string.privacy_modules_enabled), style = MaterialTheme.typography.titleMedium)
        state.modulesEnabled.forEach { Text("• $it") }
        Text(stringResource(R.string.privacy_permissions_granted), style = MaterialTheme.typography.titleMedium)
        state.permissionsGranted.forEach { Text("• $it") }
        Text(stringResource(R.string.privacy_retention_label), style = MaterialTheme.typography.titleMedium)
        state.retentionRows.forEach {
            Text(stringResource(R.string.privacy_retention_row, it.key, it.days))
        }
        Text(
            stringResource(R.string.privacy_sync_status_label) + ": " + state.syncStatus.name.lowercase(),
        )
        Button(onClick = onDeleteAll, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.privacy_delete_all_button))
        }
        Button(onClick = onDisableSync, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.privacy_disable_sync_button))
        }
        Button(onClick = onResetPermissions, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.privacy_reset_permissions_button))
        }
        Button(onClick = onOpenSettings, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.privacy_open_settings_button))
        }
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleResId: Int,
    @StringRes bodyResId: Int,
) {
    Text(stringResource(titleResId), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(bodyResId), style = MaterialTheme.typography.bodyMedium)
}
