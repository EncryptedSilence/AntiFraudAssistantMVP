package com.qalqan.antifraud.ui.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.state.EmptyState
import com.qalqan.antifraud.ui.state.LoadingState
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §17.3 — Patterns screen. Shows every system pattern with name, category, version,
 * source (seed / bundle), trigger info, and an enable/disable toggle. "Reset to defaults"
 * clears [com.qalqan.antifraud.database.patterns.PatternStateRepository] overrides.
 */
@Composable
fun PatternsRoute(
    state: PatternsUiState,
    onToggle: (patternId: String, enabled: Boolean) -> Unit,
    onResetDefaults: () -> Unit,
) {
    if (state.isLoading) {
        LoadingState()
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.patterns_title), style = MaterialTheme.typography.titleLarge)
        Button(onClick = onResetDefaults, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.patterns_reset_button))
        }
        if (state.rows.isEmpty()) {
            EmptyState(messageResId = R.string.patterns_empty)
        } else {
            LazyColumn { items(state.rows) { row -> PatternRowCard(row, onToggle) } }
        }
    }
}

@Composable
private fun PatternRowCard(
    row: PatternsUiState.PatternRow,
    onToggle: (String, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).accessibleTouchTarget(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(row.name, style = MaterialTheme.typography.titleMedium)
            Text(row.category, style = MaterialTheme.typography.bodySmall)
            Text(row.version, style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(
                    when (row.source) {
                        PatternsUiState.Source.SEED -> R.string.patterns_source_seed
                        PatternsUiState.Source.BUNDLE -> R.string.patterns_source_bundle
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(stringResource(R.string.patterns_trigger_count, row.triggerCount))
            val last = row.lastTriggeredAt
            Text(
                if (last == null) {
                    stringResource(R.string.patterns_last_trigger_never)
                } else {
                    stringResource(R.string.patterns_last_trigger_at, last.toString())
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(checked = row.enabled, onCheckedChange = { onToggle(row.patternId, it) })
    }
}
