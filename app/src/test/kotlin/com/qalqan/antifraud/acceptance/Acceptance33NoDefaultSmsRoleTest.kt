package com.qalqan.antifraud.acceptance

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #33 — the merged manifest must contain no intent filter claiming the
 * default-SMS role (`SMS_DELIVER`), no `RECEIVE_MMS`, no `RECEIVE_WAP_PUSH`.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance33NoDefaultSmsRoleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `merged manifest does not request RECEIVE_MMS`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        perms shouldNotContain "android.permission.RECEIVE_MMS"
    }

    @Test
    fun `merged manifest does not request RECEIVE_WAP_PUSH`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        perms shouldNotContain "android.permission.RECEIVE_WAP_PUSH"
    }

    @Test
    fun `merged manifest does not declare any receiver named with a Deliver suffix`() {
        val receivers =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_RECEIVERS,
            ).receivers ?: emptyArray()
        receivers.forEach { ri ->
            ri.name.endsWith("DeliverReceiver", ignoreCase = true) shouldBe false
            ri.name.contains(".SmsDeliver", ignoreCase = true) shouldBe false
        }
    }
}
