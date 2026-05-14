package com.qalqan.antifraud.alerts

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class OverlayBannerActivityTest {
    @After
    fun clearCooldown() {
        DismissalCooldown.clearAllForTest()
    }

    private val sampleReasons =
        arrayOf(
            "Caller is not in your contacts.",
            "Matched pattern: lookalike domain",
            "Risk level reached critical.",
        )

    @Test
    fun `auto-dismisses after 30 seconds and records cooldown`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent =
            Intent(ctx, OverlayBannerActivity::class.java).apply {
                putExtra(OverlayBannerActivity.EXTRA_REASONS, sampleReasons)
                putExtra(OverlayBannerActivity.EXTRA_CAMPAIGN_ID, "camp-99")
            }
        val activity = Robolectric.buildActivity(OverlayBannerActivity::class.java, intent).setup().get()

        shadowOf(Looper.getMainLooper()).idleFor(AUTO_DISMISS_PLUS_BUFFER_MS, TimeUnit.MILLISECONDS)

        DismissalCooldown.isCoolingDown("camp-99") shouldBe true
        activity.isFinishing shouldBe true
    }

    @Test
    fun `Dismiss before auto-dismiss still records cooldown and finishes`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent =
            Intent(ctx, OverlayBannerActivity::class.java).apply {
                putExtra(OverlayBannerActivity.EXTRA_REASONS, sampleReasons)
                putExtra(OverlayBannerActivity.EXTRA_CAMPAIGN_ID, "camp-100")
            }
        val activity = Robolectric.buildActivity(OverlayBannerActivity::class.java, intent).setup().get()

        val m = OverlayBannerActivity::class.java.getDeclaredMethod("onDismiss", String::class.java)
        m.isAccessible = true
        m.invoke(activity, "camp-100")

        DismissalCooldown.isCoolingDown("camp-100") shouldBe true
        activity.isFinishing shouldBe true
    }

    private companion object {
        const val AUTO_DISMISS_PLUS_BUFFER_MS = 30_001L
    }
}
