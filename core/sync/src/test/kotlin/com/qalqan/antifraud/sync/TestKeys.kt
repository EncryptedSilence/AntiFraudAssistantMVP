package com.qalqan.antifraud.sync

/**
 * Test-only Ed25519 keypair, duplicated from `:core:crypto/src/test/.../TestKeys.kt`.
 * Test source sets are not shared across modules; this duplicate lets `:core:sync` tests
 * sign canonical manifest bytes with the same fixture key the verifier accepts.
 * NOT the production key.
 */
object TestKeys {
    const val TEST_PUBLIC_KEY_HEX = "dc5e8cd1d6bac038938cc7477042e26c62d889f2e61f43b1475c113dbd83a0b3"
    const val TEST_PRIVATE_KEY_HEX = "de8b7794a198f086747e009faaf1668e7742424b041ef8caa36a94582d674835"

    fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "hex length must be even" }
        return ByteArray(hex.length / 2) { i ->
            ((Character.digit(hex[i * 2], 16) shl 4) or Character.digit(hex[i * 2 + 1], 16)).toByte()
        }
    }

    fun signWithTestKey(message: ByteArray): ByteArray {
        val signer = com.google.crypto.tink.subtle.Ed25519Sign(hexToBytes(TEST_PRIVATE_KEY_HEX))
        return signer.sign(message)
    }
}
