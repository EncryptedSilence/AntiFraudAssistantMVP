package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.domain.CallDirection
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ManualEntryAcceptanceTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val manual = ManualEntry.create(ctx, repos, InMemoryCryptoBox())

    @After
    fun close() = repos.close()

    @Test
    fun `manual entry of call SMS web each persists a single event (spec §23 #5)`() {
        runBlocking {
            manual.calls.submit(
                rawNumber = "+77001234567",
                direction = CallDirection.INCOMING,
                startedAt = Instant.parse("2026-05-08T10:00:00Z"),
                durationSec = 90,
                isKnownContact = false,
            )
            manual.sms.submit(
                sender = "BANK",
                receivedAt = Instant.parse("2026-05-08T10:01:00Z"),
                body = "code 482917",
            )
            manual.web.submit(
                domainEtldPlusOne = "halyk-secure.example",
                visitedAt = Instant.parse("2026-05-08T10:02:00Z"),
            )

            repos.calls.listSince(Instant.EPOCH) shouldHaveSize 1
            repos.sms.listSince(Instant.EPOCH) shouldHaveSize 1
            repos.web.listSince(Instant.EPOCH) shouldHaveSize 1
        }
    }
}
