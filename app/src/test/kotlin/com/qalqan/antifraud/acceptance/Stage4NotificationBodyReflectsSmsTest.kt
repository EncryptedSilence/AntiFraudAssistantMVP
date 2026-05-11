package com.qalqan.antifraud.acceptance

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
 * Spec §17.0.3 — the transparency notification body must reflect the sum of call + SMS
 * counts after Stage 4. The notification builder itself is unchanged from Stage 3.
 */
@RunWith(RobolectricTestRunner::class)
class Stage4NotificationBodyReflectsSmsTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val manual = ManualEntry.create(context, repos, InMemoryCryptoBox())

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `body counts mixed calls and SMS as a single events total`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            manual.sms.submit("Bank", now.minusSeconds(60), "Перевод KZT")
            manual.sms.submit("1414", now.minusSeconds(60), "Citizen alert")
            val counters = PassiveCounters(repos)
            val copy =
                PassiveNotificationCopy(
                    eventsLast24h = counters.eventsLast24h(now),
                    alertsLast24h = counters.alertsLast24h(now),
                )
            copy.body shouldBe "Last 24 h: 3 events, 0 alerts."
        }
    }
}
