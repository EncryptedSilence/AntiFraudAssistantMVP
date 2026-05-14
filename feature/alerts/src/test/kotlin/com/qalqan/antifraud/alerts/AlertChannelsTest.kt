package com.qalqan.antifraud.alerts

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlertChannelsTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val nm: NotificationManager
        get() = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Test
    fun `ensureChannels creates the critical channel with IMPORTANCE_HIGH and CATEGORY_CALL`() {
        AlertChannels.ensure(ctx)
        val critical = nm.getNotificationChannel(AlertChannels.CHANNEL_CRITICAL)
        critical shouldNotBe null
        critical.importance shouldBe NotificationManager.IMPORTANCE_HIGH
        critical.shouldVibrate() shouldBe true
        critical.sound shouldNotBe null
    }

    @Test
    fun `ensureChannels creates the medium channel with IMPORTANCE_DEFAULT`() {
        AlertChannels.ensure(ctx)
        val medium = nm.getNotificationChannel(AlertChannels.CHANNEL_MEDIUM)
        medium shouldNotBe null
        medium.importance shouldBe NotificationManager.IMPORTANCE_DEFAULT
    }

    @Test
    fun `ensureChannels is idempotent`() {
        AlertChannels.ensure(ctx)
        AlertChannels.ensure(ctx)
        nm.notificationChannels.count { it.id == AlertChannels.CHANNEL_CRITICAL } shouldBe 1
    }
}
