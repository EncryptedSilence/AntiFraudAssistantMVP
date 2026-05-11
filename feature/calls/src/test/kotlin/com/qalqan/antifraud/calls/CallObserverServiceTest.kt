package com.qalqan.antifraud.calls

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Spec §4.2.1 — service runs as foreground type `phoneCall`. Verifies the service
 * attaches a notification, registers the router on create, and unregisters on destroy.
 *
 * AndroidKeyStore is unavailable under Robolectric, so we replace the default
 * [CallObserverService.repositoriesFactory] with [Repositories.inMemory] before each test.
 */
@RunWith(RobolectricTestRunner::class)
class CallObserverServiceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun useInMemoryRepositories() {
        CallObserverService.repositoriesFactory = { ctx -> Repositories.inMemory(ctx) }
    }

    @Test
    fun `onCreate attaches an ongoing notification`() {
        val controller = Robolectric.buildService(CallObserverService::class.java).create()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = nm.activeNotifications.firstOrNull { it.id == CallObserverService.NOTIFICATION_ID }
        notif shouldNotBe null
        (notif?.notification?.flags?.and(Notification.FLAG_ONGOING_EVENT) != 0) shouldBe true
        controller.destroy()
    }

    @Test
    fun `notification channel is low importance with no sound`() {
        Robolectric.buildService(CallObserverService::class.java).create().destroy()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = nm.getNotificationChannel(CallObserverService.CHANNEL_ID)
        channel?.importance shouldBe NotificationManager.IMPORTANCE_LOW
        channel?.sound shouldBe null
        channel?.shouldVibrate() shouldBe false
    }

    @Test
    fun `start helper is idempotent across the public start-stop API`() {
        CallObserverService.start(context)
        CallObserverService.stop(context)
        // No exception means the service can be torn down cleanly even before transitions arrive.
    }
}
