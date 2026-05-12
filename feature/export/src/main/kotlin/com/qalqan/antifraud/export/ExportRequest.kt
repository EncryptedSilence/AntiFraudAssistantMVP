package com.qalqan.antifraud.export

/**
 * Spec §8 — the user's export intent, normalized into a typed value the orchestrator,
 * preview path, and writer path all consume. Construction-time invariants:
 *
 *   1. `categories` is non-empty (the UI cannot submit an empty selection).
 *   2. `format` is exactly one of the four §8.5 wire formats (enforced by the type).
 *   3. `anonymization` is a `Set` so order does not affect equality and the JSON
 *      formatter writes a deterministic shape.
 *
 * `summary` / `full` / `custom` detail levels (§8.3) are NOT yet a field on this type;
 * Stage 7 ships `standard` only, and later stages add a `detail: ExportDetail = STANDARD`
 * parameter without breaking existing callers.
 */
data class ExportRequest(
    val categories: Set<ExportCategory>,
    val format: ExportFormat,
    val anonymization: Set<AnonymizationOption> = emptySet(),
) {
    init {
        require(categories.isNotEmpty()) { "ExportRequest.categories must not be empty (§8.2)" }
    }
}
