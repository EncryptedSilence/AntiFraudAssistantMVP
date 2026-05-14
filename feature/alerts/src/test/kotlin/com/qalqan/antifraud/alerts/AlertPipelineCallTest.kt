package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
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
class AlertPipelineCallTest {
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
    fun `LOW risk call yields SILENT band and posts nothing`() =
        runTest {
            val recorded = mutableListOf<AlertBand>()
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = recordingDispatcher(recorded),
                    explanationProvider = AlertExplanationProvider(),
                    forcedScore = LOW_SCORE,
                )
            pipeline.onCallCaptured(lowRiskCall())
            recorded shouldBe listOf(AlertBand.SILENT)
        }

    @Test
    fun `CRITICAL risk call yields FULL_SCREEN_PLUS_OVERLAY band`() =
        runTest {
            val recorded = mutableListOf<AlertBand>()
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = recordingDispatcher(recorded),
                    explanationProvider = AlertExplanationProvider(),
                    forcedScore = CRITICAL_SCORE,
                )
            pipeline.onCallCaptured(criticalRiskCall("e2"))
            recorded shouldBe listOf(AlertBand.FULL_SCREEN_PLUS_OVERLAY)
        }

    @Test
    fun `cooldown blocks the third critical alert in 24 h`() =
        runTest {
            val recorded = mutableListOf<AlertBand>()
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = recordingDispatcher(recorded),
                    explanationProvider = AlertExplanationProvider(),
                    forcedScore = CRITICAL_SCORE,
                )
            pipeline.onCallCaptured(criticalRiskCall("e3"))
            pipeline.onCallCaptured(criticalRiskCall("e3"))
            pipeline.onCallCaptured(criticalRiskCall("e3"))
            recorded shouldBe
                listOf(
                    AlertBand.FULL_SCREEN_PLUS_OVERLAY,
                    AlertBand.FULL_SCREEN_PLUS_OVERLAY,
                )
        }

    private fun lowRiskCall(): CallEvent =
        CallEvent(
            id = EventId("e1"),
            phoneHash = PhoneHash("h1"),
            simSlot = 0,
            direction = CallDirection.INCOMING,
            startedAt = Instant.parse("2026-05-14T12:00:00Z"),
            endedAt = Instant.parse("2026-05-14T12:00:10Z"),
            durationSec = 10L,
            isKnownContact = true,
            isRepeated = false,
            callRiskScore = LOW_SCORE,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private fun criticalRiskCall(id: String): CallEvent =
        lowRiskCall().copy(
            id = EventId(id),
            phoneHash = PhoneHash("h2"),
            isKnownContact = false,
            durationSec = LONG_CALL_DURATION,
            callRiskScore = CRITICAL_SCORE,
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
        const val LONG_CALL_DURATION = 120L
    }
}
