package com.qalqan.antifraud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
            Button(onClick = { viewModel.runDemo() }) { Text("Run demo (Fast attack)") }
            Button(onClick = { viewModel.wipe() }) { Text("Wipe all data") }
            Button(onClick = { viewModel.recordSuspiciousCallStub() }) {
                Text("I had a suspicious call")
            }
            Button(onClick = { viewModel.recordSuspiciousSmsStub() }) {
                Text("I had a suspicious SMS")
            }
            Button(onClick = {
                viewModel.recordSuspiciousSiteStub()
                sheetOpen = true
            }) {
                Text("I had a suspicious site")
            }
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
