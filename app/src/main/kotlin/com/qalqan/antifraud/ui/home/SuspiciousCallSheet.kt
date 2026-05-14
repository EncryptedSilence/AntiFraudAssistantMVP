package com.qalqan.antifraud.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R

/**
 * Spec §17.1 / §23 #45 — manual-entry fallback sheet for "I had a suspicious call".
 * The form is intentionally minimal; richer fields land post-MVP. Submission goes
 * through `ManualEntry.calls.submit(...)` hosted by the caller.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuspiciousCallSheet(
    onDismiss: () -> Unit,
    onSubmit: (rawPhone: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var raw by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.home_suspicious_call_button))
            OutlinedTextField(
                value = raw,
                onValueChange = { raw = it },
                label = { Text(stringResource(R.string.suspicious_call_sheet_phone_label)) },
            )
            Button(onClick = {
                onSubmit(raw)
                onDismiss()
            }) {
                Text(stringResource(R.string.suspicious_sheet_save))
            }
        }
    }
}
