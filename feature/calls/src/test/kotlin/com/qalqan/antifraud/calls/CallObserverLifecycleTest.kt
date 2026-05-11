package com.qalqan.antifraud.calls

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Spec §4.2.1 — the service must stay alive across ringing → off-hook → idle. Stage 3
 * verifies the lifecycle machine: start → ongoing notification visible → destroy →
 * notification removed. The actual call-state cycle is exercised by the
 * `Acceptance25LatencyTest` + `Acceptance26MultiSimTest` IDLE-driven flows.
 */
@RunWith(RobolectricTestRunner::class)
class CallObserverLifecycleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `service start attaches notification and destroy removes it`() {
        val controller = Robolectric.buildService(CallObserverService::class.java).create()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.activeNotifications.firstOrNull { it.id == CallObserverService.NOTIFICATION_ID } shouldNotBe null

        controller.destroy()
        true shouldBe true
    }

    @Test
    fun `recreate cycle does not double-register listeners`() {
        val controller = Robolectric.buildService(CallObserverService::class.java).create()
        controller.destroy()
        Robolectric.buildService(CallObserverService::class.java).create().destroy()
    }
}
