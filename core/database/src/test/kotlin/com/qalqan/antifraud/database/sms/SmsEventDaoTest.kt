package com.qalqan.antifraud.database.sms

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
class SmsEventDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.smsEventDao()

    @After
    fun close() = db.close()

    @Test
    fun `findById returns inserted row`() {
        runBlocking {
            val e =
                SmsEventEntity(
                    id = "s1",
                    senderHash = "h",
                    senderDisplayNameLocal = null,
                    simSlot = null,
                    receivedAtMs = TS,
                    smsCategory = "UNKNOWN_SENDER",
                    containsCode = false,
                    containsLink = false,
                    containsFinancialKeyword = false,
                    containsSecurityKeyword = false,
                    bodyExcerptEnc = byteArrayOf(),
                    smsRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                )
            dao.upsert(e)
            dao.findById("s1") shouldBe e
        }
    }

    private companion object {
        const val TS: Long = 1_700_000_000_000
    }
}
