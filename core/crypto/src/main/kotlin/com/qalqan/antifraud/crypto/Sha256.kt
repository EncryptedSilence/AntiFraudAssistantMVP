package com.qalqan.antifraud.crypto

import java.security.MessageDigest

/**
 * Spec §7.4 — `checksum` over update-package contents uses SHA-256. Pure JVM
 * (`java.security.MessageDigest`), no Tink, no Android API.
 *
 * Output is lowercase hex without a leading `sha256:` prefix; callers that need the
 * prefixed form (as in `BundleManifest.contents` map values) prepend it themselves.
 */
object Sha256 {
    fun hashHex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
