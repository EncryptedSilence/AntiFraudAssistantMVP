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
class Stage9LoudFailureBannerTest {
    @Test
    fun `gate reports denied when system notifications are disabled (§23 #40)`() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        shadowOf(ctx.getSystemService(android.app.NotificationManager::class.java))
            .setNotificationsEnabled(false)
        NotificationPermissionGate(ctx).activeMonitoringAllowed() shouldBe false
    }

    @Test
    fun `gate reports allowed when system notifications are enabled`() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        shadowOf(ctx.getSystemService(android.app.NotificationManager::class.java))
            .setNotificationsEnabled(true)
        NotificationPermissionGate(ctx).activeMonitoringAllowed() shouldBe true
    }
}
