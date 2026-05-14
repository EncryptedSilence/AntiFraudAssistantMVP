package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlertDispatcherTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val nm get() = ctx.getSystemService(android.app.NotificationManager::class.java)
    private val dispatcher by lazy {
        AlertDispatcher(
            context = ctx,
            builder = AlertNotificationBuilder(),
            overlayLauncher = { },
            actionLogger = { },
        )
    }
    private val content =
        AlertContent(
            reasons =
                listOf(
                    "Caller is not in your contacts.",
                    "Risk level reached critical.",
                    "Matched pattern: lookalike domain",
                ),
        )

    @Before
    fun ensureChannels() {
        AlertChannels.ensure(ctx)
    }

    @After
    fun clear() {
        nm.cancelAll()
        CampaignCooldown.clearAllForTest()
    }

    @Test
    fun `SILENT band posts nothing`() {
        dispatcher.dispatch(content, AlertBand.SILENT, campaignId = "c1")
        shadowOf(nm).allNotifications.shouldBeEmpty()
    }

    @Test
    fun `REGULAR band posts one notification`() {
        dispatcher.dispatch(content, AlertBand.REGULAR, campaignId = "c1")
        shadowOf(nm).allNotifications shouldHaveSize 1
    }

    @Test
    fun `FULL_SCREEN band posts a critical-channel notification`() {
        dispatcher.dispatch(content, AlertBand.FULL_SCREEN, campaignId = "c1")
        val posted = shadowOf(nm).allNotifications.single()
        posted.channelId shouldBe AlertChannels.CHANNEL_CRITICAL
    }

    @Test
    fun `FULL_SCREEN_PLUS_OVERLAY posts and triggers overlay`() {
        var overlayFired = 0
        val d =
            AlertDispatcher(
                context = ctx,
                builder = AlertNotificationBuilder(),
                overlayLauncher = { overlayFired++ },
                actionLogger = {},
            )
        d.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c1",
            overlayShouldFire = true,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBeGreaterThan 0
    }

    @Test
    fun `overlay does not fire when gate says no`() {
        var overlayFired = 0
        val d =
            AlertDispatcher(
                context = ctx,
                builder = AlertNotificationBuilder(),
                overlayLauncher = { overlayFired++ },
                actionLogger = {},
            )
        d.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c1",
            overlayShouldFire = false,
        )
        shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBe 0
    }
}
