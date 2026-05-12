package com.qalqan.antifraud.crypto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class BundleArchiveTest {
    @Test
    fun `holds manifest bytes, 64-byte signature, and data entries`() {
        val manifest = "manifest".toByteArray()
        val signature = ByteArray(64) { it.toByte() }
        val data = mapOf("data/patterns.json" to "payload".toByteArray())
        val archive = BundleArchive(manifest, signature, data)

        archive.manifestBytes.contentEquals(manifest) shouldBe true
        archive.signature.contentEquals(signature) shouldBe true
        archive.dataEntries.keys shouldBe setOf("data/patterns.json")
    }

    @Test
    fun `signature must be exactly 64 bytes`() {
        shouldThrow<IllegalArgumentException> {
            BundleArchive(ByteArray(1), ByteArray(63), emptyMap())
        }
        shouldThrow<IllegalArgumentException> {
            BundleArchive(ByteArray(1), ByteArray(65), emptyMap())
        }
    }

    @Test
    fun `equals compares ByteArrays by content`() {
        val a =
            BundleArchive(
                manifestBytes = "m".toByteArray(),
                signature = ByteArray(64) { 0 },
                dataEntries = mapOf("data/x" to "x".toByteArray()),
            )
        val b =
            BundleArchive(
                manifestBytes = "m".toByteArray(),
                signature = ByteArray(64) { 0 },
                dataEntries = mapOf("data/x" to "x".toByteArray()),
            )
        (a == b) shouldBe true
        a.hashCode() shouldBe b.hashCode()
    }
}
