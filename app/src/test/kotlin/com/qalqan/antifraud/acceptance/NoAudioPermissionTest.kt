package com.qalqan.antifraud.acceptance

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldNotContain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §2.1 / §22 hard rule — the app must NEVER request RECORD_AUDIO.
 * §23 #22 codifies this as an acceptance criterion.
 */
@RunWith(RobolectricTestRunner::class)
class NoAudioPermissionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `merged manifest does not request RECORD_AUDIO`() {
        val perms =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            ).requestedPermissions?.toList() ?: emptyList()
        perms shouldNotContain android.Manifest.permission.RECORD_AUDIO
    }
}
