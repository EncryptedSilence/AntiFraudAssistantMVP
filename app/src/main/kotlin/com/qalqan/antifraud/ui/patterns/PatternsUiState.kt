package com.qalqan.antifraud.ui.patterns

import java.time.Instant

/**
 * Spec §17.3 — Patterns screen state. `Source` distinguishes in-APK seed patterns from
 * bundled patterns delivered via `:core:sync` (Stage 6).
 */
data class PatternsUiState(
    val rows: List<PatternRow> = emptyList(),
    val isLoading: Boolean = false,
) {
    data class PatternRow(
        val patternId: String,
        val name: String,
        val category: String,
        val version: String,
        val source: Source,
        val enabled: Boolean,
        val triggerCount: Int,
        val lastTriggeredAt: Instant?,
    )

    enum class Source { SEED, BUNDLE }
}
