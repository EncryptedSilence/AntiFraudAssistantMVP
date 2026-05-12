@file:Suppress("ReturnCount")

package com.qalqan.antifraud.export

import android.content.ContentResolver
import android.net.Uri
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.export.ExportProfileEntity
import java.time.Instant
import java.util.UUID

/**
 * Spec §8 + §16.10 + §17.5 — orchestrates the export pipeline. Two phases:
 *
 *   1. [preview] — calls the gatherer + redaction + formatter, returns the bytes the user
 *      should see. Records a transient `previewToken` so a later [write] call can prove
 *      that the same `(request, records)` pair was previewed.
 *   2. [write] — verifies the token, writes the bytes to the SAF [Uri], and persists
 *      an [ExportProfileEntity] row with `redactionPreviewShown = true` and
 *      `userConfirmed = true` (the user clicked Save in the UI).
 *
 * The two phases are decoupled by the token so the orchestrator can be driven from
 * Compose state (the preview is generated first, then the user taps Save after reviewing).
 */
class ExportOrchestrator(
    private val gatherer: ExportGatherer,
    private val pipeline: RedactionPipeline,
    private val contentResolver: ContentResolver,
    private val clock: () -> Instant,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    private val pendingPreviews = mutableMapOf<String, Preview>()

    suspend fun preview(
        request: ExportRequest,
        repositories: Repositories,
    ): Result<Preview> {
        val raw = gatherer.gather(request, repositories)
        val redacted = pipeline.apply(raw, request.anonymization)
        val bytes = ExportFormatters.forFormat(request.format).format(redacted, request)
        val token = idGenerator()
        val preview = Preview(token = token, request = request, bytes = bytes)
        pendingPreviews[token] = preview
        return Result.success(preview)
    }

    suspend fun write(
        request: ExportRequest,
        repositories: Repositories,
        uri: Uri,
        previewToken: String,
    ): Result<Unit> {
        val preview =
            pendingPreviews[previewToken]
                ?: return Result.failure(ExportWriteError.PreviewNotShown)
        if (preview.request != request) return Result.failure(ExportWriteError.PreviewNotShown)

        val writeResult = ExportWriter.writeTo(uri, preview.bytes, contentResolver)
        if (writeResult.isFailure) return writeResult

        repositories.exportProfiles.insert(
            ExportProfileEntity(
                exportId = idGenerator(),
                createdAt = clock().toEpochMilli(),
                exportType = request.categories.joinToString(",") { it.jsonValue },
                includedCategories = request.categories.joinToString(",") { it.jsonValue },
                anonymizationLevel = request.anonymization.joinToString(",") { it.jsonValue },
                format = request.format.name,
                userConfirmed = true,
                redactionPreviewShown = true,
            ),
        )
        pendingPreviews.remove(previewToken)
        return Result.success(Unit)
    }

    /** Spec §17.5 — the in-memory pairing of preview bytes with the token that gates the write. */
    data class Preview(
        val token: String,
        val request: ExportRequest,
        val bytes: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Preview) return false
            return token == other.token && request == other.request && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            var r = token.hashCode()
            r = 31 * r + request.hashCode()
            r = 31 * r + bytes.contentHashCode()
            return r
        }
    }
}
