package com.qalqan.antifraud.sync

import android.content.Context
import com.qalqan.antifraud.crypto.VerifiedBundle
import java.io.File
import java.io.FileOutputStream

/**
 * Spec §7.4 — atomic on-disk store for verified bundles.
 *
 * Layout under `filesDir/sync/`:
 *   - `current/`                — the currently active bundle (`bundle.afpkg` + data files)
 *   - `previous/`               — the prior bundle, kept as a single fallback (N = 1)
 *   - `current.tmp/`            — staging area used during [activate]
 *
 * Synced bundles live on app-internal storage and are NOT encrypted: bundles are signed
 * and publicly distributed, so the on-device copy is treated as a public artifact. The
 * §23 #19 encrypted-storage criterion does not apply to public, signed payloads.
 */
class BundleStore(context: Context) {
    private val root = File(context.filesDir, "sync")
    private val currentDir = File(root, "current")
    private val previousDir = File(root, "previous")
    private val tmpDir = File(root, "current.tmp")

    fun activate(rawBundleBytes: ByteArray, verified: VerifiedBundle): Result<Unit> = runCatching {
        root.mkdirs()
        tmpDir.deleteRecursively()
        tmpDir.mkdirs()

        writeAndFsync(File(tmpDir, "bundle.afpkg"), rawBundleBytes)
        verified.dataEntries.forEach { (path, bytes) ->
            val out = File(tmpDir, path)
            out.parentFile?.mkdirs()
            writeAndFsync(out, bytes)
        }

        if (currentDir.exists()) {
            previousDir.deleteRecursively()
            check(currentDir.renameTo(previousDir)) {
                "failed to rotate current to previous"
            }
        }
        check(tmpDir.renameTo(currentDir)) {
            "failed to rename tmp to current"
        }
        Unit
    }

    private fun writeAndFsync(file: File, bytes: ByteArray) {
        FileOutputStream(file).use { fos ->
            fos.write(bytes)
            fos.fd.sync()
        }
    }

    companion object {
        const val CURRENT_DIR = "current"
        const val PREVIOUS_DIR = "previous"
    }
}

/** Spec §7.4 — typed failure modes for [BundleStore]. */
sealed class BundleStoreError(message: String) : Throwable(message) {
    data object NoPreviousBundle : BundleStoreError("no previous bundle to roll back to")
    data class IoError(val reason: Throwable) : BundleStoreError(reason.message ?: "io error")
}
