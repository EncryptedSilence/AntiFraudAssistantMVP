package com.qalqan.antifraud.database.crypto

import android.content.Context
import java.io.File
import java.security.SecureRandom

/**
 * Spec §15.1 — keys live in the Android Keystore (via [CryptoBox]); SQLCipher receives the 32 raw
 * bytes for `PRAGMA key`. We never persist the raw bytes in plaintext. The wrapped envelope lives
 * in a single file under the app's filesDir.
 */
class DatabaseKeyProvider(
    private val keyFile: File,
    private val cryptoBox: CryptoBox,
) {
    @Synchronized
    fun obtainKey(): ByteArray {
        if (keyFile.exists()) {
            val envelope = keyFile.readBytes()
            return cryptoBox.decrypt(envelope)
        }
        val raw = ByteArray(KEY_BYTES).also(SecureRandom()::nextBytes)
        val envelope = cryptoBox.encrypt(raw)
        keyFile.parentFile?.mkdirs()
        keyFile.writeBytes(envelope)
        return raw
    }

    @Synchronized
    fun deleteKey() {
        if (keyFile.exists()) keyFile.delete()
    }

    companion object {
        const val KEYSTORE_ALIAS: String = "antifraud.db_key_wrapper"
        const val KEY_FILE_NAME: String = "antifraud.dbkey.enc"
        private const val KEY_BYTES = 32

        fun fromContext(context: Context): DatabaseKeyProvider {
            val file = File(context.filesDir, KEY_FILE_NAME)
            val box = KeyStoreCryptoBox.create(context, KEYSTORE_ALIAS)
            return DatabaseKeyProvider(file, box)
        }
    }
}
