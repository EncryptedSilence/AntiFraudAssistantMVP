package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.alerts.NotificationPermissionGate
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Acceptance40LoudFailureTest {
    @Test
    fun `§23 #40 - gate reports denied when system notifications are off`() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        shadowOf(ctx.getSystemService(android.app.NotificationManager::class.java))
            .setNotificationsEnabled(false)
        NotificationPermissionGate(ctx).activeMonitoringAllowed() shouldBe false
    }

    @Test
    fun `§23 #40 - gate reports allowed when notifications are on`() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        shadowOf(ctx.getSystemService(android.app.NotificationManager::class.java))
            .setNotificationsEnabled(true)
        NotificationPermissionGate(ctx).activeMonitoringAllowed() shouldBe true
    }
}
