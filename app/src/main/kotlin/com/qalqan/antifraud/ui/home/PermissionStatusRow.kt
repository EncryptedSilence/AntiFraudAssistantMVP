package com.qalqan.antifraud.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qalqan.antifraud.R
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.sms.SmsObserverPermissions
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §17.1.1 — top-row permission / battery / sync status indicators. Tappable into
 * the Privacy route.
 */
@Composable
fun PermissionStatusRow(
    callState: CallObserverPermissions.State,
    smsState: SmsObserverPermissions.State,
    batteryExempt: Boolean,
    syncEnabled: Boolean,
    onTap: () -> Unit,
) {
    Column(modifier = Modifier.accessibleTouchTarget().clickable(onClick = onTap)) {
        Text(stringResource(callLabel(callState)))
        Text(stringResource(smsLabel(smsState)))
        if (!batteryExempt) {
            Text(stringResource(R.string.home_battery_optimization_warning))
        }
        val syncState =
            stringResource(
                if (syncEnabled) R.string.home_sync_state_enabled else R.string.home_sync_state_disabled,
            )
        Text(stringResource(R.string.home_sync_label, syncState))
    }
}

private fun callLabel(state: CallObserverPermissions.State): Int =
    when (state) {
        CallObserverPermissions.State.GRANTED -> R.string.home_call_perm_granted
        CallObserverPermissions.State.PARTIAL -> R.string.home_call_perm_partial
        CallObserverPermissions.State.DENIED -> R.string.home_call_perm_denied
    }

private fun smsLabel(state: SmsObserverPermissions.State): Int =
    when (state) {
        SmsObserverPermissions.State.GRANTED -> R.string.home_sms_perm_granted
        SmsObserverPermissions.State.PARTIAL -> R.string.home_sms_perm_partial
        SmsObserverPermissions.State.DENIED -> R.string.home_sms_perm_denied
    }
