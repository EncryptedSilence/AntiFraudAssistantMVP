package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.domain.WebEvent
import com.qalqan.antifraud.scoring.LinkSignal
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class LinkSignalResolverTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `same number across two calls yields SAME_NUMBER`() {
        val a = call("h1", t)
        val b = call("h1", t.plusSeconds(60))
        LinkSignalResolver.resolve(RiskEvent.Call(a), RiskEvent.Call(b)) shouldContainExactlyInAnyOrder
            listOf(LinkSignal.SAME_NUMBER)
    }

    @Test fun `SMS within 24h after a call yields SMS_AFTER_CALL`() {
        val callA = call("h1", t)
        val smsB = sms("s1", t.plusSeconds(120))
        LinkSignalResolver.resolve(RiskEvent.Call(callA), RiskEvent.Sms(smsB)) shouldContainExactlyInAnyOrder
            listOf(LinkSignal.SMS_AFTER_CALL)
    }

    @Test fun `call within 24h after an SMS yields CALL_AFTER_SMS`() {
        val smsA = sms("s1", t)
        val callB = call("h1", t.plusSeconds(120))
        LinkSignalResolver.resolve(RiskEvent.Sms(smsA), RiskEvent.Call(callB)) shouldContainExactlyInAnyOrder
            listOf(LinkSignal.CALL_AFTER_SMS)
    }

    @Test fun `web visit after either call or SMS yields SITE_AFTER_CALL_OR_SMS`() {
        val callA = call("h1", t)
        val webB = web("d1", t.plusSeconds(60))
        LinkSignalResolver.resolve(RiskEvent.Call(callA), RiskEvent.Web(webB)) shouldContainExactlyInAnyOrder
            listOf(LinkSignal.SITE_AFTER_CALL_OR_SMS)
    }

    @Test fun `events more than 24h apart yield only weak`() {
        val callA = call("h1", t)
        val smsB = sms("s1", t.plusSeconds(60 * 60 * 25))
        LinkSignalResolver.resolve(RiskEvent.Call(callA), RiskEvent.Sms(smsB)) shouldBe listOf(LinkSignal.WEAK)
    }

    private fun call(
        hash: String,
        at: Instant,
    ) = CallEvent(
        id = EventId("c-$hash-$at"),
        phoneHash = PhoneHash(hash),
        simSlot = null,
        direction = CallDirection.INCOMING,
        startedAt = at,
        endedAt = at.plusSeconds(60),
        durationSec = 60,
        isKnownContact = false,
        isRepeated = false,
        callRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private fun sms(
        hash: String,
        at: Instant,
    ) = SmsEvent(
        id = EventId("s-$hash-$at"),
        senderHash = SenderHash(hash),
        senderDisplayNameLocal = null,
        simSlot = null,
        receivedAt = at,
        smsCategory = SmsCategory.UNKNOWN_SENDER,
        containsCode = false,
        containsLink = false,
        containsFinancialKeyword = false,
        containsSecurityKeyword = false,
        bodyExcerptEnc = byteArrayOf(),
        smsRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private fun web(
        hash: String,
        at: Instant,
    ) = WebEvent(
        id = EventId("w-$hash-$at"),
        domainHash = DomainHash(hash),
        domainDisplayLocal = "halykbank.kz",
        visitedAt = at,
        isNewDomain = true,
        domainStatus = DomainStatus.NEW,
        webRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )
}
