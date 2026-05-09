package com.qalqan.antifraud.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.collections.shouldBeEmpty
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class WipeAllTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)

    @After
    fun close() = repos.close()

    @Test
    fun `wipeAll empties every entity table`() {
        runBlocking {
            repos.calls.save(
                CallEvent(
                    id = EventId("c1"),
                    phoneHash = PhoneHash("h"),
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = Instant.parse("2026-05-08T10:00:00Z"),
                    endedAt = null,
                    durationSec = 0,
                    isKnownContact = false,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
            repos.wipeAll()
            repos.calls.listSince(Instant.EPOCH).shouldBeEmpty()
        }
    }
}
