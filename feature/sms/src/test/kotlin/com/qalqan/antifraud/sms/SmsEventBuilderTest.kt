package com.qalqan.antifraud.sms

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.domain.SmsCategory
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class SmsEventBuilderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)
    private val builder = SmsEventBuilder(digest = digest, box = box)

    @Test
    fun `builds SmsEvent from SmsBroadcast with correct fields`() {
        val now = Instant.parse("2026-01-01T12:00:00Z")
        val b =
            SmsBroadcast(
                rawSender = "1414",
                body = "Hello citizen, code 123456",
                receivedAt = now,
                simSlot = 0,
            )
        val ev = builder.build(b)
        ev.senderHash.value shouldHaveLength 64 // SHA-256 hex
        ev.senderDisplayNameLocal shouldBe "1414"
        ev.simSlot shouldBe 0
        ev.receivedAt shouldBe now
        // 1414 is an authority short code; authority wins over OTP per classifier priority
        ev.smsCategory shouldBe SmsCategory.AUTHORITY_SHORTCODE
        ev.containsCode shouldBe true
        ev.containsLink shouldBe false
        ev.smsRiskScore shouldBe 0 // orchestrator recomputes on insert
        ev.linkedSessionId shouldBe null
        ev.linkedCampaignId shouldBe null
    }

    @Test
    fun `body excerpt is truncated to MAX_BODY_EXCERPT_CHARS before encryption`() {
        val longBody = "a".repeat(500)
        val b = SmsBroadcast("S", longBody, Instant.now(), null)
        val ev = builder.build(b)
        // The cipher carries the plaintext length plus AEAD overhead; we cannot assert exact
        // plaintext length without decrypting, but the spec cap is on cipher byte size:
        ev.bodyExcerptEnc.size shouldBeLessThanOrEqual 512
    }

    @Test
    fun `senderDisplayNameLocal is trimmed and capped at 80 chars`() {
        val longSender = "X".repeat(200)
        val ev = builder.build(SmsBroadcast(longSender, "body", Instant.now(), null))
        ev.senderDisplayNameLocal?.length shouldBe 80
    }

    @Test
    fun `null simSlot propagates`() {
        val ev = builder.build(SmsBroadcast("S", "body", Instant.now(), null))
        ev.simSlot shouldBe null
    }

    @Test
    fun `containsFinancialKeyword and containsSecurityKeyword flags are set`() {
        val ev1 = builder.build(SmsBroadcast("Bank", "Перевод KZT", Instant.now(), null))
        ev1.containsFinancialKeyword shouldBe true
        val ev2 = builder.build(SmsBroadcast("X", "Suspicious sign-in", Instant.now(), null))
        ev2.containsSecurityKeyword shouldBe true
    }
}
