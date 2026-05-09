package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.scoring.LinkSignal
import io.kotest.matchers.collections.shouldContain
import org.junit.jupiter.api.Test
import java.time.Instant

class OutOfOrderTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `link signals are symmetric — SMS-then-call yields CALL_AFTER_SMS regardless of resolve order`() {
        val sms =
            RiskEvent.Sms(
                SmsEvent(
                    id = EventId("s1"),
                    senderHash = SenderHash("h"),
                    senderDisplayNameLocal = null,
                    simSlot = null,
                    receivedAt = t,
                    smsCategory = SmsCategory.UNKNOWN_SENDER,
                    containsCode = false,
                    containsLink = false,
                    containsFinancialKeyword = false,
                    containsSecurityKeyword = false,
                    bodyExcerptEnc = byteArrayOf(),
                    smsRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
        val call =
            RiskEvent.Call(
                CallEvent(
                    id = EventId("c1"),
                    phoneHash = PhoneHash("h"),
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = t.plusSeconds(120),
                    endedAt = t.plusSeconds(180),
                    durationSec = 60,
                    isKnownContact = false,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )

        LinkSignalResolver.resolve(sms, call) shouldContain LinkSignal.CALL_AFTER_SMS
        LinkSignalResolver.resolve(call, sms) shouldContain LinkSignal.CALL_AFTER_SMS
    }
}
