package com.qalqan.antifraud.database.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// No unit test: AndroidKeyStore JCA provider is unavailable under Robolectric 4.13.
// Verified by the instrumented test suite (post-Stage-1) and via the production code path.

/**
 * Spec §15 — Keystore-bound AES/GCM for short encrypted-at-rest blobs (phone E.164, SMS body excerpt,
 * user-note text). Envelope format: 12-byte IV || ciphertext || 16-byte GCM tag.
 *
 * The Keystore key never leaves the secure hardware (StrongBox where available); this class just
 * brokers Cipher operations. SQLCipher database key is bootstrapped separately in T48.
 */
class KeyStoreCryptoBox internal constructor(
    private val key: SecretKey,
) : CryptoBox {
    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
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
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val GCM_TAG_BYTES = 16
        private const val GCM_TAG_BITS = 128
        private const val KEY_BITS = 256

        @Suppress("UnusedParameter")
        fun create(
            context: Context,
            alias: String,
        ): KeyStoreCryptoBox {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val key = (ks.getKey(alias, null) as? SecretKey) ?: generateKey(alias)
            return KeyStoreCryptoBox(key)
        }

        private fun generateKey(alias: String): SecretKey {
            val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec =
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_BITS)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            gen.init(spec)
            return gen.generateKey()
        }
    }
}
