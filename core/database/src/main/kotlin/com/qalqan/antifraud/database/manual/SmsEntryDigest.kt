@file:Suppress("MaxLineLength")

package com.qalqan.antifraud.database.manual

import android.content.Context
import com.qalqan.antifraud.database.crypto.CryptoBox
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.domain.SenderHash
import java.io.File
import java.security.SecureRandom

/**
 * Spec §4.2.2 + §5.1 — auto-capture (Stage 4) and manual entry (Stage 1) must produce the
 * same salted hash for the same sender, so a `ContactProfile` (or future SMS-sender profile)
 * survives whichever path the user actually used. `SmsEntryDigest` reads the same salt file
 * `ManualEntry` writes, so both paths agree byte-for-byte.
 *
 * Hash recipe must match `ManualEntry.SmsSubmitter`: `Hashing.saltedSha256(sender.trim(), salt)`.
 */
class SmsEntryDigest internal constructor(
    private val salt: ByteArray,
) {
    fun hash(rawSender: String): SenderHash = SenderHash(Hashing.saltedSha256(rawSender.trim(), salt))

    companion object {
        private const val SALT_FILE = "antifraud.hash.salt"
        private const val SALT_BYTES = 16

        fun create(context: Context): SmsEntryDigest = create(context, KeyStoreCryptoBox.create(context, alias = "antifraud.field_box"))

        fun create(
            context: Context,
            box: CryptoBox,
        ): SmsEntryDigest = SmsEntryDigest(salt = obtainSalt(context, box))

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
