package com.qalqan.antifraud.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R

/**
 * Профиль tab — entry point to the screens that are not top-level tabs in the v2 shell:
 * Settings, detection Patterns (§17.3), References (§17.4), risk-chain Campaigns (§17.2),
 * and Privacy / delete-all (§17.6). Keeping these reachable preserves §23 #2/#14/#20/#44.
 */
@Composable
@Suppress("LongParameterList")
fun ProfileRoute(
    onOpenSettings: () -> Unit,
    onOpenPatterns: () -> Unit,
    onOpenReferences: () -> Unit,
    onOpenCampaigns: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            stringResource(R.string.nav_profile),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        ProfileRow(stringResource(R.string.profile_settings), onOpenSettings)
        HorizontalDivider()
        ProfileRow(stringResource(R.string.profile_patterns), onOpenPatterns)
        HorizontalDivider()
        ProfileRow(stringResource(R.string.profile_references), onOpenReferences)
        HorizontalDivider()
        ProfileRow(stringResource(R.string.profile_campaigns), onOpenCampaigns)
        HorizontalDivider()
        ProfileRow(stringResource(R.string.profile_privacy), onOpenPrivacy)
    }
}

@Composable
private fun ProfileRow(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        label,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 18.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
}
