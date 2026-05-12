package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import org.junit.Test

class Sha256Test {
    @Test
    fun `empty input produces the canonical empty-string digest`() {
        // NIST FIPS 180-4 test vector for SHA-256 of the empty string.
        Sha256.hashHex(ByteArray(0)) shouldBe
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }

    @Test
    fun `abc produces the FIPS 180-4 test vector`() {
        // NIST FIPS 180-4 sample appendix: SHA-256("abc").
        Sha256.hashHex("abc".toByteArray(Charsets.US_ASCII)) shouldBe
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    }

    @Test
    fun `hex output is always 64 lowercase characters`() {
        val out = Sha256.hashHex(byteArrayOf(0x01, 0x02, 0x03))
        out.length shouldBe 64
        (out == out.lowercase()) shouldBe true
        out.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
    }
}
