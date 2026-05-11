package com.qalqan.antifraud.acceptance

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.CallObserverService
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class PassiveTransparencyNotificationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun useInMemoryRepositories() {
        CallObserverService.repositoriesFactory = { ctx -> Repositories.inMemory(ctx) }
    }

    @Test
    fun `service create attaches an ongoing low-priority notification carrying §17_0_3 copy`() {
        val controller = Robolectric.buildService(CallObserverService::class.java).create()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val sbn = nm.activeNotifications.firstOrNull { it.id == CallObserverService.NOTIFICATION_ID }
        sbn shouldNotBe null
        val title = sbn?.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = sbn?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        title shouldBe "Watching for fraud signals"
        text?.shouldContain("Last 24 h:")
        (sbn?.notification?.flags?.and(Notification.FLAG_ONGOING_EVENT) != 0) shouldBe true
        controller.destroy()
    }
}
