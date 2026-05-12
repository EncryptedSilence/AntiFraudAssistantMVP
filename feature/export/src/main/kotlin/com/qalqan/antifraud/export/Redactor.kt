package com.qalqan.antifraud.export

/**
 * Spec §8.4 — one strategy per anonymization option. Each redactor maps a single record
 * to its redacted form for a single option; the [RedactionPipeline] composes the three
 * via `Set<AnonymizationOption>` membership.
 *
 * Redactors are pure: same input → same output, no side effects, no `Context`. This is
 * why they can be tested without Robolectric and why the preview path and writer path
 * are guaranteed to produce identical bytes.
 */
interface Redactor {
    fun apply(record: ExportRecord): ExportRecord
}
