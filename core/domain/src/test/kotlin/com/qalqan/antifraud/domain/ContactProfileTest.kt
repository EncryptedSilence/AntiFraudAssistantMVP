package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class ContactProfileTest {
    private val baseTime = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `requires phoneLast4 be exactly 4 digits when present`() {
        shouldThrow<IllegalArgumentException> {
            ContactProfile(
                id = "c1",
                phoneNormalizedEnc = "AES:placeholder".toByteArray(),
                phoneHash = PhoneHash("hash"),
                phoneLast4 = "12",
                isShortCode = false,
                displayNameLocal = null,
                isInContacts = false,
                trustStatus = TrustStatus.UNKNOWN,
                firstSeenAt = baseTime,
                lastSeenAt = baseTime,
                riskCounter = 0,
                userComment = null,
            )
        }
    }

    @Test
    fun `riskCounter cannot be negative`() {
        shouldThrow<IllegalArgumentException> {
            ContactProfile(
                id = "c1",
                phoneNormalizedEnc = byteArrayOf(),
                phoneHash = PhoneHash("hash"),
                phoneLast4 = "1234",
                isShortCode = false,
                displayNameLocal = null,
                isInContacts = false,
                trustStatus = TrustStatus.UNKNOWN,
                firstSeenAt = baseTime,
                lastSeenAt = baseTime,
                riskCounter = -1,
                userComment = null,
            )
        }
    }

    @Test
    fun `lastSeenAt must be on or after firstSeenAt`() {
        shouldThrow<IllegalArgumentException> {
            ContactProfile(
                id = "c1",
                phoneNormalizedEnc = byteArrayOf(),
                phoneHash = PhoneHash("hash"),
                phoneLast4 = null,
                isShortCode = false,
                displayNameLocal = null,
                isInContacts = false,
                trustStatus = TrustStatus.UNKNOWN,
                firstSeenAt = baseTime,
                lastSeenAt = baseTime.minusSeconds(1),
                riskCounter = 0,
                userComment = null,
            )
        }
    }

    @Test
    fun `valid contact profile is accepted`() {
        val cp =
            ContactProfile(
                id = "c1",
                phoneNormalizedEnc = byteArrayOf(),
                phoneHash = PhoneHash("hash"),
                phoneLast4 = "1234",
                isShortCode = false,
                displayNameLocal = "Mom",
                isInContacts = true,
                trustStatus = TrustStatus.TRUSTED,
                firstSeenAt = baseTime,
                lastSeenAt = baseTime,
                riskCounter = 0,
                userComment = null,
            )
        cp.trustStatus shouldBe TrustStatus.TRUSTED
    }
}
