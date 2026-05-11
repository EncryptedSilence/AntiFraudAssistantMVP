package com.qalqan.antifraud.acceptance

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldNotContain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Acceptance28NoDefaultPhoneRoleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `merged manifest declares no BIND_INCALL_SERVICE`() {
        val services =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SERVICES,
            ).services
        val perms = services?.map { it.permission ?: "" } ?: emptyList()
        perms shouldNotContain "android.permission.BIND_INCALL_SERVICE"
    }

    @Test
    fun `merged manifest declares no BIND_SCREENING_SERVICE`() {
        val services =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SERVICES,
            ).services
        val perms = services?.map { it.permission ?: "" } ?: emptyList()
        perms shouldNotContain "android.permission.BIND_SCREENING_SERVICE"
    }

    @Test
    fun `merged manifest does not request CALL_PHONE`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        perms shouldNotContain android.Manifest.permission.CALL_PHONE
    }

    @Test
    fun `merged manifest does not request PROCESS_OUTGOING_CALLS`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        @Suppress("DEPRECATION")
        perms shouldNotContain android.Manifest.permission.PROCESS_OUTGOING_CALLS
    }
}
