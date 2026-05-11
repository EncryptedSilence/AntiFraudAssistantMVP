package com.qalqan.antifraud.acceptance

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.calls.CallObserverService
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class Acceptance27DeniedPermissionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val app = shadowOf(context.applicationContext as android.app.Application)

    @Test
    fun `state is DENIED with no grants`() {
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.DENIED
    }

    @Test
    fun `state is PARTIAL with only READ_PHONE_STATE`() {
        app.grantPermissions(Manifest.permission.READ_PHONE_STATE)
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.PARTIAL
    }

    @Test
    fun `start helper does not crash when permissions are denied`() {
        CallObserverService.start(context)
        CallObserverService.stop(context)
    }
}
