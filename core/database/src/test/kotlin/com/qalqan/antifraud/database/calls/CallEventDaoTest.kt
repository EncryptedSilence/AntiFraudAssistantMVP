package com.qalqan.antifraud.database.calls

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallEventDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.callEventDao()

    @After
    fun close() = db.close()

    @Test
    fun `delete older than removes only old rows`() {
        runBlocking {
            dao.upsert(sample("c1", CUTOFF - 1))
            dao.upsert(sample("c2", CUTOFF + 1))
            dao.deleteOlderThan(CUTOFF) shouldBe 1
            dao.findById("c1") shouldBe null
            dao.findById("c2")?.id shouldBe "c2"
        }
    }

    private fun sample(
        id: String,
        atMs: Long,
    ) = CallEventEntity(
        id = id,
        phoneHash = "h",
        simSlot = null,
        direction = "INCOMING",
        startedAtMs = atMs,
        endedAtMs = atMs + SIXTY_K,
        durationSec = 60,
        isKnownContact = false,
        isRepeated = false,
        callRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private companion object {
        const val CUTOFF: Long = 1_700_000_000_000
        const val SIXTY_K: Long = 60_000
    }
}
