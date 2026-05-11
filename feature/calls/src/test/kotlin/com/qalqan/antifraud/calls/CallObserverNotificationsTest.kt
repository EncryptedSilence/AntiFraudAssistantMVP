package com.qalqan.antifraud.calls

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallObserverNotificationsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `channel is created with low importance and no sound`() {
        CallObserverNotifications.ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = nm.getNotificationChannel(CallObserverService.CHANNEL_ID)
        channel shouldNotBe null
        channel?.importance shouldBe NotificationManager.IMPORTANCE_LOW
        channel?.shouldVibrate() shouldBe false
        channel?.sound shouldBe null
    }

    @Test
    fun `build returns an ongoing notification with §17_0_3 copy`() {
        val notif = CallObserverNotifications.build(
            context,
            PassiveNotificationCopy(eventsLast24h = 2, alertsLast24h = 0),
        )
        val title = notif.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val text = notif.extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
        title shouldBe "Watching for fraud signals"
        text shouldBe "Last 24 h: 2 events, 0 alerts."
    }
}
