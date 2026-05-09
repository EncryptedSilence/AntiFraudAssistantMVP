package com.qalqan.antifraud.database.campaigns

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
class RiskCampaignDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.riskCampaignDao()

    @After
    fun close() = db.close()

    @Test
    fun `markFalsePositive flips the status`() {
        runBlocking {
            dao.upsert(sample("k1", "ACTIVE"))
            dao.markFalsePositive("k1") shouldBe 1
            dao.findById("k1")?.status shouldBe "FALSE_POSITIVE"
        }
    }

    @Test
    fun `deleteArchivedOlderThan only removes archived rows`() {
        runBlocking {
            dao.upsert(sample("active", "ACTIVE", lastEventAtMs = TS - 1))
            dao.upsert(sample("old", "ARCHIVED", lastEventAtMs = TS - 1))
            dao.upsert(sample("recent", "ARCHIVED", lastEventAtMs = TS + 1))
            dao.deleteArchivedOlderThan(TS) shouldBe 1
            dao.findById("active")?.campaignId shouldBe "active"
            dao.findById("old") shouldBe null
            dao.findById("recent")?.campaignId shouldBe "recent"
        }
    }

    private fun sample(
        id: String,
        status: String,
        lastEventAtMs: Long = TS,
    ) = RiskCampaignEntity(
        campaignId = id,
        startedAtMs = TS,
        lastEventAtMs = lastEventAtMs,
        status = status,
        scenarioType = null,
        relatedPhoneHashes = emptyList(),
        relatedSmsSenderHashes = emptyList(),
        relatedDomainHashes = emptyList(),
        relatedEventIds = emptyList(),
        relatedSessionIds = emptyList(),
        userAnswerIds = emptyList(),
        triggeredPatternIds = emptyList(),
        campaignRiskScore = 0,
        campaignRiskBand = "LOW",
        explanation = null,
    )

    private companion object {
        const val TS: Long = 1_700_000_000_000
    }
}
