package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlertPermissionResultLoggerTest {
    private val repos by lazy {
        Repositories.inMemory(ApplicationProvider.getApplicationContext<Context>())
    }

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `log writes PERMISSION_GRANTED with permission detail when granted`() =
        runTest {
            AlertPermissionResultLogger.log(
                logger = repos.actionLogger,
                permission = "android.permission.SYSTEM_ALERT_WINDOW",
                granted = true,
            )
            val entries = repos.actionLog.recent(MAX_RECENT)
            entries.any {
                it.action == AppAction.PERMISSION_GRANTED &&
                    it.details["permission"] == "android.permission.SYSTEM_ALERT_WINDOW"
            } shouldBe true
        }

    @Test
    fun `log writes PERMISSION_DENIED when denied`() =
        runTest {
            AlertPermissionResultLogger.log(
                logger = repos.actionLogger,
                permission = "android.permission.USE_FULL_SCREEN_INTENT",
                granted = false,
            )
            val entries = repos.actionLog.recent(MAX_RECENT)
            entries.any {
                it.action == AppAction.PERMISSION_DENIED &&
                    it.details["permission"] == "android.permission.USE_FULL_SCREEN_INTENT"
            } shouldBe true
        }

    private companion object {
        const val MAX_RECENT = 100
    }
}
