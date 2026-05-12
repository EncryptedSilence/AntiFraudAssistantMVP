package com.qalqan.antifraud.export

import com.qalqan.antifraud.database.log.ApplicationActionLogger
import com.qalqan.antifraud.domain.AppAction

/**
 * Spec §17.5 + §20.1 — writes the three Stage 7 state markers to the application action
 * log without ever including a phone number, domain, URI, path, body, comment, exception,
 * or stack trace. The boundary test in this phase pins the forbidden-key set.
 *
 * Markers:
 *   - `export_preview_shown` — the user reached the redaction-preview screen.
 *   - `export_completed`     — the writer landed bytes on the SAF Uri successfully.
 *   - `export_failed`        — the writer or earlier step failed; `step` carries one of
 *                              `gather` / `format` / `write`. No exception detail.
 */
class ExportActionLog(private val logger: ApplicationActionLogger?) {
    suspend fun previewShown(request: ExportRequest) {
        logger?.log(
            AppAction.SETTING_CHANGED,
            mapOf(
                "setting" to "export_preview_shown",
                "format" to request.format.name,
                "category_count" to request.categories.size.toString(),
                "anonymization_count" to request.anonymization.size.toString(),
            ),
        )
    }

    suspend fun completed(request: ExportRequest) {
        logger?.log(
            AppAction.SETTING_CHANGED,
            mapOf(
                "setting" to "export_completed",
                "format" to request.format.name,
                "category_count" to request.categories.size.toString(),
                "anonymization_count" to request.anonymization.size.toString(),
            ),
        )
    }

    suspend fun failed(step: String) {
        logger?.log(
            AppAction.SETTING_CHANGED,
            mapOf("setting" to "export_failed", "step" to step),
        )
    }
}
