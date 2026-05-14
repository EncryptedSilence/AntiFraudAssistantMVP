package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlertPipelineSmsTest {
    private val repos by lazy {
        Repositories.inMemory(ApplicationProvider.getApplicationContext<Context>())
    }

    @After
    fun clear() {
        CampaignCooldown.clearAllForTest()
        DismissalCooldown.clearAllForTest()
        repos.close()
    }

    @Test
    fun `LOW risk sms yields SILENT band`() =
        runTest {
            val recorded = mutableListOf<AlertBand>()
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = recordingDispatcher(recorded),
                    explanationProvider = AlertExplanationProvider(),
                    forcedScore = LOW_SCORE,
                )
            pipeline.onSmsCaptured(lowRiskSms())
            recorded shouldBe listOf(AlertBand.SILENT)
        }

    @Test
    fun `CRITICAL risk sms yields FULL_SCREEN_PLUS_OVERLAY band`() =
        runTest {
            val recorded = mutableListOf<AlertBand>()
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = recordingDispatcher(recorded),
                    explanationProvider = AlertExplanationProvider(),
                    forcedScore = CRITICAL_SCORE,
                )
            pipeline.onSmsCaptured(criticalRiskSms("s1"))
            recorded shouldBe listOf(AlertBand.FULL_SCREEN_PLUS_OVERLAY)
        }

    private fun lowRiskSms(): SmsEvent =
        SmsEvent(
            id = EventId("s0"),
            senderHash = SenderHash("h0"),
            senderDisplayNameLocal = null,
            simSlot = 0,
            receivedAt = Instant.parse("2026-05-14T12:00:00Z"),
            smsCategory = SmsCategory.UNKNOWN_SENDER,
            containsCode = false,
            containsLink = false,
            containsFinancialKeyword = false,
            containsSecurityKeyword = false,
            bodyExcerptEnc = ByteArray(0),
            smsRiskScore = LOW_SCORE,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private fun criticalRiskSms(id: String): SmsEvent =
        lowRiskSms().copy(
            id = EventId(id),
            senderHash = SenderHash("h1"),
            containsCode = true,
            containsLink = true,
            containsFinancialKeyword = true,
            smsRiskScore = CRITICAL_SCORE,
        )

    private fun recordingDispatcher(into: MutableList<AlertBand>): AlertDispatcher =
        object : AlertDispatcher(
            context = ApplicationProvider.getApplicationContext(),
            builder = AlertNotificationBuilder(),
            overlayLauncher = {},
            actionLogger = {},
        ) {
            override fun dispatch(
                content: AlertContent,
                band: AlertBand,
                campaignId: String,
                overlayShouldFire: Boolean,
            ) {
                into += band
            }
        }

    private companion object {
        const val LOW_SCORE = 10
        const val CRITICAL_SCORE = 85
    }
}
