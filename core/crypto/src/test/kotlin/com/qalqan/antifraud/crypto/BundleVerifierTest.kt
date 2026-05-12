package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class BundleVerifierTest {
    private val publicKey = TestKeys.hexToBytes(TestKeys.TEST_PUBLIC_KEY_HEX)
    private val patternsBytes = "patterns-payload".toByteArray(Charsets.UTF_8)
    private val patternsHash = Sha256.hashHex(patternsBytes)

    private fun manifest(
        schemaVersion: Int = 1,
        minAppVersion: Int = 1,
    ): BundleManifest = BundleManifest(
        version = "2026.05.12-001",
        createdAt = Instant.parse("2026-05-12T10:00:00Z"),
        source = "stable",
        schemaVersion = schemaVersion,
        minAppVersion = minAppVersion,
        priority = BundlePriority.NORMAL,
        previousPackageId = null,
        contents = mapOf("data/patterns.json" to "sha256:$patternsHash"),
    )

    private fun signedArchive(m: BundleManifest = manifest()): BundleArchive {
        val canonical = BundleManifestJson.toCanonicalJson(m)
        val signature = TestKeys.signWithTestKey(canonical)
        return BundleArchive(
            manifestBytes = canonical,
            signature = signature,
            dataEntries = mapOf("data/patterns.json" to patternsBytes),
        )
    }

    @Test
    fun `tampered manifest fails signature verification`() {
        val archive = signedArchive()
        val tampered = archive.manifestBytes.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        val bad = BundleArchive(
            manifestBytes = tampered,
            signature = archive.signature,
            dataEntries = archive.dataEntries,
        )
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey)
        val r = verifier.verify(bad, appVersionCode = 1)
        r.isFailure shouldBe true
        (r.exceptionOrNull() as VerificationErrorException).error shouldBe VerificationError.BadSignature
    }

    @Test
    fun `wrong public key fails signature verification`() {
        val archive = signedArchive()
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), ByteArray(32) { i -> i.toByte() })
        val r = verifier.verify(archive, appVersionCode = 1)
        r.isFailure shouldBe true
        (r.exceptionOrNull() as VerificationErrorException).error shouldBe VerificationError.BadSignature
    }
}
