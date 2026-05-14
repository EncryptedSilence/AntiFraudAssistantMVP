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
 * Spec §17.1 / §23 #45 / §2.1 — manual-entry fallback sheet for "I had a suspicious SMS".
 * Body is truncated to `SmsEvent.MAX_BODY_EXCERPT_CHARS` (≤200 chars) inside
 * `ManualEntry.sms.submit`. The sheet does no truncation itself so the user sees
 * exactly what they typed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuspiciousSmsSheet(
    onDismiss: () -> Unit,
    onSubmit: (sender: String, body: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var sender by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.home_suspicious_sms_button))
            OutlinedTextField(
                value = sender,
                onValueChange = { sender = it },
                label = { Text(stringResource(R.string.suspicious_sms_sheet_sender_label)) },
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.suspicious_sms_sheet_body_label)) },
            )
            Button(onClick = {
                onSubmit(sender, body)
                onDismiss()
            }) {
                Text(stringResource(R.string.suspicious_sheet_save))
            }
        }
    }
}
