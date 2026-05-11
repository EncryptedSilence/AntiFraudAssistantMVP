package com.qalqan.antifraud.calls

import android.Manifest
import android.os.Build
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PermissionRequesterTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // Android 13
    fun `requestList includes POST_NOTIFICATIONS on Android 13+`() {
        PermissionRequester.requestList() shouldContain Manifest.permission.POST_NOTIFICATIONS
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // Android 12
    fun `requestList omits POST_NOTIFICATIONS on Android 12 and below`() {
        PermissionRequester.requestList() shouldNotContain Manifest.permission.POST_NOTIFICATIONS
    }

    @Test
    fun `requestList always includes the §4_2_1 runtime pair`() {
        val list = PermissionRequester.requestList()
        list shouldContain Manifest.permission.READ_PHONE_STATE
        list shouldContain Manifest.permission.READ_CALL_LOG
    }

    @Test
    fun `summarize maps grant maps to a tri-state`() {
        val granted =
            mapOf(
                Manifest.permission.READ_PHONE_STATE to true,
                Manifest.permission.READ_CALL_LOG to true,
            )
        PermissionRequester.summarize(granted) shouldBe CallObserverPermissions.State.GRANTED

        val partial =
            mapOf(
                Manifest.permission.READ_PHONE_STATE to true,
                Manifest.permission.READ_CALL_LOG to false,
            )
        PermissionRequester.summarize(partial) shouldBe CallObserverPermissions.State.PARTIAL

        val denied =
            mapOf(
                Manifest.permission.READ_PHONE_STATE to false,
                Manifest.permission.READ_CALL_LOG to false,
            )
        PermissionRequester.summarize(denied) shouldBe CallObserverPermissions.State.DENIED
    }
}
