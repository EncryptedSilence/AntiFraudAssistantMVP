package com.qalqan.antifraud.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CriticalAlertScreen(
    content: AlertContent,
    onPause: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BACKGROUND_RED)
                .padding(OUTER_PADDING),
        verticalArrangement = Arrangement.spacedBy(VERTICAL_SPACING),
    ) {
        Text(
            text = content.title,
            fontSize = TITLE_FONT_SIZE,
            color = TITLE_RED,
        )
        content.reasons.forEach { line ->
            Text(text = "• $line", fontSize = REASON_FONT_SIZE)
        }
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BUTTON_SPACING, Alignment.CenterHorizontally),
        ) {
            Button(
                onClick = onPause,
                contentPadding = PaddingValues(horizontal = BUTTON_H_PADDING, vertical = BUTTON_V_PADDING),
            ) {
                Text(text = content.pauseLabel, fontSize = BUTTON_FONT_SIZE)
            }
            OutlinedButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = BUTTON_H_PADDING, vertical = BUTTON_V_PADDING),
            ) {
                Text(text = content.dismissLabel, fontSize = BUTTON_FONT_SIZE)
            }
        }
        Text(
            text = content.whyLinkLabel,
            fontSize = WHY_LINK_FONT_SIZE,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = SPACER_HEIGHT),
        )
    }
}

private val BACKGROUND_RED = Color(0xFFFFEBEE)
private val TITLE_RED = Color(0xFFB71C1C)
private val OUTER_PADDING = 24.dp
private val VERTICAL_SPACING = 16.dp
private val SPACER_HEIGHT = 8.dp
private val BUTTON_SPACING = 12.dp
private val BUTTON_H_PADDING = 24.dp
private val BUTTON_V_PADDING = 16.dp
private val TITLE_FONT_SIZE = 28.sp
private val REASON_FONT_SIZE = 18.sp
private val BUTTON_FONT_SIZE = 18.sp
private val WHY_LINK_FONT_SIZE = 14.sp
