package com.qalqan.antifraud.ui.state

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Spec §17.7 — minimum touch target 48 dp.
 *
 * defaultMinSize floors the measured width and height; callers can still grow the target
 * with a .size(...) or .fillMaxWidth(...) later in the chain. Apply this modifier to
 * every clickable element on every screen.
 */
fun Modifier.accessibleTouchTarget(): Modifier = this.then(Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp))
