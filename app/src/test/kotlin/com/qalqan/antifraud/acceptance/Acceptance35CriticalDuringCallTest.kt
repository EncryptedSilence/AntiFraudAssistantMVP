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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeLessThan
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
class Acceptance35CriticalDuringCallTest {
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
    fun `§23 #35 - critical-during-call produces a full-screen alert under 2 s`() =
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
                    forcedScore = CRITICAL_SCORE,
                )
            val ms = measureTimeMillis { pipeline.onCallCaptured(criticalCall()) }
            ms shouldBeLessThan TWO_SECONDS_MS
            shadowOf(nm).allNotifications shouldHaveSize 1
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
        const val LONG_CALL_DURATION = 120L
        const val TWO_SECONDS_MS = 2_000L
    }
}
