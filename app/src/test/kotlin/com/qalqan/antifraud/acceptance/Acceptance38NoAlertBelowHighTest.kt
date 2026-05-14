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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Acceptance38NoAlertBelowHighTest {
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
    fun `§23 #38 - low risk posts no notification`() =
        runTest {
            val pipeline = buildPipeline(forcedScore = LOW_SCORE)
            pipeline.onCallCaptured(sampleCall())
            shadowOf(nm).allNotifications.shouldBeEmpty()
        }

    @Test
    fun `§23 #38 - medium risk posts a quiet medium-channel notification (not full-screen)`() =
        runTest {
            AlertChannels.ensure(ctx)
            val pipeline = buildPipeline(forcedScore = MEDIUM_SCORE)
            pipeline.onCallCaptured(sampleCall())
            val posted =
                shadowOf(nm).allNotifications.singleOrNull()
                    ?: error("expected exactly one medium-channel notification")
            posted.channelId shouldBe AlertChannels.CHANNEL_MEDIUM
            (posted.fullScreenIntent == null) shouldBe true
        }

    private fun buildPipeline(forcedScore: Int): AlertPipeline =
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
            forcedScore = forcedScore,
        )

    private fun sampleCall(): CallEvent =
        CallEvent(
            id = EventId("e1"),
            phoneHash = PhoneHash("h1"),
            simSlot = 0,
            direction = CallDirection.INCOMING,
            startedAt = Instant.parse("2026-05-14T12:00:00Z"),
            endedAt = Instant.parse("2026-05-14T12:01:00Z"),
            durationSec = SHORT_CALL_DURATION,
            isKnownContact = true,
            isRepeated = false,
            callRiskScore = LOW_SCORE,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private companion object {
        const val LOW_SCORE = 10
        const val MEDIUM_SCORE = 45
        const val SHORT_CALL_DURATION = 60L
    }
}
