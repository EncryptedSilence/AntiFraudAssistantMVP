package com.qalqan.antifraud.database.manual

import android.content.Context
import com.qalqan.antifraud.database.crypto.CryptoBox
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import java.io.File
import java.security.SecureRandom

/**
 * Spec §5.1 / §16.4 — exposes the per-install salt that [ManualEntry.WebSubmitter]
 * uses to hash `domainEtldPlusOne`, so the (post-MVP) auto-capture path produces
 * byte-identical `domainHash` values for the same eTLD+1.
 *
 * Mirrors [CallEntryDigest] and [SmsEntryDigest] for calls and SMS respectively.
 */
class WebEntryDigest private constructor(private val salt: ByteArray) {
    fun hash(canonicalEtldPlusOne: String): String =
        Hashing.saltedSha256(canonicalEtldPlusOne.trim().lowercase(), salt)

    companion object {
        private const val SALT_FILE = "antifraud.hash.salt"
        private const val SALT_BYTES = 16
        private const val DEFAULT_ALIAS = "antifraud.field_box"

        fun create(context: Context): WebEntryDigest {
            val box = KeyStoreCryptoBox.create(context, alias = DEFAULT_ALIAS)
            return create(context, box)
        }

        fun create(
            context: Context,
            box: CryptoBox,
        ): WebEntryDigest {
            val salt = obtainSalt(context, box)
            return WebEntryDigest(salt)
        }

        private fun obtainSalt(
            context: Context,
            box: CryptoBox,
        ): ByteArray {
            val file = File(context.filesDir, SALT_FILE)
            if (file.exists()) return box.decrypt(file.readBytes())
            val raw = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
            file.writeBytes(box.encrypt(raw))
            return raw
        }
    }
}
