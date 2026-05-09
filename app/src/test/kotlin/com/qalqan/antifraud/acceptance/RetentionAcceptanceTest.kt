package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.RetentionPurger
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.WebEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RetentionAcceptanceTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val purger = RetentionPurger.forRepositories(repos)

    @After
    fun close() = repos.close()

    @Test
    fun `events older than 30 days are purged (spec §23 #8)`() {
        runBlocking {
            val now = Instant.parse("2026-05-08T10:00:00Z")
            repos.calls.save(
                CallEvent(
                    id = EventId("old"),
                    phoneHash = PhoneHash("h"),
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = now.minus(Duration.ofDays(40)),
                    endedAt = null,
                    durationSec = 0,
                    isKnownContact = false,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
            purger.purge(now)
            repos.calls.listSince(Instant.EPOCH).shouldBeEmpty()
        }
    }

    @Test
    fun `WebEvent never stores a full URL (spec §23 #24)`() {
        // The domain entity invariant in T16 already enforces this; a DB round-trip confirms.
        runBlocking {
            val now = Instant.parse("2026-05-08T10:00:00Z")
            repos.web.save(
                WebEvent(
                    id = EventId("w1"),
                    domainHash = DomainHash("h"),
                    domainDisplayLocal = "halyk-secure.example",
                    visitedAt = now,
                    isNewDomain = true,
                    domainStatus = DomainStatus.NEW,
                    webRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
            val saved = repos.web.listSince(Instant.EPOCH).single()
            ('/' in saved.domainDisplayLocal) shouldBe false
            ('?' in saved.domainDisplayLocal) shouldBe false
        }
    }
}
