package com.qalqan.antifraud.database.crypto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InMemoryCryptoBoxTest {
    @Test
    fun `encrypt then decrypt round-trips a short blob`() {
        val box = InMemoryCryptoBox()
        val plaintext = "hello".toByteArray()
        val ciphertext = box.encrypt(plaintext)
        box.decrypt(ciphertext).contentEquals(plaintext) shouldBe true
    }

    @Test
    fun `encrypt produces distinct ciphertexts for the same plaintext (IV randomness)`() {
        val box = InMemoryCryptoBox()
        val a = box.encrypt("same".toByteArray())
        val b = box.encrypt("same".toByteArray())
        a.contentEquals(b) shouldBe false
    }

    @Test
    fun `tampered ciphertext fails decryption`() {
        val box = InMemoryCryptoBox()
        val ct = box.encrypt("payload".toByteArray())
        ct[ct.size - 1] = (ct[ct.size - 1].toInt() xor 0x01).toByte()
        assertThrows<javax.crypto.AEADBadTagException> { box.decrypt(ct) }
    }

    @Test
    fun `envelope of empty plaintext is exactly IV plus GCM tag`() {
        val box = InMemoryCryptoBox()
        val out = box.encrypt(byteArrayOf())
        out.size shouldBe IV_BYTES + GCM_TAG_BYTES
    }

    private companion object {
        const val IV_BYTES = 12
        const val GCM_TAG_BYTES = 16
    }
}
