package com.qalqan.antifraud.sms

import android.Manifest
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Spec §23 #32 — with RECEIVE_SMS or READ_SMS denied, the app falls back to manual paste
 * without crashing. Verifies the state-machine reads correctly; the full UI fallback is
 * verified by the "I had a suspicious SMS" reachability test (Phase 7).
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance32DeniedFallbackTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
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
    fun `state is PARTIAL with only READ_SMS`() {
        app.grantPermissions(Manifest.permission.READ_SMS)
        SmsObserverPermissions(context).state() shouldBe SmsObserverPermissions.State.PARTIAL
    }

    @Test
    fun `summarize maps a denied response to DENIED`() {
        val granted = mapOf(
            Manifest.permission.RECEIVE_SMS to false,
            Manifest.permission.READ_SMS to false,
        )
        SmsPermissionRequester.summarize(granted) shouldBe SmsObserverPermissions.State.DENIED
    }
}
