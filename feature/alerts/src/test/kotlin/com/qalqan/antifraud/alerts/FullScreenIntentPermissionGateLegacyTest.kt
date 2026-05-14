package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class FullScreenIntentPermissionGateLegacyTest {
    @Test
    fun `pre-Android 14 always returns true`() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        FullScreenIntentPermissionGate(ctx).fullScreenAllowed() shouldBe true
    }
}
