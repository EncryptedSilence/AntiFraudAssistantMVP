package com.qalqan.antifraud.sms

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
class SmsObserverActionLogTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val log = SmsObserverActionLog(repos.actionLogger)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `grant is logged as PERMISSION_GRANTED with the permission key`() {
        runBlocking {
            log.grant(android.Manifest.permission.RECEIVE_SMS)
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.PERMISSION_GRANTED
            entry.details["permission"] shouldBe android.Manifest.permission.RECEIVE_SMS
        }
    }

    @Test
    fun `deny is logged as PERMISSION_DENIED`() {
        runBlocking {
            log.deny(android.Manifest.permission.READ_SMS)
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.PERMISSION_DENIED
        }
    }

    @Test
    fun `sweep start is logged as SETTING_CHANGED auto_sms_sweep running`() {
        runBlocking {
            log.sweepStarted()
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.SETTING_CHANGED
            entry.details["setting"] shouldBe "auto_sms_sweep"
            entry.details["state"] shouldBe "running"
        }
    }

    @Test
    fun `sweep stop is logged with state stopped`() {
        runBlocking {
            log.sweepStopped()
            val entry = repos.actionLog.recent(1).single()
            entry.details["state"] shouldBe "stopped"
        }
    }
}
