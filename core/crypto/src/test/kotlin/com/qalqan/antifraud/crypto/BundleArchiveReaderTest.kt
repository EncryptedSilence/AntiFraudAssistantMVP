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
}
