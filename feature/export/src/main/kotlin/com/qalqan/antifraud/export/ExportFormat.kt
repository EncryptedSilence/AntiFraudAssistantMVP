package com.qalqan.antifraud.export

/**
 * Spec §8.5 — the four MVP wire formats. PDF is post-MVP per v2 §24 #7 and is not
 * declared here. Adding a format means adding a variant here plus a new [ExportFormatter]
 * implementation in Phase 4.
 *
 * `mimeType` is passed to `ActivityResultContracts.OpenDocument` via `setType` so the
 * Android SAF picker filters by file type. `fileExtension` drives the suggested
 * `Intent.EXTRA_TITLE` for the picker.
 */
enum class ExportFormat(val mimeType: String, val fileExtension: String) {
    TXT("text/plain", "txt"),
    MARKDOWN("text/markdown", "md"),
    JSON("application/json", "json"),
    CSV("text/csv", "csv"),
}
