package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SmsEventFieldsTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")
    private val sms = RiskEvent.Sms(
        SmsEvent(
            id = EventId("s1"),
            senderHash = SenderHash("h"),
            senderDisplayNameLocal = null,
            simSlot = null,
            receivedAt = t,
            smsCategory = SmsCategory.OTP,
            containsCode = true,
            containsLink = true,
            containsFinancialKeyword = false,
            containsSecurityKeyword = true,
            bodyExcerptEnc = byteArrayOf(),
            smsRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null
        )
    )

    @Test fun `containsCode`() { SmsEventFields.lookup(sms, "containsCode") shouldBe true }
    @Test fun `containsLink`() { SmsEventFields.lookup(sms, "containsLink") shouldBe true }
    @Test fun `containsFinancialKeyword`() { SmsEventFields.lookup(sms, "containsFinancialKeyword") shouldBe false }
    @Test fun `containsSecurityKeyword`() { SmsEventFields.lookup(sms, "containsSecurityKeyword") shouldBe true }
    @Test fun `smsCategory as String`() { SmsEventFields.lookup(sms, "smsCategory") shouldBe "OTP" }
    @Test fun `unknown field returns null`() { SmsEventFields.lookup(sms, "ghost") shouldBe null }
}
