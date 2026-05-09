package com.qalqan.antifraud.database.manual

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class HashingTest {
    @Test fun `same input plus salt is deterministic`() {
        val salt = ByteArray(16) { 1 }
        val a = Hashing.saltedSha256("+77001234567", salt)
        val b = Hashing.saltedSha256("+77001234567", salt)
        a shouldBe b
    }

    @Test fun `different inputs give different hashes`() {
        val salt = ByteArray(16) { 1 }
        Hashing.saltedSha256("+77001234567", salt) shouldNotBe Hashing.saltedSha256("+77000000000", salt)
    }

    @Test fun `different salts give different hashes for same input`() {
        val a = Hashing.saltedSha256("+77001234567", ByteArray(16) { 1 })
        val b = Hashing.saltedSha256("+77001234567", ByteArray(16) { 2 })
        a shouldNotBe b
    }
}
