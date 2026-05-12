@file:Suppress("ReturnCount")

package com.qalqan.antifraud.export

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException

/**
 * Spec §8 + CLAUDE.md hard rule — writes a buffered `ByteArray` to a SAF-provided
 * [Uri] via [ContentResolver.openOutputStream]. The Stage 7 contract: the input bytes
 * are EXACTLY the bytes the user saw in the preview path. The §23 #15 acceptance test
 * (T35) confirms this by sha256-equality.
 *
 * `ContentResolver.openOutputStream` returning `null` is treated as an I/O error
 * (the typical cause is a stale Uri whose grant was revoked). Every failure path
 * surfaces as a typed [ExportWriteError]; the writer never throws.
 */
object ExportWriter {
    fun writeTo(
        uri: Uri,
        bytes: ByteArray,
        contentResolver: ContentResolver,
    ): Result<Unit> {
        val output =
            try {
                contentResolver.openOutputStream(uri)
            } catch (_: Exception) {
                return Result.failure(ExportWriteError.IoError)
            }
        if (output == null) return Result.failure(ExportWriteError.IoError)
        return try {
            output.use { it.write(bytes) }
            Result.success(Unit)
        } catch (_: IOException) {
            Result.failure(ExportWriteError.IoError)
        } catch (_: Exception) {
            Result.failure(ExportWriteError.IoError)
        }
    }
}

/** Spec §8 — typed failure modes for [ExportWriter.writeTo]. */
sealed class ExportWriteError : Throwable() {
    data object IoError : ExportWriteError()

    data object PreviewNotShown : ExportWriteError()
}
