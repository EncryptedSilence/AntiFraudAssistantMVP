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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatusScreen(viewModel: StatusViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }
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
            Text("AntiFraud Assistant — Stage 2 status board", style = MaterialTheme.typography.titleLarge)
            Text("Calls captured: ${state.calls}")
            Text(callPermissionBanner(state.callPermissionsState), style = MaterialTheme.typography.bodyLarge)
            Text(smsPermissionBanner(state.smsPermissionsState), style = MaterialTheme.typography.bodyLarge)
            if (!state.batteryOptimizationExempt) {
                Text(
                    "Battery optimization is on; call observation may be killed in the background.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("SMS captured: ${state.sms}")
            Text("Web visits captured: ${state.web}")
            Text("Patterns enabled: ${state.patternsEnabledCount}")
            state.latestWarningLevel?.let { level ->
                Text("Latest warning: ${level.jsonValue.uppercase()} — ${state.latestWarningReason ?: ""}")
            }
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
            if (sheetOpen) {
                WebEntrySheet(
                    onDismiss = { sheetOpen = false },
                    onSubmit = { input, callback ->
                        viewModel.submitSiteFromSheet(input, callback)
                    },
                )
            }
        }
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
    Button(onClick = onDemo) { Text("Run demo (Fast attack)") }
    Button(onClick = onWipe) { Text("Wipe all data") }
    Button(onClick = onSuspiciousCall) { Text("I had a suspicious call") }
    Button(onClick = onSuspiciousSms) { Text("I had a suspicious SMS") }
    Button(onClick = onSuspiciousSite) { Text("I had a suspicious site") }
}

@Composable
private fun SyncControlsRow(
    syncEnabled: Boolean,
    lastSyncAt: java.time.Instant?,
    onToggle: () -> Unit,
    onSyncNow: () -> Unit,
    onImportLocal: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Sync: ${if (syncEnabled) "enabled" else "disabled"}")
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
        androidx.compose.material3.Switch(
            checked = syncEnabled,
            onCheckedChange = { onToggle() },
        )
    }
    Text("Last synced: ${lastSyncAt ?: "—"}")
    Button(onClick = onSyncNow, enabled = syncEnabled) { Text("Sync now") }
    Button(onClick = onImportLocal) { Text("Import local bundle…") }
}

private fun callPermissionBanner(state: com.qalqan.antifraud.calls.CallObserverPermissions.State): String =
    when (state) {
        com.qalqan.antifraud.calls.CallObserverPermissions.State.GRANTED ->
            "Auto call capture: on"
        com.qalqan.antifraud.calls.CallObserverPermissions.State.PARTIAL ->
            "Auto call capture: partial — some permissions missing"
        com.qalqan.antifraud.calls.CallObserverPermissions.State.DENIED ->
            "Auto call capture: off — manual entry only"
    }

private fun smsPermissionBanner(state: com.qalqan.antifraud.sms.SmsObserverPermissions.State): String =
    when (state) {
        com.qalqan.antifraud.sms.SmsObserverPermissions.State.GRANTED ->
            "Auto SMS capture: on"
        com.qalqan.antifraud.sms.SmsObserverPermissions.State.PARTIAL ->
            "Auto SMS capture: partial — some permissions missing"
        com.qalqan.antifraud.sms.SmsObserverPermissions.State.DENIED ->
            "Auto SMS capture: off — manual paste only"
    }
