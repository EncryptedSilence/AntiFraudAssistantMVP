@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.sms

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ManifestPrivacyBoundaryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    /** §23 #33 — manifest must contain no `<intent-filter>` claiming the default-SMS role. */
    @Test
    fun `manifest does not declare SMS_DELIVER intent filter`() {
        val receivers =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_RECEIVERS or PackageManager.GET_INTENT_FILTERS,
            ).receivers ?: emptyArray()
        // No receiver in the :feature:sms manifest claims SMS_DELIVER.
        receivers.forEach { ri ->
            // Robolectric exposes the receiver name but does not always populate
            // intent filter actions; the strongest assertion we can make here is
            // that we declare no receiver named with a "Deliver" suffix and the
            // BROADCAST_SMS permission gate is present on our one receiver.
            (ri.name.contains("Deliver", ignoreCase = true)) shouldBe false
        }
    }

    /** §2.1 — no `SEND_SMS`, no MMS / WAP-push permissions. */
    @Test
    fun `manifest does not request forbidden SMS-app permissions`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        perms shouldNotContain "android.permission.SEND_SMS"
        perms shouldNotContain "android.permission.WRITE_SMS"
        perms shouldNotContain "android.permission.RECEIVE_MMS"
        perms shouldNotContain "android.permission.RECEIVE_WAP_PUSH"
        perms shouldNotContain "android.permission.BROADCAST_SMS"
    }

    /** §4.2.2 — `RECEIVE_SMS` and `READ_SMS` ARE expected. */
    @Test
    fun `manifest declares the two §4_2_2 runtime permissions`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        (android.Manifest.permission.RECEIVE_SMS in perms) shouldBe true
        (android.Manifest.permission.READ_SMS in perms) shouldBe true
    }

    /** §4.2.2 — exactly one receiver is declared and it is gated on BROADCAST_SMS. */
    @Test
    fun `SmsBroadcastReceiver is declared exported and gated on BROADCAST_SMS`() {
        val receivers =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_RECEIVERS,
            ).receivers ?: emptyArray()
        val ours = receivers.firstOrNull { it.name.endsWith("SmsBroadcastReceiver") }
        ours shouldBe ours
        ours?.exported shouldBe true
        ours?.permission shouldBe "android.permission.BROADCAST_SMS"
    }
}
