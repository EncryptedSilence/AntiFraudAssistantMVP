package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.alerts.AlertBand
import com.qalqan.antifraud.alerts.AlertChannels
import com.qalqan.antifraud.alerts.AlertContent
import com.qalqan.antifraud.alerts.AlertDispatcher
import com.qalqan.antifraud.alerts.AlertNotificationBuilder
import com.qalqan.antifraud.alerts.CampaignCooldown
import com.qalqan.antifraud.alerts.OverlayGate
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Acceptance39OverlayGatingTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val nm get() = ctx.getSystemService(android.app.NotificationManager::class.java)
    private val content =
        AlertContent(
            reasons =
                listOf(
                    "Caller is not in your contacts.",
                    "Risk level reached critical.",
                    "Matched pattern: lookalike domain",
                ),
        )

    @After
    fun clear() {
        nm.cancelAll()
        CampaignCooldown.clearAllForTest()
    }

    @Test
    fun `§23 #39 permission denied + critical + relevant FG yields no overlay, notification fires`() {
        AlertChannels.ensure(ctx)
        var overlayFired = 0
        val dispatcher = newDispatcher { overlayFired++ }
        val shouldFire =
            OverlayGate.shouldFire(
                canDrawOverlays = false,
                band = AlertBand.FULL_SCREEN_PLUS_OVERLAY,
                foregroundIsRelevant = true,
            )
        dispatcher.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c1",
            overlayShouldFire = shouldFire,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBe 0
    }

    @Test
    fun `§23 #39 permission granted + high + relevant FG yields no overlay`() {
        AlertChannels.ensure(ctx)
        var overlayFired = 0
        val dispatcher = newDispatcher { overlayFired++ }
        val shouldFire =
            OverlayGate.shouldFire(
                canDrawOverlays = true,
                band = AlertBand.FULL_SCREEN,
                foregroundIsRelevant = true,
            )
        dispatcher.dispatch(
            content,
            AlertBand.FULL_SCREEN,
            campaignId = "c2",
            overlayShouldFire = shouldFire,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBe 0
    }

    @Test
    fun `§23 #39 permission granted + critical + irrelevant FG yields no overlay`() {
        AlertChannels.ensure(ctx)
        var overlayFired = 0
        val dispatcher = newDispatcher { overlayFired++ }
        val shouldFire =
            OverlayGate.shouldFire(
                canDrawOverlays = true,
                band = AlertBand.FULL_SCREEN_PLUS_OVERLAY,
                foregroundIsRelevant = false,
            )
        dispatcher.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c3",
            overlayShouldFire = shouldFire,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBe 0
    }

    @Test
    fun `§23 #39 all three conditions met fires overlay`() {
        AlertChannels.ensure(ctx)
        var overlayFired = 0
        val dispatcher = newDispatcher { overlayFired++ }
        val shouldFire =
            OverlayGate.shouldFire(
                canDrawOverlays = true,
                band = AlertBand.FULL_SCREEN_PLUS_OVERLAY,
                foregroundIsRelevant = true,
            )
        dispatcher.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c4",
            overlayShouldFire = shouldFire,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBe 1
    }

    @Test
    fun `§23 #39 permission denied + critical does not crash the dispatcher`() {
        AlertChannels.ensure(ctx)
        val dispatcher = newDispatcher { }
        dispatcher.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c5",
            overlayShouldFire = false,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
    }

    private fun newDispatcher(onOverlay: () -> Unit): AlertDispatcher =
        AlertDispatcher(
            context = ctx,
            builder = AlertNotificationBuilder(),
            overlayLauncher = { onOverlay() },
            actionLogger = {},
        )
}
