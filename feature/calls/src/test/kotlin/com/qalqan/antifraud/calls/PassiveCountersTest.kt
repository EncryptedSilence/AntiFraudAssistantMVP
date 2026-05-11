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
    fun `alertsLast24h reports zero in Stage 3 (alert pipeline lands in Stage 9)`() {
        val counters = PassiveCounters(repos)
        counters.alertsLast24h(Instant.now()) shouldBe 0
    }
}
