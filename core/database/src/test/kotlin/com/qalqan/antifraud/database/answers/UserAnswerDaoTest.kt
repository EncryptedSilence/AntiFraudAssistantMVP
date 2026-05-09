package com.qalqan.antifraud.database.answers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserAnswerDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.userAnswerDao()

    @After
    fun close() = db.close()

    @Test
    fun `minimal projection never selects the encrypted note column`() {
        runBlocking {
            val secret = byteArrayOf(0x42, 0x42, 0x42)
            dao.upsert(
                UserAnswerEntity(
                    id = "a1",
                    relatedEventId = "e1",
                    relatedSessionId = null,
                    relatedCampaignId = null,
                    questionCode = "Q1_CALLER_OFFICIAL_CLAIM",
                    answerCode = "YES",
                    userNoteLocalEnc = secret,
                    answerRiskScore = 0,
                    createdAtMs = TS,
                ),
            )
            val rows = dao.listMinimalProjection()
            rows.size shouldBe 1
            rows[0].id shouldBe "a1"
            // The summary type does not even contain a note field — privacy boundary holds by construction.
            rows[0]::class.java.declaredFields.any { it.name.contains("note", ignoreCase = true) } shouldBe false
            // Sanity: the full row still preserves the encrypted blob.
            dao.findById("a1")?.userNoteLocalEnc shouldNotBe null
        }
    }

    private companion object {
        const val TS: Long = 1_700_000_000_000
    }
}
