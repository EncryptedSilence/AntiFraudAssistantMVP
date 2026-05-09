package com.qalqan.antifraud.database.sms

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SmsEventMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves data including encrypted body excerpt`() {
        val body = byteArrayOf(0x01, 0x02, 0x03)
        val sms =
            SmsEvent(
                id = EventId("s1"),
                senderHash = SenderHash("h"),
                senderDisplayNameLocal = "HALYK",
                simSlot = 1,
                receivedAt = t,
                smsCategory = SmsCategory.OTP,
                containsCode = true,
                containsLink = false,
                containsFinancialKeyword = true,
                containsSecurityKeyword = false,
                bodyExcerptEnc = body,
                smsRiskScore = SCORE,
                linkedSessionId = null,
                linkedCampaignId = null,
            )
        sms.toEntity().toDomain() shouldBe sms
    }

    private companion object {
        const val SCORE: Int = 60
    }
}
