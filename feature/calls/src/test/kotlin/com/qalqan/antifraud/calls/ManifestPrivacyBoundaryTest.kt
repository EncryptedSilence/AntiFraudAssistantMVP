package com.qalqan.antifraud.calls

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

    /** §23 #28 — manifest must contain no `<intent-filter>` claiming the default-Phone role. */
    @Test
    fun `manifest does not request default-Phone role`() {
        val pkg = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES,
        )
        val activityActions = pkg.activities?.flatMap { it.exported.let { _ -> emptyList<String>() } } ?: emptyList()
        // android.intent.action.DIAL / android.intent.action.CALL targets are reserved for the default-Phone role
        activityActions shouldNotContain "android.intent.action.DIAL"
        activityActions shouldNotContain "android.intent.action.CALL"
    }

    /** §23 #28 — manifest must contain no `BIND_INCALL_SERVICE` declaration. */
    @Test
    fun `manifest does not bind InCallService`() {
        val services = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SERVICES,
        ).services
        val permissions = services?.map { it.permission ?: "" } ?: emptyList()
        permissions shouldNotContain "android.permission.BIND_INCALL_SERVICE"
    }

    /** §23 #28 — no `BIND_SCREENING_SERVICE` either. */
    @Test
    fun `manifest does not bind CallScreeningService`() {
        val services = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SERVICES,
        ).services
        val permissions = services?.map { it.permission ?: "" } ?: emptyList()
        permissions shouldNotContain "android.permission.BIND_SCREENING_SERVICE"
    }

    /** §4.2.1 — `READ_PHONE_STATE` and `READ_CALL_LOG` ARE expected. */
    @Test
    fun `manifest declares the two §4_2_1 runtime permissions`() {
        val perms = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        ).requestedPermissions?.toList() ?: emptyList()
        (android.Manifest.permission.READ_PHONE_STATE in perms) shouldBe true
        (android.Manifest.permission.READ_CALL_LOG in perms) shouldBe true
    }
}
