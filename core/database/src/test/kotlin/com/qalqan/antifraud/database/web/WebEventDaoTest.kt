package com.qalqan.antifraud.database.web

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
class WebEventDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.webEventDao()

    @After
    fun close() = db.close()

    @Test
    fun `delete older than removes only old rows`() {
        runBlocking {
            dao.upsert(sample("w1", CUTOFF - 1))
            dao.upsert(sample("w2", CUTOFF + 1))
            dao.deleteOlderThan(CUTOFF) shouldBe 1
            dao.findById("w1") shouldBe null
            dao.findById("w2")?.id shouldBe "w2"
        }
    }

    private fun sample(
        id: String,
        atMs: Long,
    ) = WebEventEntity(
        id = id,
        domainHash = "h",
        domainDisplayLocal = "example.com",
        visitedAtMs = atMs,
        isNewDomain = false,
        domainStatus = "KNOWN",
        webRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null,
    )

    private companion object {
        const val CUTOFF: Long = 1_700_000_000_000
    }
}
