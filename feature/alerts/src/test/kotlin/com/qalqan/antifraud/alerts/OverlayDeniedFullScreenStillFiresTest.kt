package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OverlayDeniedFullScreenStillFiresTest {
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
    fun `with overlay permission denied, full-screen notification still fires (§4_4_3)`() {
        AlertChannels.ensure(ctx)
        var overlayFired = 0
        val dispatcher =
            AlertDispatcher(
                context = ctx,
                builder = AlertNotificationBuilder(),
                overlayLauncher = { overlayFired++ },
                actionLogger = {},
            )
        // simulate the gate result the pipeline would compute when permission is denied
        dispatcher.dispatch(
            content,
            AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            campaignId = "c1",
            overlayShouldFire = false,
        )
        org.robolectric.Shadows.shadowOf(nm).allNotifications shouldHaveSize 1
        overlayFired shouldBe 0
    }
}
