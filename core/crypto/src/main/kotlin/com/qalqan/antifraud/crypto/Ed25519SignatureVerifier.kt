@file:Suppress("ReturnCount")

package com.qalqan.antifraud.crypto

import com.google.crypto.tink.subtle.Ed25519Verify
import java.security.GeneralSecurityException

/**
 * Spec §7.4 — verifies an Ed25519 detached signature against a 32-byte public key.
 *
 * Backed by Google Tink's `Ed25519Verify` subtle API, which works on every Android
 * minSdk-26 device without depending on the platform `Signature` provider (which
 * gained Ed25519 only in API 33). Tink ships its own implementation.
 *
 * Returns `false` for any failure — wrong key, tampered message, tampered signature,
 * malformed inputs, or internal Tink exceptions. Never throws.
 */
class Ed25519SignatureVerifier {
    fun verify(
        message: ByteArray,
        signature: ByteArray,
        publicKey: ByteArray,
    ): Boolean {
        if (publicKey.size != ED25519_PUBLIC_KEY_BYTES) return false
        if (signature.size != ED25519_SIGNATURE_BYTES) return false
        return try {
            Ed25519Verify(publicKey).verify(signature, message)
            true
        } catch (_: GeneralSecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    companion object {
        const val ED25519_PUBLIC_KEY_BYTES = 32
        const val ED25519_SIGNATURE_BYTES = 64
    }
}
