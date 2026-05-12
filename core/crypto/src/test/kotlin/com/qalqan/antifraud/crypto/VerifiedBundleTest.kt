package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.Instant

class VerifiedBundleTest {
    @Test
    fun `VerifiedBundle carries manifest and data entries`() {
        val manifest =
            BundleManifest(
                version = "v1",
                createdAt = Instant.parse("2026-05-12T10:00:00Z"),
                source = "stable",
                schemaVersion = 1,
                minAppVersion = 1,
                priority = BundlePriority.NORMAL,
                previousPackageId = null,
                contents = mapOf("data/patterns.json" to "sha256:${"a".repeat(64)}"),
            )
        val data = mapOf("data/patterns.json" to "payload".toByteArray())
        val v = VerifiedBundle(manifest, data)
        v.manifest shouldBe manifest
        v.dataEntries.keys shouldBe setOf("data/patterns.json")
    }

    @Test
    fun `VerificationError variants are distinguishable`() {
        val bad: VerificationError = VerificationError.BadSignature
        bad.shouldBeInstanceOf<VerificationError.BadSignature>()

        val chk: VerificationError = VerificationError.BadChecksum("data/patterns.json")
        chk.shouldBeInstanceOf<VerificationError.BadChecksum>()
        (chk as VerificationError.BadChecksum).path shouldBe "data/patterns.json"

        val sv: VerificationError = VerificationError.UnsupportedSchemaVersion(2)
        sv.shouldBeInstanceOf<VerificationError.UnsupportedSchemaVersion>()
        (sv as VerificationError.UnsupportedSchemaVersion).version shouldBe 2

        val app: VerificationError = VerificationError.AppTooOld(required = 99, current = 1)
        app.shouldBeInstanceOf<VerificationError.AppTooOld>()
        (app as VerificationError.AppTooOld).required shouldBe 99
        app.current shouldBe 1
    }
}
