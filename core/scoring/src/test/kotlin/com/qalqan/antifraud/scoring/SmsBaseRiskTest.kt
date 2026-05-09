package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SmsBaseRiskTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun sms(
        category: SmsCategory = SmsCategory.UNKNOWN_SENDER,
        containsLink: Boolean = false,
        containsCode: Boolean = false,
        containsFinancialKeyword: Boolean = false,
    ) = SmsEvent(
        id = EventId("s"),
        senderHash = SenderHash("h"),
        senderDisplayNameLocal = null,
        simSlot = null,
        receivedAt = t,
        smsCategory = category,
        containsCode = containsCode,
        containsLink = containsLink,
        containsFinancialKeyword = containsFinancialKeyword,
        containsSecurityKeyword = false,
        bodyExcerptEnc = byteArrayOf(),
        smsRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    @Test fun `unknown sender contributes 10`() {
        SmsBaseRisk.compute(sms()) shouldBe 10
    }

    @Test fun `link adds 20`() {
        SmsBaseRisk.compute(sms(containsLink = true)) shouldBe 30
    }

    @Test fun `OTP adds 30`() {
        SmsBaseRisk.compute(sms(containsCode = true)) shouldBe 40
    }

    @Test fun `bank category adds 20`() {
        SmsBaseRisk.compute(sms(category = SmsCategory.BANK)) shouldBe 20
    }

    @Test fun `authority short code adds 20`() {
        SmsBaseRisk.compute(sms(category = SmsCategory.AUTHORITY_SHORTCODE)) shouldBe 20
    }

    @Test fun `login or registration adds 30`() {
        SmsBaseRisk.compute(sms(category = SmsCategory.LOGIN)) shouldBe 30
        SmsBaseRisk.compute(sms(category = SmsCategory.REGISTRATION)) shouldBe 30
        SmsBaseRisk.compute(sms(category = SmsCategory.PASSWORD_CHANGE)) shouldBe 30
    }
}
