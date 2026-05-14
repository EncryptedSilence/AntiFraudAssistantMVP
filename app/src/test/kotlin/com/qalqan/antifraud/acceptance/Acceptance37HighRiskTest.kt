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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Acceptance37HighRiskTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val repos by lazy { Repositories.inMemory(ctx) }
    private val nm get() = ctx.getSystemService(android.app.NotificationManager::class.java)

    @After
    fun clear() {
        CampaignCooldown.clearAllForTest()
        nm.cancelAll()
        repos.close()
    }

    @Test
    fun `§23 #37 - high risk produces a full-screen-intent notification under 5 s on CHANNEL_CRITICAL`() =
        runTest {
            AlertChannels.ensure(ctx)
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher =
                        AlertDispatcher(
                            context = ctx,
                            builder = AlertNotificationBuilder(),
                            overlayLauncher = {},
                            actionLogger = {},
                        ),
                    explanationProvider = AlertExplanationProvider(),
                    forcedScore = HIGH_SCORE,
                )
            val ms = measureTimeMillis { pipeline.onCallCaptured(highRiskCall()) }
            ms shouldBeLessThan FIVE_SECONDS_MS
            val posted =
                shadowOf(nm).allNotifications.singleOrNull()
                    ?: error("expected exactly one notification")
            posted.channelId shouldBe AlertChannels.CHANNEL_CRITICAL
            posted.fullScreenIntent shouldNotBe null
        }

    private fun highRiskCall(): CallEvent =
        CallEvent(
            id = EventId("e1"),
            phoneHash = PhoneHash("h1"),
            simSlot = 0,
            direction = CallDirection.INCOMING,
            startedAt = Instant.parse("2026-05-14T12:00:00Z"),
            endedAt = Instant.parse("2026-05-14T12:01:30Z"),
            durationSec = LONG_CALL_DURATION,
            isKnownContact = false,
            isRepeated = false,
            callRiskScore = HIGH_SCORE,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private companion object {
        const val HIGH_SCORE = 70
        const val LONG_CALL_DURATION = 90L
        const val FIVE_SECONDS_MS = 5_000L
    }
}
