package com.qalqan.antifraud.database.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Stock-JCA AES/GCM CryptoBox for unit tests and JVM-only fixtures. Has no Android dependency
 * and never touches AndroidKeyStore. The production code path uses [KeyStoreCryptoBox].
 *
 * Envelope format matches [KeyStoreCryptoBox]: 12-byte IV || ciphertext || 16-byte GCM tag.
 */
class InMemoryCryptoBox(
    private val key: SecretKey = generateKey(),
    private val random: SecureRandom = SecureRandom(),
) : CryptoBox {
    constructor(rawKey: ByteArray) : this(SecretKeySpec(rawKey, "AES"))

    override fun encrypt(plaintext: ByteArray): ByteArray {
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ct = cipher.doFinal(plaintext)
        return iv + ct
    }

    override fun decrypt(ciphertext: ByteArray): ByteArray {
        require(ciphertext.size >= IV_BYTES + GCM_TAG_BYTES) {
            "envelope too short to contain IV and GCM tag"
        }
        val iv = ciphertext.copyOfRange(0, IV_BYTES)
        val ct = ciphertext.copyOfRange(IV_BYTES, ciphertext.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ct)
    }

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val GCM_TAG_BYTES = 16
        private const val GCM_TAG_BITS = 128
        private const val KEY_BITS = 256

        private fun generateKey(): SecretKey {
            val gen = KeyGenerator.getInstance("AES")
            gen.init(KEY_BITS)
            return gen.generateKey()
        }
    }
}
