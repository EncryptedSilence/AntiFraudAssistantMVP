@file:Suppress("ReturnCount")

package com.qalqan.antifraud.crypto

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/**
 * Spec §7.4 — reads an `.afpkg` ZIP into a [BundleArchive]. Performs no cryptographic
 * verification; that is the [BundleVerifier]'s job. The reader's job is to extract bytes
 * safely without crashing on malformed or hostile archives.
 *
 * DoS caps and path-allowlist enforcement land in T13.
 */
class BundleArchiveReader {
    fun read(input: InputStream): Result<BundleArchive> = runCatching {
        var manifestBytes: ByteArray? = null
        var signature: ByteArray? = null
        val dataEntries = mutableMapOf<String, ByteArray>()
        var totalBytes = 0L

        try {
            ZipInputStream(input).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val name = entry.name
                        checkAllowedPath(name)
                        val bytes = zis.readEntryBytesBounded(name)
                        totalBytes += bytes.size
                        if (totalBytes > MAX_TOTAL_BYTES) {
                            throw BundleArchiveError.ArchiveTooLarge(totalBytes)
                        }
                        when {
                            name == MANIFEST_NAME -> manifestBytes = bytes
                            name == SIGNATURE_NAME -> signature = bytes
                            name.startsWith(DATA_PREFIX) -> dataEntries[name] = bytes
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: ZipException) {
            throw BundleArchiveError.MalformedZip(e.message ?: "malformed zip")
        } catch (e: IOException) {
            throw BundleArchiveError.MalformedZip(e.message ?: "io error")
        }

        val m = manifestBytes ?: throw BundleArchiveError.MissingManifest
        val s = signature ?: throw BundleArchiveError.MissingSignature
        if (s.size != BundleArchive.ED25519_SIGNATURE_BYTES) {
            throw BundleArchiveError.SignatureWrongSize(s.size)
        }
        BundleArchive(manifestBytes = m, signature = s, dataEntries = dataEntries)
    }

    private fun checkAllowedPath(name: String) {
        if (name.startsWith("/") || name.contains("..") || name.contains("\\")) {
            throw BundleArchiveError.ForbiddenPath(name)
        }
        val allowed = name == MANIFEST_NAME ||
            name == SIGNATURE_NAME ||
            name.startsWith(DATA_PREFIX)
        if (!allowed) throw BundleArchiveError.ForbiddenPath(name)
    }

    private fun ZipInputStream.readEntryBytesBounded(name: String): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            val n = read(buf)
            if (n == -1) break
            total += n
            if (total > MAX_ENTRY_BYTES) {
                throw BundleArchiveError.EntryTooLarge(name, total)
            }
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    companion object {
        const val MANIFEST_NAME = "manifest.json"
        const val SIGNATURE_NAME = "signature"
        const val DATA_PREFIX = "data/"
        const val MAX_ENTRY_BYTES = 256L * 1024L
        const val MAX_TOTAL_BYTES = 1024L * 1024L
        private const val BUFFER_SIZE = 8192
    }
}

/**
 * Spec §7.4 — typed failure modes for [BundleArchiveReader.read]. Defined here (next to
 * the reader) so call sites can `when`-match exhaustively without an unrelated import.
 */
sealed class BundleArchiveError(message: String) : Throwable(message) {
    data object MissingManifest : BundleArchiveError("manifest.json is missing")
    data object MissingSignature : BundleArchiveError("signature is missing")
    data class SignatureWrongSize(val sizeBytes: Int) :
        BundleArchiveError("signature must be 64 bytes, was $sizeBytes")
    data class ForbiddenPath(val name: String) :
        BundleArchiveError("entry path is outside allowlist: '$name'")
    data class EntryTooLarge(val name: String, val sizeBytes: Long) :
        BundleArchiveError("entry '$name' is $sizeBytes bytes, exceeds per-entry cap")
    data class ArchiveTooLarge(val sizeBytes: Long) :
        BundleArchiveError("archive uncompressed size is $sizeBytes bytes, exceeds total cap")
    data class MalformedZip(val reason: String) :
        BundleArchiveError("malformed zip: $reason")
}
