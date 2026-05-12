package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BundleArchiveReaderTest {
    private fun zip(entries: List<Pair<String, ByteArray>>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            entries.forEach { (name, bytes) ->
                zos.putNextEntry(ZipEntry(name))
                zos.write(bytes)
                zos.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    @Test
    fun `reads manifest, signature, and one data entry`() {
        val manifest = "manifest-bytes".toByteArray()
        val signature = ByteArray(64) { it.toByte() }
        val patterns = "patterns-payload".toByteArray()

        val bytes = zip(
            listOf(
                "manifest.json" to manifest,
                "signature" to signature,
                "data/patterns.json" to patterns,
            ),
        )

        val result = BundleArchiveReader().read(ByteArrayInputStream(bytes))
        result.isSuccess shouldBe true
        val archive = result.getOrThrow()
        archive.manifestBytes.contentEquals(manifest) shouldBe true
        archive.signature.contentEquals(signature) shouldBe true
        archive.dataEntries.keys shouldBe setOf("data/patterns.json")
        archive.dataEntries.getValue("data/patterns.json").contentEquals(patterns) shouldBe true
    }

    @Test
    fun `rejects entry larger than 256 KB`() {
        val signature = ByteArray(64)
        val oversize = ByteArray(MAX_ENTRY_BYTES_PLUS_ONE) { 'a'.code.toByte() }
        val bytes = zip(
            listOf(
                "manifest.json" to "m".toByteArray(),
                "signature" to signature,
                "data/big.json" to oversize,
            ),
        )
        val r = BundleArchiveReader().read(ByteArrayInputStream(bytes))
        r.isFailure shouldBe true
        (r.exceptionOrNull() is BundleArchiveError.EntryTooLarge) shouldBe true
    }

    @Test
    fun `rejects total uncompressed size exceeding 1 MB`() {
        val signature = ByteArray(64)
        val chunk = ByteArray(MAX_ENTRY_BYTES) { 'x'.code.toByte() }
        val bytes = zip(
            buildList {
                add("manifest.json" to "m".toByteArray())
                add("signature" to signature)
                repeat(5) { i -> add("data/chunk-$i.bin" to chunk) }
            },
        )
        val r = BundleArchiveReader().read(ByteArrayInputStream(bytes))
        r.isFailure shouldBe true
        val ex = r.exceptionOrNull()
        (ex is BundleArchiveError.EntryTooLarge || ex is BundleArchiveError.ArchiveTooLarge) shouldBe true
    }

    @Test
    fun `rejects entry whose name contains parent-directory escape`() {
        val signature = ByteArray(64)
        val bytes = zip(
            listOf(
                "manifest.json" to "m".toByteArray(),
                "signature" to signature,
                "data/../etc/passwd" to "x".toByteArray(),
            ),
        )
        val r = BundleArchiveReader().read(ByteArrayInputStream(bytes))
        r.isFailure shouldBe true
        (r.exceptionOrNull() is BundleArchiveError.ForbiddenPath) shouldBe true
    }

    @Test
    fun `rejects entry whose name starts with a slash`() {
        val signature = ByteArray(64)
        val bytes = zip(
            listOf(
                "manifest.json" to "m".toByteArray(),
                "signature" to signature,
                "/etc/passwd" to "x".toByteArray(),
            ),
        )
        val r = BundleArchiveReader().read(ByteArrayInputStream(bytes))
        r.isFailure shouldBe true
        (r.exceptionOrNull() is BundleArchiveError.ForbiddenPath) shouldBe true
    }

    companion object {
        private const val MAX_ENTRY_BYTES = 256 * 1024
        private const val MAX_ENTRY_BYTES_PLUS_ONE = MAX_ENTRY_BYTES + 1
    }
}
