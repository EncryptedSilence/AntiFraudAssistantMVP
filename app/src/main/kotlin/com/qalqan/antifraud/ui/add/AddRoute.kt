package com.qalqan.antifraud.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * Добавить tab. For Milestone 1 this reuses the existing manual-entry sheets via open-callbacks
 * (the richer category / impersonated-as / threat-level form lands with the Lists milestone).
 */
@Composable
fun AddRoute(
    onAddCall: () -> Unit,
    onAddSms: () -> Unit,
    onAddSite: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.add_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.add_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onAddCall, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_suspicious_call_button))
        }
        Button(onClick = onAddSms, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_suspicious_sms_button))
        }
        Button(onClick = onAddSite, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_suspicious_site_button))
        }
    }
}
