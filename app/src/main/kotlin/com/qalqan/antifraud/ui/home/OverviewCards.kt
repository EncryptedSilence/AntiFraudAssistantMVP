package com.qalqan.antifraud.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.ui.theme.riskColor

@Composable
fun bandTitle(band: RiskBand?): String =
    stringResource(
        when (band) {
            RiskBand.LOW -> R.string.risk_band_low
            RiskBand.MEDIUM -> R.string.risk_band_medium
            RiskBand.HIGH -> R.string.risk_band_high
            RiskBand.CRITICAL -> R.string.risk_band_critical
            null -> R.string.risk_band_clear
        },
    )

@Composable
fun RiskLevelCard(
    band: RiskBand?,
    label: String,
    hint: String,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                bandTitle(band),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = riskColor(band),
            )
            RiskGauge(band)
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RiskGauge(band: RiskBand?) {
    val active =
        when (band) {
            RiskBand.LOW -> 1
            RiskBand.MEDIUM -> 2
            RiskBand.HIGH -> 3
            RiskBand.CRITICAL -> 4
            null -> 0
        }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..GAUGE_SEGMENTS) {
            val color =
                if (i <= active) riskColor(band) else MaterialTheme.colorScheme.surfaceVariant
            Box(
                Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }
    }
}

@Composable
@Suppress("LongParameterList")
fun ActivityCard(
    title: String,
    calls: Int,
    sms: Int,
    sites: Int,
    callsLabel: String,
    smsLabel: String,
    sitesLabel: String,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Counter(calls, callsLabel)
                Counter(sms, smsLabel)
                Counter(sites, sitesLabel)
            }
        }
    }
}

@Composable
private fun Counter(
    value: Int,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$value",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val GAUGE_SEGMENTS = 4
