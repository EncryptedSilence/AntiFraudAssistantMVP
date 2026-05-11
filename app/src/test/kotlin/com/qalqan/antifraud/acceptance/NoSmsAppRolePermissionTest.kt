package com.qalqan.antifraud.acceptance

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldNotContain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §2.1 / §4.2.2 — the app observes SMS but never *is* the SMS app.
 * §23 #33 / #34 codify this.
 */
@RunWith(RobolectricTestRunner::class)
class NoSmsAppRolePermissionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `merged manifest does not request SEND_SMS`() {
        permsContain() shouldNotContain "android.permission.SEND_SMS"
    }

    @Test
    fun `merged manifest does not request WRITE_SMS`() {
        permsContain() shouldNotContain "android.permission.WRITE_SMS"
    }

    @Test
    fun `merged manifest does not request RECEIVE_MMS`() {
        permsContain() shouldNotContain "android.permission.RECEIVE_MMS"
    }

    @Test
    fun `merged manifest does not request RECEIVE_WAP_PUSH`() {
        permsContain() shouldNotContain "android.permission.RECEIVE_WAP_PUSH"
    }

    @Test
    fun `merged manifest does not request BROADCAST_SMS`() {
        permsContain() shouldNotContain "android.permission.BROADCAST_SMS"
    }

    private fun permsContain(): List<String> =
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        ).requestedPermissions?.toList() ?: emptyList()
}
