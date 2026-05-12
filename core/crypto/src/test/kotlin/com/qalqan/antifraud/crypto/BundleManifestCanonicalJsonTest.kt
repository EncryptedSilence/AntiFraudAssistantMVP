package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class BundleManifestCanonicalJsonTest {
    private fun fixture(
        contents: Map<String, String> = mapOf(
            "data/patterns.json" to "sha256:" + "a".repeat(64),
            "data/domains.json" to "sha256:" + "b".repeat(64),
        ),
    ): BundleManifest = BundleManifest(
        version = "2026.05.12-001",
        createdAt = Instant.parse("2026-05-12T10:00:00Z"),
        source = "stable",
        schemaVersion = 1,
        minAppVersion = 1,
        priority = BundlePriority.NORMAL,
        previousPackageId = null,
        contents = contents,
    )

    @Test
    fun `canonical json contains alphabetically sorted contents keys`() {
        val bytes = BundleManifestJson.toCanonicalJson(fixture())
        val s = bytes.toString(Charsets.UTF_8)
        val domainsIdx = s.indexOf("data/domains.json")
        val patternsIdx = s.indexOf("data/patterns.json")
        (domainsIdx in 0 until patternsIdx) shouldBe true
    }

    @Test
    fun `two semantically equal manifests produce byte-identical canonical json`() {
        val a = BundleManifestJson.toCanonicalJson(fixture())
        val b = BundleManifestJson.toCanonicalJson(
            fixture(
                contents = linkedMapOf(
                    "data/patterns.json" to "sha256:" + "a".repeat(64),
                    "data/domains.json" to "sha256:" + "b".repeat(64),
                ),
            ),
        )
        a.contentEquals(b) shouldBe true
    }

    @Test
    fun `round-trips parse to toCanonicalJson`() {
        val m = fixture()
        val canonical = BundleManifestJson.toCanonicalJson(m)
        val reparsed = BundleManifestJson.parse(canonical).getOrThrow()
        reparsed shouldBe m
    }

    @Test
    fun `canonical json contains no whitespace`() {
        val s = BundleManifestJson.toCanonicalJson(fixture()).toString(Charsets.UTF_8)
        (s.any { it == '\n' || it == '\r' || it == '\t' }) shouldBe false
        s.contains(", \"") shouldBe false
        s.contains("\": ") shouldBe false
    }
}
