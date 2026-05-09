package com.qalqan.antifraud.database.sessions

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
class RiskSessionDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.riskSessionDao()

    @After
    fun close() = db.close()

    @Test
    fun `listOpen returns only OPEN sessions and JSON columns round-trip`() {
        runBlocking {
            dao.upsert(
                RiskSessionEntity(
                    id = "open",
                    startedAtMs = TS,
                    endedAtMs = null,
                    status = "OPEN",
                    relatedCallEventIds = listOf("c1", "c2"),
                    relatedSmsEventIds = emptyList(),
                    relatedWebEventIds = emptyList(),
                    relatedUserAnswerIds = emptyList(),
                    sessionRiskScore = 0,
                    sessionRiskBand = "LOW",
                    explanation = null,
                ),
            )
            dao.upsert(
                RiskSessionEntity(
                    id = "closed",
                    startedAtMs = TS,
                    endedAtMs = TS,
                    status = "CLOSED_AUTO",
                    relatedCallEventIds = emptyList(),
                    relatedSmsEventIds = emptyList(),
                    relatedWebEventIds = emptyList(),
                    relatedUserAnswerIds = emptyList(),
                    sessionRiskScore = 0,
                    sessionRiskBand = "LOW",
                    explanation = null,
                ),
            )
            val open = dao.listOpen()
            open.size shouldBe 1
            open[0].id shouldBe "open"
            open[0].relatedCallEventIds shouldBe listOf("c1", "c2")
        }
    }

    private companion object {
        const val TS: Long = 1_700_000_000_000
    }
}
