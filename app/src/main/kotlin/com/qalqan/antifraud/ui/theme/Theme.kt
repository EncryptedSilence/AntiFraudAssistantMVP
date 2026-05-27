package com.qalqan.antifraud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.qalqan.antifraud.domain.RiskBand

private val AntifraudColors =
    darkColorScheme(
        primary = GreenPrimary,
        onPrimary = GreenOnPrimary,
        background = Bg,
        onBackground = TextHigh,
        surface = Surface,
        onSurface = TextHigh,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = TextMuted,
        outline = Outline,
        error = RiskHigh,
    )

@Composable
fun AntifraudTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AntifraudColors,
        typography = AntifraudTypography,
        content = content,
    )
}

fun riskColor(band: RiskBand?): Color =
    when (band) {
        RiskBand.LOW -> RiskLow
        RiskBand.MEDIUM -> RiskMedium
        RiskBand.HIGH -> RiskHigh
        RiskBand.CRITICAL -> RiskCritical
        null -> RiskLow
    }
