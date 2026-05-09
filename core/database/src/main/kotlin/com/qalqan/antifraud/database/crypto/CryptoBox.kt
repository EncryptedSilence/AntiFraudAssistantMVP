package com.qalqan.antifraud.database.crypto

/**
 * Spec §15 — at-rest encryption primitive. Production binds the key to AndroidKeyStore;
 * tests use a stock-JCA implementation with an ephemeral in-process key.
 */
interface CryptoBox {
    fun encrypt(plaintext: ByteArray): ByteArray

    fun decrypt(ciphertext: ByteArray): ByteArray
}
