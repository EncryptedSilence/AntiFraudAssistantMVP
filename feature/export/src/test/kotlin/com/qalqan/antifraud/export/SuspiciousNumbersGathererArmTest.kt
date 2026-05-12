package com.qalqan.antifraud.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.TrustStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class SuspiciousNumbersGathererArmTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `empty repository emits an empty list`() {
        runBlocking {
            val records = SuspiciousNumbersGathererArm.gather(repos)
            records.size shouldBe 0
        }
    }

    @Test
    fun `emits one record per suspicious contact, skipping trusted`() {
        runBlocking {
            val now = Instant.parse("2026-05-01T10:00:00Z")
            repos.contacts.save(
                ContactProfile(
                    id = "c-suspicious",
                    phoneNormalizedEnc = ByteArray(0),
                    phoneHash = PhoneHash("hash-suspicious"),
                    phoneLast4 = "4567",
                    isShortCode = false,
                    displayNameLocal = null,
                    isInContacts = false,
                    trustStatus = TrustStatus.SUSPICIOUS,
                    firstSeenAt = now,
                    lastSeenAt = now,
                    riskCounter = 3,
                    userComment = null,
                ),
            )
            repos.contacts.save(
                ContactProfile(
                    id = "c-trusted",
                    phoneNormalizedEnc = ByteArray(0),
                    phoneHash = PhoneHash("hash-trusted"),
                    phoneLast4 = "9999",
                    isShortCode = false,
                    displayNameLocal = null,
                    isInContacts = true,
                    trustStatus = TrustStatus.TRUSTED,
                    firstSeenAt = now,
                    lastSeenAt = now,
                    riskCounter = 0,
                    userComment = null,
                ),
            )

            val records = SuspiciousNumbersGathererArm.gather(repos)
            records.size shouldBe 1
            val r = records.first() as ExportRecord.SuspiciousNumber
            r.phoneLast4 shouldBe "4567"
            r.trustStatus shouldBe "suspicious"
        }
    }
}
