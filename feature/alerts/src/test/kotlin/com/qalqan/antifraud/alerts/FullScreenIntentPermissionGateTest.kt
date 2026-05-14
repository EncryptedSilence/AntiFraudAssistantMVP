package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FullScreenIntentPermissionGateTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `Android 14+ honors NotificationManager canUseFullScreenIntent`() {
        val gate = FullScreenIntentPermissionGate(ctx)
        val nm = ctx.getSystemService(android.app.NotificationManager::class.java)
        gate.fullScreenAllowed() shouldBe nm.canUseFullScreenIntent()
    }
}
