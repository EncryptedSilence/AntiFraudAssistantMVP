package com.qalqan.antifraud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatusScreen(viewModel: StatusViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }
    var exportSheetOpen by remember { mutableStateOf(false) }
    val importLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        ) { uri: android.net.Uri? ->
            if (uri != null) viewModel.importLocalBundle(uri)
        }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            StatusInfoBlock(state)
            ManualEntryButtons(
                onDemo = { viewModel.runDemo() },
                onWipe = { viewModel.wipe() },
                onSuspiciousCall = { viewModel.recordSuspiciousCallStub() },
                onSuspiciousSms = { viewModel.recordSuspiciousSmsStub() },
                onSuspiciousSite = {
                    viewModel.recordSuspiciousSiteStub()
                    sheetOpen = true
                },
            )
            SyncControlsRow(
                syncEnabled = state.syncEnabled,
                lastSyncAt = state.lastSyncAt,
                onToggle = { viewModel.toggleSync() },
                onSyncNow = { viewModel.runSyncNow() },
                onImportLocal = { importLauncher.launch(arrayOf("application/zip", "*/*")) },
            )
            Button(onClick = {
                viewModel.recordExportButtonTap()
                exportSheetOpen = true
            }) {
                Text(stringResource(R.string.home_export_button))
            }
            if (sheetOpen) {
                WebEntrySheet(
                    onDismiss = { sheetOpen = false },
                    onSubmit = { input, callback -> viewModel.submitSiteFromSheet(input, callback) },
                )
            }
            ExportSheetHost(exportSheetOpen, onDismiss = { exportSheetOpen = false }, viewModel)
        }
    }
}

@Composable
private fun StatusInfoBlock(state: StatusViewModel.State) {
    Text(stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge)
    Text(stringResource(R.string.home_calls_count, state.calls))
    Text(callPermissionBanner(state.callPermissionsState), style = MaterialTheme.typography.bodyLarge)
    Text(smsPermissionBanner(state.smsPermissionsState), style = MaterialTheme.typography.bodyLarge)
    if (!state.batteryOptimizationExempt) {
        Text(
            stringResource(R.string.home_battery_optimization_warning),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Text(stringResource(R.string.home_sms_count, state.sms))
    Text(stringResource(R.string.home_web_count, state.web))
    Text(stringResource(R.string.home_patterns_enabled_count, state.patternsEnabledCount))
    state.latestWarningLevel?.let { level ->
        Text(stringResource(R.string.home_latest_warning, level.jsonValue.uppercase(), state.latestWarningReason ?: ""))
    }
}

@Composable
private fun ExportSheetHost(
    open: Boolean,
    onDismiss: () -> Unit,
    viewModel: StatusViewModel,
) {
    if (open) {
        ExportSheet(onDismiss = onDismiss, viewModel = viewModel)
    }
}

@Composable
private fun ManualEntryButtons(
    onDemo: () -> Unit,
    onWipe: () -> Unit,
    onSuspiciousCall: () -> Unit,
    onSuspiciousSms: () -> Unit,
    onSuspiciousSite: () -> Unit,
) {
    Button(onClick = onDemo) { Text(stringResource(R.string.home_run_demo_button)) }
    Button(onClick = onWipe) { Text(stringResource(R.string.home_wipe_button)) }
    Button(onClick = onSuspiciousCall) { Text(stringResource(R.string.home_suspicious_call_button)) }
    Button(onClick = onSuspiciousSms) { Text(stringResource(R.string.home_suspicious_sms_button)) }
    Button(onClick = onSuspiciousSite) { Text(stringResource(R.string.home_suspicious_site_button)) }
}

@Composable
private fun SyncControlsRow(
    syncEnabled: Boolean,
    lastSyncAt: java.time.Instant?,
    onToggle: () -> Unit,
    onSyncNow: () -> Unit,
    onImportLocal: () -> Unit,
) {
    val syncStateLabel =
        if (syncEnabled) {
            stringResource(R.string.home_sync_state_enabled)
        } else {
            stringResource(R.string.home_sync_state_disabled)
        }
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.home_sync_label, syncStateLabel))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
        androidx.compose.material3.Switch(
            checked = syncEnabled,
            onCheckedChange = { onToggle() },
        )
    }
    Text(stringResource(R.string.home_last_synced, lastSyncAt?.toString() ?: "—"))
    Button(onClick = onSyncNow, enabled = syncEnabled) { Text(stringResource(R.string.home_sync_now_button)) }
    Button(onClick = onImportLocal) { Text(stringResource(R.string.home_import_local_button)) }
}

@Composable
private fun callPermissionBanner(state: com.qalqan.antifraud.calls.CallObserverPermissions.State): String =
    when (state) {
        com.qalqan.antifraud.calls.CallObserverPermissions.State.GRANTED ->
            stringResource(R.string.home_call_perm_granted)
        com.qalqan.antifraud.calls.CallObserverPermissions.State.PARTIAL ->
            stringResource(R.string.home_call_perm_partial)
        com.qalqan.antifraud.calls.CallObserverPermissions.State.DENIED ->
            stringResource(R.string.home_call_perm_denied)
    }

@Composable
private fun smsPermissionBanner(state: com.qalqan.antifraud.sms.SmsObserverPermissions.State): String =
    when (state) {
        com.qalqan.antifraud.sms.SmsObserverPermissions.State.GRANTED ->
            stringResource(R.string.home_sms_perm_granted)
        com.qalqan.antifraud.sms.SmsObserverPermissions.State.PARTIAL ->
            stringResource(R.string.home_sms_perm_partial)
        com.qalqan.antifraud.sms.SmsObserverPermissions.State.DENIED ->
            stringResource(R.string.home_sms_perm_denied)
    }
