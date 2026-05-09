package com.qalqan.antifraud.database

import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.qalqan.antifraud.database.crypto.DatabaseKeyProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Spec §15.1 — wraps SQLCipher's OpenHelperFactory with a key materialised through the
 * [DatabaseKeyProvider], which itself uses a [com.qalqan.antifraud.database.crypto.CryptoBox]
 * (production: AndroidKeyStore-bound AES/GCM).
 */
internal fun sqlCipherFactory(keyProvider: DatabaseKeyProvider): SupportSQLiteOpenHelper.Factory {
    System.loadLibrary("sqlcipher")
    val key = keyProvider.obtainKey()
    return SupportOpenHelperFactory(key)
}
