package com.qalqan.antifraud.acceptance

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class Stage9MergedManifestTest {
    @Test
    fun `merged manifest declares alert permissions and no §2_1 forbidden ones`() {
        val pm = RuntimeEnvironment.getApplication().packageManager
        val info =
            pm.getPackageInfo(
                RuntimeEnvironment.getApplication().packageName,
                android.content.pm.PackageManager.GET_PERMISSIONS,
            )
        val perms = info.requestedPermissions?.toList().orEmpty()

        perms shouldContain "android.permission.USE_FULL_SCREEN_INTENT"
        perms shouldContain "android.permission.SYSTEM_ALERT_WINDOW"

        perms shouldNotContainAnyOf
            listOf(
                "android.permission.RECORD_AUDIO",
                "android.permission.BIND_ACCESSIBILITY_SERVICE",
                "android.permission.BIND_INCALL_SERVICE",
                "android.permission.BIND_SCREENING_SERVICE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MANAGE_EXTERNAL_STORAGE",
                "android.permission.PACKAGE_USAGE_STATS",
            )
    }

    @Test
    fun `merged manifest declares CriticalAlertActivity (full-screen target)`() {
        val pm = RuntimeEnvironment.getApplication().packageManager
        val info =
            pm.getPackageInfo(
                RuntimeEnvironment.getApplication().packageName,
                android.content.pm.PackageManager.GET_ACTIVITIES,
            )
        val names = info.activities?.map { it.name }.orEmpty()
        names shouldContain "com.qalqan.antifraud.alerts.CriticalAlertActivity"
    }

    @Test
    fun `merged manifest declares OverlayBannerActivity (overlay target)`() {
        val pm = RuntimeEnvironment.getApplication().packageManager
        val info =
            pm.getPackageInfo(
                RuntimeEnvironment.getApplication().packageName,
                android.content.pm.PackageManager.GET_ACTIVITIES,
            )
        val names = info.activities?.map { it.name }.orEmpty()
        names shouldContain "com.qalqan.antifraud.alerts.OverlayBannerActivity"
    }
}
