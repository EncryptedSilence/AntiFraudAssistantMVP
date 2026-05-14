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
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
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
class AlertLatencyTest {
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
    fun `critical-call latency below 2 s (spec §4_4_2 + §23 #35)`() =
        runTest {
            val p = pipeline(forcedScore = CRITICAL_SCORE)
            val ms = measureTimeMillis { p.onCallCaptured(criticalCall()) }
            ms shouldBeLessThan TWO_SECONDS_MS
        }

    @Test
    fun `high latency below 5 s (spec §4_4_2 + §23 #37)`() =
        runTest {
            val p = pipeline(forcedScore = HIGH_SCORE)
            val ms = measureTimeMillis { p.onCallCaptured(criticalCall()) }
            ms shouldBeLessThan FIVE_SECONDS_MS
        }

    @Test
    fun `medium latency below 10 s (spec §4_4_2)`() =
        runTest {
            val p = pipeline(forcedScore = MEDIUM_SCORE)
            val ms = measureTimeMillis { p.onCallCaptured(criticalCall()) }
            ms shouldBeLessThan TEN_SECONDS_MS
        }

    @Test
    fun `low latency below 1 s (silent path is short-circuit)`() =
        runTest {
            val p = pipeline(forcedScore = LOW_SCORE)
            val ms = measureTimeMillis { p.onCallCaptured(criticalCall()) }
            ms shouldBeLessThan ONE_SECOND_MS
        }

    private fun criticalCall(): CallEvent =
        CallEvent(
            id = EventId("e1"),
            phoneHash = PhoneHash("h1"),
            simSlot = 0,
            direction = CallDirection.INCOMING,
            startedAt = Instant.parse("2026-05-14T12:00:00Z"),
            endedAt = Instant.parse("2026-05-14T12:02:00Z"),
            durationSec = LONG_CALL_DURATION,
            isKnownContact = false,
            isRepeated = false,
            callRiskScore = CRITICAL_SCORE,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private companion object {
        const val CRITICAL_SCORE = 85
        const val HIGH_SCORE = 70
        const val MEDIUM_SCORE = 45
        const val LOW_SCORE = 10
        const val LONG_CALL_DURATION = 120L
        const val ONE_SECOND_MS = 1_000L
        const val TWO_SECONDS_MS = 2_000L
        const val FIVE_SECONDS_MS = 5_000L
        const val TEN_SECONDS_MS = 10_000L
    }
}
