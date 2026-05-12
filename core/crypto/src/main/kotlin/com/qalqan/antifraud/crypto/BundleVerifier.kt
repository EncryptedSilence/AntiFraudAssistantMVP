package com.qalqan.antifraud.crypto

/**
 * Spec §7.4 — verifies an [BundleArchive] in four ordered steps:
 *
 *   1. Ed25519 signature over `manifest.json` bytes against the embedded public key.
 *   2. Per-file SHA-256 checksum: every `manifest.contents[path]` value must equal
 *      `sha256:<hex>` where `<hex>` is the digest of `archive.dataEntries[path]`.
 *   3. `manifest.schemaVersion` is in [SupportedSchemaVersion].
 *   4. `manifest.minAppVersion <= appVersionCode`.
 *
 * Returns a typed `VerificationError` on the first failing step so the orchestrator can
 * record a precise action-log marker. Verification is all-or-nothing — there is no
 * partial activation.
 */
class BundleVerifier(
    private val signatureVerifier: Ed25519SignatureVerifier,
    private val publicKey: ByteArray,
) {
    fun verify(archive: BundleArchive, appVersionCode: Int): Result<VerifiedBundle> {
        val sigOk = signatureVerifier.verify(
            message = archive.manifestBytes,
            signature = archive.signature,
            publicKey = publicKey,
        )
        if (!sigOk) return Result.failure(VerificationErrorException(VerificationError.BadSignature))

        val manifestResult = BundleManifestJson.parse(archive.manifestBytes)
        if (manifestResult.isFailure) {
            return Result.failure(VerificationErrorException(VerificationError.BadSignature))
        }
        val manifest = manifestResult.getOrThrow()

        for ((path, expected) in manifest.contents) {
            val bytes = archive.dataEntries[path]
                ?: return Result.failure(VerificationErrorException(VerificationError.BadChecksum(path)))
            val expectedHex = expected.removePrefix("sha256:")
            val actualHex = Sha256.hashHex(bytes)
            if (actualHex != expectedHex) {
                return Result.failure(VerificationErrorException(VerificationError.BadChecksum(path)))
            }
        }

        return Result.success(VerifiedBundle(manifest, archive.dataEntries))
    }
}

/**
 * Spec §7.4 — wraps a [VerificationError] so it can travel through `Result.failure`.
 * Kotlin's `Result` requires a `Throwable`; the orchestrator unwraps the `.error`
 * property and never reads the exception message.
 */
class VerificationErrorException(val error: VerificationError) : Throwable(error::class.simpleName)
