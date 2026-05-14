package com.qalqan.antifraud.ui.state

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R

@Composable
fun OfflineState(
    @StringRes messageResId: Int = R.string.state_offline_default,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .semantics { contentDescription = "Offline state" },
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(messageResId), style = MaterialTheme.typography.bodyLarge)
    }
}
