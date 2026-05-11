package com.qalqan.antifraud.sms

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.PassiveCounters
import com.qalqan.antifraud.calls.PassiveNotificationCopy
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

/**
 * Spec §17.0.3 — the transparency notification body must reflect the union of call + SMS
 * counts in the last 24 h. This is the cross-module integration check for the Stage 4
 * counters extension; the notification builder itself is unchanged.
 */
@RunWith(RobolectricTestRunner::class)
class PassiveNotificationCopySumsCallsAndSmsTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val manual = ManualEntry.create(context, repos, InMemoryCryptoBox())

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `body counts 2 calls + 3 SMS as 5 events`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            manual.calls.submit("+72", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            manual.sms.submit("S1", now.minusSeconds(60), "B")
            manual.sms.submit("S2", now.minusSeconds(60), "B")
            manual.sms.submit("S3", now.minusSeconds(60), "B")

            val counters = PassiveCounters(repos)
            val events = counters.eventsLast24h(now)
            val alerts = counters.alertsLast24h(now)
            val copy = PassiveNotificationCopy(eventsLast24h = events, alertsLast24h = alerts)
            copy.body shouldBe "Last 24 h: 5 events, 0 alerts."
        }
    }
}
