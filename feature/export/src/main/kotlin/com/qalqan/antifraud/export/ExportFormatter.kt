package com.qalqan.antifraud.export

/**
 * Spec §8.5 — one implementation per wire format. The contract: given a list of redacted
 * [ExportRecord]s, produce a deterministic `ByteArray`. Determinism here means:
 *
 *   1. Same input → same byte output (no clock-time, no UUID, no nondeterministic iteration).
 *   2. UTF-8 encoding, no BOM.
 *   3. Stable sort within each category by the variant's stable key.
 *
 * The preview path and the writer path both call [format] with the same `(records,
 * request)` — that is how the §23 #15 invariant is held.
 */
interface ExportFormatter {
    val format: ExportFormat

    fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray
}

/**
 * Dispatch table: maps every [ExportFormat] to its singleton formatter. Adding a format
 * means adding the variant in [ExportFormat] and a new arm here.
 */
object ExportFormatters {
    fun forFormat(format: ExportFormat): ExportFormatter =
        when (format) {
            ExportFormat.TXT -> TxtFormatter
            ExportFormat.MARKDOWN -> MarkdownFormatter
            ExportFormat.JSON -> JsonFormatter
            ExportFormat.CSV -> CsvFormatter
        }
}
