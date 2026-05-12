package com.qalqan.antifraud.crypto

/**
 * Fixed test-only Ed25519 keypair used by [Ed25519SignatureVerifierTest] and downstream
 * bundle-verifier tests. NOT the production key — the production public key lives at
 * `core/crypto/src/main/res/raw/sync_public_key.bin` and is shipped in the APK.
 */
object TestKeys {
    const val TEST_PUBLIC_KEY_HEX = "dc5e8cd1d6bac038938cc7477042e26c62d889f2e61f43b1475c113dbd83a0b3"
    const val TEST_PRIVATE_KEY_HEX = "de8b7794a198f086747e009faaf1668e7742424b041ef8caa36a94582d674835"
    const val TEST_SIGNATURE_FOR_HELLO_HEX = "8bb2a3415b7589f473b51166f872e00c888f71eee63ed60778d3801fcb21c9a1454a089305a6999f1088fb417d9e273aca6362a16bbfa1bf6d287f781d0dce0e"

    fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "hex length must be even" }
        return ByteArray(hex.length / 2) { i ->
            ((Character.digit(hex[i * 2], 16) shl 4) or Character.digit(hex[i * 2 + 1], 16)).toByte()
        }
    }
}
