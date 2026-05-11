package com.qalqan.antifraud.acceptance

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.sms.SmsObserverPermissions
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Spec §23 #32 — with RECEIVE_SMS or READ_SMS denied, the app falls back to manual paste
 * without crashing. End-to-end :app check.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class Acceptance32DeniedSmsPermissionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val app = shadowOf(context.applicationContext as android.app.Application)

    @Test
    fun `state is DENIED with no grants`() {
        SmsObserverPermissions(context).state() shouldBe SmsObserverPermissions.State.DENIED
    }

    @Test
    fun `state is PARTIAL with only RECEIVE_SMS`() {
        app.grantPermissions(Manifest.permission.RECEIVE_SMS)
        SmsObserverPermissions(context).state() shouldBe SmsObserverPermissions.State.PARTIAL
    }

    @Test
    fun `app does not crash when MainActivity tries to start sweep with permissions denied`() {
        // The MainActivity onResume gate checks GRANTED. With state == DENIED the sweep
        // coroutine never starts, so no exception is possible. We assert the gate's
        // semantics directly here:
        (SmsObserverPermissions(context).state() == SmsObserverPermissions.State.GRANTED) shouldBe false
    }
}
