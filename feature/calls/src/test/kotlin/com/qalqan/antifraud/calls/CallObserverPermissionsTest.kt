package com.qalqan.antifraud.calls

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class CallObserverPermissionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val app = shadowOf(context.applicationContext as android.app.Application)

    @Test
    fun `state is denied when no permissions granted`() {
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.DENIED
    }

    @Test
    fun `state is granted when both READ_PHONE_STATE and READ_CALL_LOG granted`() {
        app.grantPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.GRANTED
    }

    @Test
    fun `state is partial when only one permission granted`() {
        app.grantPermissions(Manifest.permission.READ_PHONE_STATE)
        CallObserverPermissions(context).state() shouldBe CallObserverPermissions.State.PARTIAL
    }

    @Test
    fun `required returns the two §4_2_1 runtime permissions`() {
        CallObserverPermissions.REQUIRED shouldBe
            listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
    }
}
