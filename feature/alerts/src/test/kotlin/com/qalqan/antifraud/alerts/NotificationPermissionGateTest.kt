package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationPermissionGateTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `when notifications are enabled monitoring stays active`() {
        val gate = NotificationPermissionGate(ctx)
        shadowOf(ctx.getSystemService(android.app.NotificationManager::class.java))
            .setNotificationsEnabled(true)
        gate.activeMonitoringAllowed() shouldBe true
    }

    @Test
    fun `when notifications are disabled monitoring is disabled (loud failure)`() {
        val gate = NotificationPermissionGate(ctx)
        shadowOf(ctx.getSystemService(android.app.NotificationManager::class.java))
            .setNotificationsEnabled(false)
        gate.activeMonitoringAllowed() shouldBe false
    }
}
