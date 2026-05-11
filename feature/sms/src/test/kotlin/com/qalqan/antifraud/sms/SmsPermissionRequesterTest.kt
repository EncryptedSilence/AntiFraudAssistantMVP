package com.qalqan.antifraud.sms

import android.Manifest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmsPermissionRequesterTest {

    @Test
    fun `requestList contains exactly the §4_2_2 runtime pair`() {
        SmsPermissionRequester.requestList() shouldContainExactlyInAnyOrder listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
        )
    }

    @Test
    fun `summarize maps a full-grant map to GRANTED`() {
        val granted = mapOf(
            Manifest.permission.RECEIVE_SMS to true,
            Manifest.permission.READ_SMS to true,
        )
        SmsPermissionRequester.summarize(granted) shouldBe SmsObserverPermissions.State.GRANTED
    }

    @Test
    fun `summarize maps a partial-grant map to PARTIAL`() {
        val partial = mapOf(
            Manifest.permission.RECEIVE_SMS to true,
            Manifest.permission.READ_SMS to false,
        )
        SmsPermissionRequester.summarize(partial) shouldBe SmsObserverPermissions.State.PARTIAL
    }

    @Test
    fun `summarize maps a no-grant map to DENIED`() {
        val denied = mapOf(
            Manifest.permission.RECEIVE_SMS to false,
            Manifest.permission.READ_SMS to false,
        )
        SmsPermissionRequester.summarize(denied) shouldBe SmsObserverPermissions.State.DENIED
    }
}
