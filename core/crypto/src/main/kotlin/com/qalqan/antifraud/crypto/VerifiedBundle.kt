package com.qalqan.antifraud.crypto

/**
 * Spec §7.4 — return value of [BundleVerifier.verify]. Carries the parsed manifest plus
 * the data-entry bytes that were already loaded by the archive reader. Downstream
 * consumers (the orchestrator) write these to disk via [BundleStore].
 */
data class VerifiedBundle(
    val manifest: BundleManifest,
    val dataEntries: Map<String, ByteArray>,
)

/**
 * Spec §7.4 — the four reasons a bundle is rejected. The orchestrator translates these
 * into action-log state markers (`bad_signature`, `bad_checksum`, `unsupported_schema`,
 * `app_too_old`); no error string makes it past the `:core:sync` boundary.
 */
sealed interface VerificationError {
    data object BadSignature : VerificationError
    data class BadChecksum(val path: String) : VerificationError
    data class UnsupportedSchemaVersion(val version: Int) : VerificationError
    data class AppTooOld(val required: Int, val current: Int) : VerificationError
}
