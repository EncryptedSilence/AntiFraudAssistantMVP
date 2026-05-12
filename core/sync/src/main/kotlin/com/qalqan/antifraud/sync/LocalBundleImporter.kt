@file:Suppress("ReturnCount")

package com.qalqan.antifraud.sync

import com.qalqan.antifraud.crypto.BundleArchiveReader
import com.qalqan.antifraud.crypto.BundleVerifier
import com.qalqan.antifraud.database.log.ApplicationActionLogger
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Spec §7.5 — the `local` channel. A user-imported `.afpkg` runs through the same
 * verifier pipeline as the `stable` channel; the only difference is that the action-log
 * marker is `local_bundle_imported` with `channel=local`, regardless of what the
 * manifest's `source` field declares.
 *
 * The importer bounds the input stream at 1 MB (same cap as [SyncDownloader]) so a
 * pathologically large file does not blow up memory before the archive reader sees it.
 */
class LocalBundleImporter(
    private val archiveReader: BundleArchiveReader,
    private val verifier: BundleVerifier,
    private val store: BundleStore,
    private val actionLogger: ApplicationActionLogger?,
) {
    suspend fun import(stream: InputStream): SyncOutcome {
        val bytes = try {
            readBounded(stream)
        } catch (_: Exception) {
            return fail("read")
        }

        val readResult = archiveReader.read(bytes.inputStream())
        if (readResult.isFailure) return fail("read")
        val archive = readResult.getOrThrow()

        val verifyResult = verifier.verify(archive, appVersionCode = APP_VERSION_CODE)
        if (verifyResult.isFailure) return fail("verify")
        val verified = verifyResult.getOrThrow()

        val activateResult = store.activate(bytes, verified)
        if (activateResult.isFailure) return fail("activate")

        actionLogger?.log(
            com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
            mapOf("setting" to "local_bundle_imported", "channel" to "local"),
        )
        return SyncOutcome.Activated(verified)
    }

    private fun readBounded(stream: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            val n = stream.read(buf)
            if (n == -1) break
            total += n
            if (total > MAX_BODY_BYTES) error("local bundle exceeds 1 MB cap")
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    private suspend fun fail(step: String): SyncOutcome {
        actionLogger?.log(
            com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
            mapOf("setting" to "local_bundle_failed", "channel" to "local", "step" to step),
        )
        return SyncOutcome.Failed(step)
    }

    companion object {
        const val APP_VERSION_CODE = 1
        const val MAX_BODY_BYTES = 1024L * 1024L
        private const val BUFFER_SIZE = 8192
    }
}
