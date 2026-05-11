package com.qalqan.antifraud.calls

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallObserverActionLogTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val log = CallObserverActionLog(repos.actionLogger)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `grants are logged as PERMISSION_GRANTED with the permission key`() {
        runBlocking {
            log.grant(android.Manifest.permission.READ_PHONE_STATE)
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.PERMISSION_GRANTED
            entry.details["permission"] shouldBe android.Manifest.permission.READ_PHONE_STATE
        }
    }

    @Test
    fun `denies are logged as PERMISSION_DENIED`() {
        runBlocking {
            log.deny(android.Manifest.permission.READ_CALL_LOG)
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.PERMISSION_DENIED
            entry.details["permission"] shouldBe android.Manifest.permission.READ_CALL_LOG
        }
    }

    @Test
    fun `observer state changes are logged as SETTING_CHANGED with auto_call_capture`() {
        runBlocking {
            log.observerStarted()
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.SETTING_CHANGED
            entry.details["setting"] shouldBe "auto_call_capture"
            entry.details["state"] shouldBe "running"
        }
    }

    @Test
    fun `observer stopped is also a SETTING_CHANGED with state stopped`() {
        runBlocking {
            log.observerStopped()
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.SETTING_CHANGED
            entry.details["state"] shouldBe "stopped"
        }
    }
}
