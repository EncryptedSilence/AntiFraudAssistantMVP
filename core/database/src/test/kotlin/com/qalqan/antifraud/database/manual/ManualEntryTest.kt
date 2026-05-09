package com.qalqan.antifraud.database.manual

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.domain.CallDirection
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ManualEntryTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val manual = ManualEntry.create(ctx, repos, InMemoryCryptoBox())

    @After
    fun close() = repos.close()

    @Test
    fun `submitCall stores a normalized call event`() {
        runBlocking {
            val id = manual.calls.submit(
                rawNumber = "+7 700 123 45 67",
                direction = CallDirection.INCOMING,
                startedAt = Instant.parse("2026-05-08T10:00:00Z"),
                durationSec = 90,
                isKnownContact = false,
            )
            val saved = repos.calls.find(id)
            saved shouldNotBe null
            saved!!.phoneHash.value.length shouldBe SHA256_HEX_LEN
        }
    }

    @Test
    fun `submitAnswerNote rejects OTP-shaped text`() {
        runBlocking {
            shouldThrow<ManualEntry.SensitiveNoteRejected> {
                manual.answers.submitNote(
                    relatedEventId = com.qalqan.antifraud.domain.EventId("e1"),
                    noteText = "code 123456 share with caller",
                )
            }
        }
    }

    private companion object {
        const val SHA256_HEX_LEN = 64
    }
}
