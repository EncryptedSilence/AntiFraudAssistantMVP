package com.qalqan.antifraud.crypto

import java.time.Instant

/**
 * Spec §7.4 — in-memory shape of an `.afpkg` manifest. Construction enforces every
 * structural invariant so the verifier (Phase 4) can rely on any `BundleManifest`
 * value being well-formed. Semantic checks (signature, per-file SHA-256, schemaVersion
 * support, minAppVersion vs running app) are downstream.
 */
data class BundleManifest(
    val version: String,
    val createdAt: Instant,
    val source: String,
    val schemaVersion: Int,
    val minAppVersion: Int,
    val priority: BundlePriority,
    val previousPackageId: String?,
    val contents: Map<String, String>,
) {
    init {
        require(version.isNotBlank()) { "version must be non-blank" }
        require(schemaVersion >= 1) { "schemaVersion must be >= 1, was $schemaVersion" }
        require(minAppVersion >= 1) { "minAppVersion must be >= 1, was $minAppVersion" }
        require(contents.isNotEmpty()) { "contents must be non-empty" }
        contents.forEach { (path, hash) ->
            require(path.startsWith("data/")) {
                "contents path must start with 'data/': '$path'"
            }
            require(!path.contains("..") && !path.contains("/./") && !path.startsWith("/")) {
                "contents path must not contain path-escape sequences: '$path'"
            }
            require(SHA256_PREFIXED.matches(hash)) {
                "contents hash for '$path' must match 'sha256:<64-hex>', was '$hash'"
            }
        }
    }

    companion object {
        private val SHA256_PREFIXED = Regex("^sha256:[0-9a-f]{64}$")
    }
}

/** Spec §7.4 — supported priorities are exactly `normal` and `urgent`. */
enum class BundlePriority {
    NORMAL,
    URGENT,
}
