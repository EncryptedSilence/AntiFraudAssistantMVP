package com.qalqan.antifraud.ui.references

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.state.EmptyState
import com.qalqan.antifraud.ui.state.LoadingState

/**
 * Spec §17.4 — References screen with four read-only tabs.
 */
@Composable
fun ReferencesRoute(state: ReferencesUiState) {
    if (state.isLoading) {
        LoadingState()
        return
    }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        val tabs =
            listOf(
                R.string.references_tab_numbers,
                R.string.references_tab_domains,
                R.string.references_tab_sms_categories,
                R.string.references_tab_official,
            )
        var selectedIndex by remember { mutableIntStateOf(0) }
        TabRow(selectedTabIndex = selectedIndex) {
            tabs.forEachIndexed { i, label ->
                Tab(selected = i == selectedIndex, onClick = { selectedIndex = i }) {
                    Text(stringResource(label), modifier = Modifier.padding(16.dp))
                }
            }
        }
        BundleLine(state)
        when (selectedIndex) {
            0 -> NumbersPane(state)
            1 -> DomainsPane(state)
            2 -> SmsCategoriesPane(state)
            else -> OfficialContactsPane(state)
        }
    }
}

@Composable
private fun BundleLine(state: ReferencesUiState) {
    val at = state.lastBundleAt
    val src = state.lastBundleSource
    if (at == null || src == null) {
        Text(stringResource(R.string.references_no_bundle), style = MaterialTheme.typography.bodySmall)
    } else {
        Text(
            stringResource(R.string.references_last_update_label, at.toString(), src),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun NumbersPane(state: ReferencesUiState) {
    if (state.suspiciousNumbers.isEmpty() && state.trustedNumbers.isEmpty()) {
        EmptyState(messageResId = R.string.references_empty_list)
        return
    }
    Text(stringResource(R.string.references_section_suspicious), style = MaterialTheme.typography.titleMedium)
    LazyColumn { items(state.suspiciousNumbers) { Text(it) } }
    Text(stringResource(R.string.references_section_trusted), style = MaterialTheme.typography.titleMedium)
    LazyColumn { items(state.trustedNumbers) { Text(it) } }
}

@Composable
private fun DomainsPane(state: ReferencesUiState) {
    if (state.suspiciousDomains.isEmpty() && state.trustedDomains.isEmpty()) {
        EmptyState(messageResId = R.string.references_empty_list)
        return
    }
    Text(stringResource(R.string.references_section_suspicious), style = MaterialTheme.typography.titleMedium)
    LazyColumn { items(state.suspiciousDomains) { Text(it) } }
    Text(stringResource(R.string.references_section_trusted), style = MaterialTheme.typography.titleMedium)
    LazyColumn { items(state.trustedDomains) { Text(it) } }
}

@Composable
private fun SmsCategoriesPane(state: ReferencesUiState) {
    if (state.smsCategories.isEmpty()) {
        EmptyState(messageResId = R.string.references_empty_list)
        return
    }
    LazyColumn { items(state.smsCategories) { Text(it) } }
}

@Composable
private fun OfficialContactsPane(state: ReferencesUiState) {
    if (state.officialContacts.isEmpty()) {
        EmptyState(messageResId = R.string.references_empty_list)
        return
    }
    LazyColumn { items(state.officialContacts) { Text(it) } }
}
