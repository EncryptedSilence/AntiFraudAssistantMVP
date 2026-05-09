package com.qalqan.antifraud.database.contacts

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
class ContactProfileDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(context)
    private val dao = db.contactProfileDao()

    @After
    fun close() = db.close()

    @Test
    fun `upsert and findByHash round-trip`() {
        runBlocking {
            val e =
                ContactProfileEntity(
                    id = "c1",
                    phoneNormalizedEnc = byteArrayOf(),
                    phoneHash = "h1",
                    phoneLast4 = "1234",
                    isShortCode = false,
                    displayNameLocal = null,
                    isInContacts = false,
                    trustStatus = "UNKNOWN",
                    firstSeenAtMs = TS,
                    lastSeenAtMs = TS,
                    riskCounter = 0,
                    userComment = null,
                )
            dao.upsert(e)
            dao.findByHash("h1") shouldBe e
        }
    }

    private companion object {
        const val TS: Long = 1_700_000_000_000
    }
}
