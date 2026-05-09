package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SmsEventTest {
    private val received = Instant.parse("2026-05-08T10:00:00Z")

    private fun sms(
        bodyExcerptEnc: ByteArray = byteArrayOf(),
        riskScore: Int = 0
    ): SmsEvent = SmsEvent(
        id = EventId("e1"),
        senderHash = SenderHash("s"),
        senderDisplayNameLocal = null,
        simSlot = null,
        receivedAt = received,
        smsCategory = SmsCategory.UNKNOWN_SENDER,
        containsCode = false,
        containsLink = false,
        containsFinancialKeyword = false,
        containsSecurityKeyword = false,
        bodyExcerptEnc = bodyExcerptEnc,
        smsRiskScore = riskScore,
        linkedSessionId = null,
        linkedCampaignId = null
    )

    @Test fun `body excerpt cannot exceed 200 encrypted bytes`() {
        // Reasoning: a Keystore-AEAD-encrypted blob of a 200-char UTF-8 body is at most ~256 bytes.
        // We enforce a hard ceiling of 512 to cover GCM tag and IV; longer means storage rules were violated.
        shouldThrow<IllegalArgumentException> { sms(bodyExcerptEnc = ByteArray(513)) }
    }

    @Test fun `risk score is bounded to 0 dot dot 100`() {
        shouldThrow<IllegalArgumentException> { sms(riskScore = -1) }
        shouldThrow<IllegalArgumentException> { sms(riskScore = 101) }
    }

    @Test fun `valid sms is accepted`() {
        sms().smsCategory shouldBe SmsCategory.UNKNOWN_SENDER
    }
}
