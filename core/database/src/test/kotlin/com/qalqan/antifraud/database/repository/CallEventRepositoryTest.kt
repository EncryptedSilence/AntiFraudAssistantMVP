package com.qalqan.antifraud.database.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class CallEventRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val repo = CallEventRepository(db.callEventDao())

    @After
    fun close() = db.close()

    @Test
    fun `save and load round-trip`() {
        runBlocking {
            val ev =
                CallEvent(
                    id = EventId("c1"),
                    phoneHash = PhoneHash("h"),
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = Instant.parse("2026-05-08T10:00:00Z"),
                    endedAt = Instant.parse("2026-05-08T10:01:00Z"),
                    durationSec = SIXTY,
                    isKnownContact = false,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                )
            repo.save(ev)
            repo.find(EventId("c1")) shouldBe ev
        }
    }

    private companion object {
        const val SIXTY: Long = 60
    }
}
