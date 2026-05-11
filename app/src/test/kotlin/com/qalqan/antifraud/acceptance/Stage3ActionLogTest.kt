package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.CallObserverActionLog
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Stage3ActionLogTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)

    @After fun tearDown() { repos.close() }

    @Test
    fun `observer start, permission grant, and observer stop produce three entries with no phone numbers`() {
        val log = CallObserverActionLog(repos.actionLogger)
        runBlocking {
            log.observerStarted()
            log.grant(android.Manifest.permission.READ_PHONE_STATE)
            log.observerStopped()

            val entries = repos.actionLog.recent(10)
            entries.size shouldBe 3
            entries.map { it.action } shouldContain AppAction.SETTING_CHANGED
            entries.map { it.action } shouldContain AppAction.PERMISSION_GRANTED
            entries.flatMap { it.details.keys }.forEach { k ->
                (k in setOf("phone", "phoneNumber", "phoneNormalized")) shouldBe false
            }
        }
    }
}
