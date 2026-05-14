package com.qalqan.antifraud.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverlayBannerScreen(
    content: AlertContent,
    onPause: () -> Unit,
    onDismiss: () -> Unit,
) {
    val twoLines = content.reasons.take(MAX_OVERLAY_LINES)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(BANNER_RED)
                .padding(horizontal = HORIZONTAL_PADDING, vertical = VERTICAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(LINE_SPACING),
    ) {
        Text(text = content.title, color = Color.White, fontSize = TITLE_FONT_SIZE)
        twoLines.forEach { line ->
            Text(text = "• $line", color = Color.White, fontSize = BODY_FONT_SIZE)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BUTTON_SPACING, Alignment.End),
        ) {
            Button(onClick = onPause) { Text(text = content.pauseLabel) }
            OutlinedButton(onClick = onDismiss) { Text(text = content.dismissLabel) }
        }
    }
}

private const val MAX_OVERLAY_LINES = 2

@Suppress("MagicNumber")
private val BANNER_RED = Color(0xCCB71C1C)
private val HORIZONTAL_PADDING = 16.dp
private val VERTICAL_PADDING = 12.dp
private val LINE_SPACING = 8.dp
private val BUTTON_SPACING = 8.dp
private val TITLE_FONT_SIZE = 18.sp
private val BODY_FONT_SIZE = 14.sp
