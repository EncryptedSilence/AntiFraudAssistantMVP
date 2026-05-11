package com.qalqan.antifraud.sms

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class SmsObserverPermissionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val app = shadowOf(context.applicationContext as android.app.Application)

    @Test
    fun `state is denied when no permissions granted`() {
        SmsObserverPermissions(context).state() shouldBe SmsObserverPermissions.State.DENIED
    }

    @Test
    fun `state is granted when both RECEIVE_SMS and READ_SMS granted`() {
        app.grantPermissions(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        SmsObserverPermissions(context).state() shouldBe SmsObserverPermissions.State.GRANTED
    }

    @Test
    fun `state is partial when only one permission granted`() {
        app.grantPermissions(Manifest.permission.RECEIVE_SMS)
        SmsObserverPermissions(context).state() shouldBe SmsObserverPermissions.State.PARTIAL
    }

    @Test
    fun `required returns the two §4_2_2 runtime permissions`() {
        SmsObserverPermissions.REQUIRED shouldBe
            listOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
    }
}
