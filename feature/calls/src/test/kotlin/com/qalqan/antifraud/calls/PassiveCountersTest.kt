package com.qalqan.antifraud.calls

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.domain.CallDirection
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class PassiveCountersTest {
    private val context: android.content.Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(context)
    private val manual = ManualEntry.create(context, repos, InMemoryCryptoBox())

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `eventsLast24h counts only the last 24 hours of calls`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(36 * 3600), 30, false)
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(2 * 3600), 30, false)
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            val counters = PassiveCounters(repos)
            counters.eventsLast24h(now) shouldBe 2
        }
    }

    @Test
    fun `alertsLast24h reports zero when no PATTERN_APPLIED alert entries exist`() {
        runBlocking {
            val counters = PassiveCounters(repos)
            counters.alertsLast24h(Instant.now()) shouldBe 0
        }
    }

    @Test
    fun `alertsLast24h counts PATTERN_APPLIED entries with source=alert in the window`() {
        runBlocking {
            val now = Instant.now()
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.PATTERN_APPLIED,
                mapOf("source" to "alert"),
            )
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.PATTERN_APPLIED,
                mapOf("source" to "alert"),
            )
            // a non-alert PATTERN_APPLIED entry should not count
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.PATTERN_APPLIED,
                mapOf("source" to "bundle"),
            )
            val counters = PassiveCounters(repos)
            counters.alertsLast24h(now) shouldBe 2
        }
    }

    @Test
    fun `smsLast24h counts only the last 24 hours of SMS`() {
        runBlocking {
            val now = Instant.now()
            manual.sms.submit("S", now.minusSeconds(36 * 3600), "B1")
            manual.sms.submit("S", now.minusSeconds(2 * 3600), "B2")
            manual.sms.submit("S", now.minusSeconds(60), "B3")
            val counters = PassiveCounters(repos)
            counters.smsLast24h(now) shouldBe 2
        }
    }

    @Test
    fun `eventsLast24h sums calls and SMS in the window`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            manual.sms.submit("S", now.minusSeconds(60), "B")
            PassiveCounters(repos).eventsLast24h(now) shouldBe 2
        }
    }

    @Test
    fun `callsLast24h is unchanged from Stage 3 behavior`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            PassiveCounters(repos).callsLast24h(now) shouldBe 1
        }
    }
}
