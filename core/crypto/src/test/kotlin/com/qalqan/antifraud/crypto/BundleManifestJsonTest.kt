package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.Instant

class BundleManifestJsonTest {
    private val validJson = """
        {
          "version": "2026.05.12-001",
          "createdAt": "2026-05-12T10:00:00Z",
          "source": "stable",
          "schemaVersion": 1,
          "minAppVersion": 1,
          "priority": "NORMAL",
          "previousPackageId": null,
          "contents": {
            "data/patterns.json": "sha256:${"a".repeat(64)}"
          }
        }
    """.trimIndent().toByteArray(Charsets.UTF_8)

    @Test
    fun `parses a valid manifest`() {
        val r = BundleManifestJson.parse(validJson)
        r.isSuccess shouldBe true
        val m = r.getOrThrow()
        m.version shouldBe "2026.05.12-001"
        m.createdAt shouldBe Instant.parse("2026-05-12T10:00:00Z")
        m.priority shouldBe BundlePriority.NORMAL
        m.previousPackageId shouldBe null
        m.contents.keys shouldBe setOf("data/patterns.json")
    }

    @Test
    fun `rejects missing required field`() {
        val missingVersion = """
            {
              "createdAt": "2026-05-12T10:00:00Z",
              "source": "stable",
              "schemaVersion": 1,
              "minAppVersion": 1,
              "priority": "NORMAL",
              "previousPackageId": null,
              "contents": { "data/patterns.json": "sha256:${"a".repeat(64)}" }
            }
        """.trimIndent().toByteArray(Charsets.UTF_8)
        BundleManifestJson.parse(missingVersion).isFailure shouldBe true
    }

    @Test
    fun `rejects schemaVersion as a non-numeric JSON string`() {
        // Moshi's reflective adapter coerces numeric strings ("1") to Int per its
        // documented lenient-number behavior; use a non-numeric string to assert that
        // a genuinely wrong type is rejected.
        val badType = """
            {
              "version": "v1",
              "createdAt": "2026-05-12T10:00:00Z",
              "source": "stable",
              "schemaVersion": "abc",
              "minAppVersion": 1,
              "priority": "NORMAL",
              "previousPackageId": null,
              "contents": { "data/patterns.json": "sha256:${"a".repeat(64)}" }
            }
        """.trimIndent().toByteArray(Charsets.UTF_8)
        val r = BundleManifestJson.parse(badType)
        r.isFailure shouldBe true
        r.exceptionOrNull().shouldBeInstanceOf<Throwable>()
    }
}
