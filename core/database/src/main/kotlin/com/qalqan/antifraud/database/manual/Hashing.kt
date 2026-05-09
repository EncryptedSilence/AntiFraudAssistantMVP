package com.qalqan.antifraud.database.manual

import java.security.MessageDigest

object Hashing {
    fun saltedSha256(
        input: String,
        salt: ByteArray,
    ): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(input.toByteArray(Charsets.UTF_8))
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
