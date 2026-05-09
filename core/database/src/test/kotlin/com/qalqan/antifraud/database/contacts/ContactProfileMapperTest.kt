package com.qalqan.antifraud.database.contacts

import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.TrustStatus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class ContactProfileMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves data`() {
        val original =
            ContactProfile(
                id = "c1",
                phoneNormalizedEnc = byteArrayOf(1, 2, 3),
                phoneHash = PhoneHash("h"),
                phoneLast4 = "1234",
                isShortCode = false,
                displayNameLocal = "Mom",
                isInContacts = true,
                trustStatus = TrustStatus.TRUSTED,
                firstSeenAt = t,
                lastSeenAt = t,
                riskCounter = 3,
                userComment = "verified",
            )
        val entity = original.toEntity()
        val back = entity.toDomain()
        back shouldBe original
    }
}
