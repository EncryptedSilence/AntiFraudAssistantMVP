package com.qalqan.antifraud.crypto

import io.kotest.assertions.throwables.shouldThrow
import org.junit.Test
import java.time.Instant

class BundleManifestBoundaryTest {
    private fun build(
        version: String = "v1",
        schemaVersion: Int = 1,
        minAppVersion: Int = 1,
        contents: Map<String, String> = mapOf("data/patterns.json" to "sha256:${"a".repeat(64)}"),
    ): BundleManifest =
        BundleManifest(
            version = version,
            createdAt = Instant.parse("2026-05-12T10:00:00Z"),
            source = "stable",
            schemaVersion = schemaVersion,
            minAppVersion = minAppVersion,
            priority = BundlePriority.NORMAL,
            previousPackageId = null,
            contents = contents,
        )

    @Test
    fun `blank version is rejected`() {
        shouldThrow<IllegalArgumentException> { build(version = "") }
        shouldThrow<IllegalArgumentException> { build(version = "   ") }
    }

    @Test
    fun `schemaVersion less than 1 is rejected`() {
        shouldThrow<IllegalArgumentException> { build(schemaVersion = 0) }
        shouldThrow<IllegalArgumentException> { build(schemaVersion = -5) }
    }

    @Test
    fun `minAppVersion less than 1 is rejected`() {
        shouldThrow<IllegalArgumentException> { build(minAppVersion = 0) }
    }

    @Test
    fun `empty contents map is rejected`() {
        shouldThrow<IllegalArgumentException> { build(contents = emptyMap()) }
    }

    @Test
    fun `contents value missing sha256 prefix is rejected`() {
        shouldThrow<IllegalArgumentException> {
            build(contents = mapOf("data/patterns.json" to "a".repeat(64)))
        }
    }

    @Test
    fun `contents value with wrong hash length is rejected`() {
        shouldThrow<IllegalArgumentException> {
            build(contents = mapOf("data/patterns.json" to "sha256:" + "a".repeat(63)))
        }
        shouldThrow<IllegalArgumentException> {
            build(contents = mapOf("data/patterns.json" to "sha256:" + "a".repeat(65)))
        }
    }

    @Test
    fun `contents key not starting with data slash is rejected`() {
        shouldThrow<IllegalArgumentException> {
            build(contents = mapOf("patterns.json" to "sha256:${"a".repeat(64)}"))
        }
        shouldThrow<IllegalArgumentException> {
            build(contents = mapOf("other/patterns.json" to "sha256:${"a".repeat(64)}"))
        }
    }

    @Test
    fun `contents key containing parent-directory escape is rejected`() {
        shouldThrow<IllegalArgumentException> {
            build(contents = mapOf("data/../etc/passwd" to "sha256:${"a".repeat(64)}"))
        }
    }
}
