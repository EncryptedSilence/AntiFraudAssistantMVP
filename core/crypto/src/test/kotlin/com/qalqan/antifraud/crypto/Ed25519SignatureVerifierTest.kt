package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import org.junit.Test

class Ed25519SignatureVerifierTest {
    private val verifier = Ed25519SignatureVerifier()
    private val publicKey = TestKeys.hexToBytes(TestKeys.TEST_PUBLIC_KEY_HEX)
    private val helloBytes = "hello".toByteArray(Charsets.US_ASCII)
    private val helloSig = TestKeys.hexToBytes(TestKeys.TEST_SIGNATURE_FOR_HELLO_HEX)

    @Test
    fun `valid signature with correct key and message verifies`() {
        verifier.verify(message = helloBytes, signature = helloSig, publicKey = publicKey) shouldBe true
    }

    @Test
    fun `tampered message fails verification`() {
        verifier.verify(
            message = "HELLO".toByteArray(Charsets.US_ASCII),
            signature = helloSig,
            publicKey = publicKey,
        ) shouldBe false
    }

    @Test
    fun `tampered signature fails verification`() {
        val tampered = helloSig.copyOf().also { it[0] = (it[0].toInt() xor 0x01).toByte() }
        verifier.verify(message = helloBytes, signature = tampered, publicKey = publicKey) shouldBe false
    }

    @Test
    fun `wrong public key fails verification`() {
        val wrong = ByteArray(32) { i -> i.toByte() }
        verifier.verify(message = helloBytes, signature = helloSig, publicKey = wrong) shouldBe false
    }

    @Test
    fun `wrong-size public key returns false (does not throw)`() {
        verifier.verify(message = helloBytes, signature = helloSig, publicKey = ByteArray(31)) shouldBe false
    }

    @Test
    fun `wrong-size signature returns false (does not throw)`() {
        verifier.verify(message = helloBytes, signature = ByteArray(63), publicKey = publicKey) shouldBe false
    }
}
