package com.qalqan.antifraud.calls

import android.Manifest
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Spec §23 #27 — with READ_PHONE_STATE or READ_CALL_LOG denied, the app falls back to
 * manual entry without crashing. Stage 3 verifies the *state machine* and the fact that
 * `CallObserverPermissions.state()` reports DENIED / PARTIAL accordingly. The full UI
 * fallback (manual-entry buttons reachable in one tap) is verified in T37.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance27DeniedFallbackTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val app = shadowOf(context.applicationContext as android.app.Application)

    @Test
    fun `state is DENIED when no permissions granted`() {
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.DENIED
    }

    @Test
    fun `state is PARTIAL when only READ_PHONE_STATE granted`() {
        app.grantPermissions(Manifest.permission.READ_PHONE_STATE)
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.PARTIAL
    }

    @Test
    fun `state is PARTIAL when only READ_CALL_LOG granted`() {
        app.grantPermissions(Manifest.permission.READ_CALL_LOG)
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.PARTIAL
    }

    @Test
    fun `summarize maps a denied response to DENIED`() {
        val granted = mapOf(
            Manifest.permission.READ_PHONE_STATE to false,
            Manifest.permission.READ_CALL_LOG to false,
        )
        PermissionRequester.summarize(granted) shouldBe CallObserverPermissions.State.DENIED
    }
}
