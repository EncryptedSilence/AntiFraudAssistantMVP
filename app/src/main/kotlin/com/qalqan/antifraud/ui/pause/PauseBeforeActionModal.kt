package com.qalqan.antifraud.ui.pause

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.qalqan.antifraud.R

/**
 * Spec §11.5 — non-cancelable pause-before-action modal. Surfaced when the active
 * campaign reaches [com.qalqan.antifraud.domain.RiskBand.CRITICAL]. The user must
 * actively choose one of the two buttons; tap-outside and back-press are disabled.
 */
@Composable
fun PauseBeforeActionModal(
    visible: Boolean,
    onPause: () -> Unit,
    onShowDetails: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = { /* §11.5 — non-cancelable; the user must pick a button. */ },
        title = { Text(stringResource(R.string.pause_title)) },
        text = { Text(stringResource(R.string.pause_body)) },
        confirmButton = {
            TextButton(onClick = onPause) { Text(stringResource(R.string.pause_action_pause)) }
        },
        dismissButton = {
            TextButton(onClick = onShowDetails) {
                Text(stringResource(R.string.pause_action_show_details))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
    )
}
