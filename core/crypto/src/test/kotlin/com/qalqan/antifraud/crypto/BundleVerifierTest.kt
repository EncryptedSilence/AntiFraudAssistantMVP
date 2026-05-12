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
    ): BundleManifest =
        BundleManifest(
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
        val bad =
            BundleArchive(
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

    @Test
    fun `happy path returns Success when signature and all checksums pass`() {
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey)
        val r = verifier.verify(signedArchive(), appVersionCode = 1)
        r.isSuccess shouldBe true
        r.getOrThrow().manifest.version shouldBe "2026.05.12-001"
        r.getOrThrow().dataEntries.keys shouldBe setOf("data/patterns.json")
    }

    @Test
    fun `tampered data file yields BadChecksum with the offending path`() {
        val archive = signedArchive()
        val mutated =
            archive.dataEntries.toMutableMap().also {
                it["data/patterns.json"] = "DIFFERENT".toByteArray()
            }
        val bad =
            BundleArchive(
                manifestBytes = archive.manifestBytes,
                signature = archive.signature,
                dataEntries = mutated,
            )
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey)
        val r = verifier.verify(bad, appVersionCode = 1)
        r.isFailure shouldBe true
        val err = (r.exceptionOrNull() as VerificationErrorException).error
        err shouldBe VerificationError.BadChecksum("data/patterns.json")
    }

    @Test
    fun `missing data entry yields BadChecksum with the missing path`() {
        val archive = signedArchive()
        val bad =
            BundleArchive(
                manifestBytes = archive.manifestBytes,
                signature = archive.signature,
                dataEntries = emptyMap(),
            )
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey)
        val r = verifier.verify(bad, appVersionCode = 1)
        r.isFailure shouldBe true
        val err = (r.exceptionOrNull() as VerificationErrorException).error
        err shouldBe VerificationError.BadChecksum("data/patterns.json")
    }

    @Test
    fun `unsupported schemaVersion is rejected with UnsupportedSchemaVersion`() {
        val m = manifest(schemaVersion = 2)
        val canonical = BundleManifestJson.toCanonicalJson(m)
        val archive =
            BundleArchive(
                manifestBytes = canonical,
                signature = TestKeys.signWithTestKey(canonical),
                dataEntries = mapOf("data/patterns.json" to patternsBytes),
            )
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey)
        val r = verifier.verify(archive, appVersionCode = 1)
        r.isFailure shouldBe true
        val err = (r.exceptionOrNull() as VerificationErrorException).error
        err shouldBe VerificationError.UnsupportedSchemaVersion(2)
    }

    @Test
    fun `minAppVersion greater than appVersionCode yields AppTooOld`() {
        val m = manifest(minAppVersion = 999)
        val canonical = BundleManifestJson.toCanonicalJson(m)
        val archive =
            BundleArchive(
                manifestBytes = canonical,
                signature = TestKeys.signWithTestKey(canonical),
                dataEntries = mapOf("data/patterns.json" to patternsBytes),
            )
        val verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey)
        val r = verifier.verify(archive, appVersionCode = 1)
        r.isFailure shouldBe true
        val err = (r.exceptionOrNull() as VerificationErrorException).error
        err shouldBe VerificationError.AppTooOld(required = 999, current = 1)
    }
}
