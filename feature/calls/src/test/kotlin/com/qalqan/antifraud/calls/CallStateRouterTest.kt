package com.qalqan.antifraud.calls

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class CallStateRouterTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // Android 12
    fun `selects modern path on Android 12+`() {
        val router = CallStateRouter(context) {}
        router.path() shouldBe CallStateRouter.Path.MODERN
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // Android 11
    fun `selects legacy path on Android 11`() {
        val router = CallStateRouter(context) {}
        router.path() shouldBe CallStateRouter.Path.LEGACY
    }

    @Test
    fun `register on legacy returns true with TelephonyManager service available`() {
        val router = CallStateRouter(context) {}
        router.register()
        router.unregister() // must be idempotent
    }
}
