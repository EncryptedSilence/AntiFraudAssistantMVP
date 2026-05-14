package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.CallObserverService
import com.qalqan.antifraud.calls.PassiveCounters
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Acceptance41PassiveTransparencyTest {
    @Test
    fun `§23 #41 - PassiveCounters_alertsLast24h reflects logged alert entries`() =
        runTest {
            val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
            val repos = Repositories.inMemory(ctx)
            try {
                repos.actionLogger.log(AppAction.PATTERN_APPLIED, mapOf("source" to "alert"))
                repos.actionLogger.log(AppAction.PATTERN_APPLIED, mapOf("source" to "alert"))
                val counters = PassiveCounters(repos)
                counters.alertsLast24h(Instant.now()) shouldBeGreaterThan 1
            } finally {
                repos.close()
            }
        }

    @Test
    fun `§23 #41 - the §17_0_3 passive observer channel constant is kept`() {
        CallObserverService.CHANNEL_ID.shouldNotBeEmpty()
    }
}
