@file:Suppress("MaxLineLength")

package com.qalqan.antifraud.database.manual

import android.content.Context
import com.qalqan.antifraud.database.crypto.CryptoBox
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.domain.PhoneHash
import java.io.File
import java.security.SecureRandom

/**
 * Spec §5.1 — auto-capture (Stage 3) and manual entry (Stage 1) must produce the same
 * salted hash for the same phone, so a `ContactProfile` survives the path the user
 * actually used. `CallEntryDigest` reads the same salt file `ManualEntry` writes,
 * so both paths agree byte-for-byte.
 */
class CallEntryDigest internal constructor(
    private val phone: PhoneNormalizer,
    private val salt: ByteArray,
) {
    fun hash(rawNumber: String): PhoneHash = PhoneHash(Hashing.saltedSha256(phone.normalize(rawNumber).normalizedE164, salt))

    companion object {
        private const val SALT_FILE = "antifraud.hash.salt"
        private const val SALT_BYTES = 16
        private const val DEFAULT_COUNTRY_CODE = 7

        fun create(context: Context): CallEntryDigest = create(context, KeyStoreCryptoBox.create(context, alias = "antifraud.field_box"))

        fun create(
            context: Context,
            box: CryptoBox,
        ): CallEntryDigest =
            CallEntryDigest(
                phone = PhoneNormalizer(defaultCountryCode = DEFAULT_COUNTRY_CODE),
                salt = obtainSalt(context, box),
            )

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
