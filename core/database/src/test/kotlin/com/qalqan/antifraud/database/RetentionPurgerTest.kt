package com.qalqan.antifraud.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.calls.CallEventEntity
import com.qalqan.antifraud.database.sms.SmsEventEntity
import com.qalqan.antifraud.database.web.WebEventEntity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class RetentionPurgerTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(ctx)
    private val purger = RetentionPurger(db, RetentionPolicy.DEFAULT)

    @After
    fun close() = db.close()

    @Test
    fun `events older than 30 days are purged across all event tables`() {
        runBlocking {
            val now = Instant.parse("2026-05-08T10:00:00Z")
            val past = now.minus(Duration.ofDays(FORTY)).toEpochMilli()
            val recent = now.minus(Duration.ofDays(ONE)).toEpochMilli()

            db.callEventDao().upsert(callSample("call-old", past))
            db.callEventDao().upsert(callSample("call-fresh", recent))
            db.smsEventDao().upsert(smsSample("sms-old", past))
            db.smsEventDao().upsert(smsSample("sms-fresh", recent))
            db.webEventDao().upsert(webSample("web-old", past))
            db.webEventDao().upsert(webSample("web-fresh", recent))

            purger.purge(now)

            db.callEventDao().findById("call-old") shouldBe null
            db.callEventDao().findById("call-fresh")?.id shouldBe "call-fresh"
            db.smsEventDao().findById("sms-old") shouldBe null
            db.smsEventDao().findById("sms-fresh")?.id shouldBe "sms-fresh"
            db.webEventDao().findById("web-old") shouldBe null
            db.webEventDao().findById("web-fresh")?.id shouldBe "web-fresh"
        }
    }

    private fun callSample(
        id: String,
        atMs: Long,
    ) = CallEventEntity(
        id = id,
        phoneHash = "h",
        simSlot = null,
        direction = "INCOMING",
        startedAtMs = atMs,
        endedAtMs = null,
        durationSec = 0,
        isKnownContact = false,
        isRepeated = false,
        callRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private fun smsSample(
        id: String,
        atMs: Long,
    ) = SmsEventEntity(
        id = id,
        senderHash = "h",
        senderDisplayNameLocal = null,
        simSlot = null,
        receivedAtMs = atMs,
        smsCategory = "UNKNOWN_SENDER",
        containsCode = false,
        containsLink = false,
        containsFinancialKeyword = false,
        containsSecurityKeyword = false,
        bodyExcerptEnc = byteArrayOf(),
        smsRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private fun webSample(
        id: String,
        atMs: Long,
    ) = WebEventEntity(
        id = id,
        domainHash = "h",
        domainDisplayLocal = "example.com",
        visitedAtMs = atMs,
        isNewDomain = false,
        domainStatus = "KNOWN",
        webRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private companion object {
        const val FORTY: Long = 40
        const val ONE: Long = 1
    }
}
