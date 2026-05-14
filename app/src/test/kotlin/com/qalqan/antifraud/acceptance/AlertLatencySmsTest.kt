package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.alerts.AlertChannels
import com.qalqan.antifraud.alerts.AlertDispatcher
import com.qalqan.antifraud.alerts.AlertExplanationProvider
import com.qalqan.antifraud.alerts.AlertNotificationBuilder
import com.qalqan.antifraud.alerts.AlertPipeline
import com.qalqan.antifraud.alerts.CampaignCooldown
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.longs.shouldBeLessThan
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlertLatencySmsTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val repos by lazy { Repositories.inMemory(ctx) }

    @After
    fun clear() {
        CampaignCooldown.clearAllForTest()
        repos.close()
    }

    private fun pipeline(forcedScore: Int): AlertPipeline {
        AlertChannels.ensure(ctx)
        val dispatcher =
            AlertDispatcher(
                context = ctx,
                builder = AlertNotificationBuilder(),
                overlayLauncher = {},
                actionLogger = {},
            )
        return AlertPipeline(
            repos = repos,
            dispatcher = dispatcher,
            explanationProvider = AlertExplanationProvider(),
            forcedScore = forcedScore,
        )
    }

    @Test
    fun `critical-sms latency below 3 s (spec §4_4_2 + §23 #36)`() =
        runTest {
            val p = pipeline(forcedScore = CRITICAL_SCORE)
            val ms = measureTimeMillis { p.onSmsCaptured(criticalSms()) }
            ms shouldBeLessThan THREE_SECONDS_MS
        }

    @Test
    fun `high-sms latency below 5 s (spec §4_4_2 + §23 #37)`() =
        runTest {
            val p = pipeline(forcedScore = HIGH_SCORE)
            val ms = measureTimeMillis { p.onSmsCaptured(criticalSms()) }
            ms shouldBeLessThan FIVE_SECONDS_MS
        }

    private fun criticalSms(): SmsEvent =
        SmsEvent(
            id = EventId("s1"),
            senderHash = SenderHash("h1"),
            senderDisplayNameLocal = null,
            simSlot = 0,
            receivedAt = Instant.parse("2026-05-14T12:00:00Z"),
            smsCategory = SmsCategory.UNKNOWN_SENDER,
            containsCode = true,
            containsLink = true,
            containsFinancialKeyword = true,
            containsSecurityKeyword = false,
            bodyExcerptEnc = ByteArray(0),
            smsRiskScore = CRITICAL_SCORE,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private companion object {
        const val CRITICAL_SCORE = 85
        const val HIGH_SCORE = 70
        const val THREE_SECONDS_MS = 3_000L
        const val FIVE_SECONDS_MS = 5_000L
    }
}
