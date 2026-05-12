package com.qalqan.antifraud.crypto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class BundleManifestTest {
    private fun validContents(): Map<String, String> = mapOf(
        "data/patterns.json" to "sha256:" + "a".repeat(64),
    )

    @Test
    fun `valid manifest is constructible`() {
        val m = BundleManifest(
            version = "2026.05.12-001",
            createdAt = Instant.parse("2026-05-12T10:00:00Z"),
            source = "stable",
            schemaVersion = 1,
            minAppVersion = 1,
            priority = BundlePriority.NORMAL,
            previousPackageId = null,
            contents = validContents(),
        )
        m.version shouldBe "2026.05.12-001"
        m.priority shouldBe BundlePriority.NORMAL
        m.contents.size shouldBe 1
    }

    @Test
    fun `priority enum has NORMAL and URGENT`() {
        BundlePriority.NORMAL.name shouldBe "NORMAL"
        BundlePriority.URGENT.name shouldBe "URGENT"
        BundlePriority.entries.size shouldBe 2
    }

    @Test
    fun `blank version is rejected`() {
        shouldThrow<IllegalArgumentException> {
            BundleManifest(
                version = "  ",
                createdAt = Instant.now(),
                source = "stable",
                schemaVersion = 1,
                minAppVersion = 1,
                priority = BundlePriority.NORMAL,
                previousPackageId = null,
                contents = validContents(),
            )
        }
    }
}
