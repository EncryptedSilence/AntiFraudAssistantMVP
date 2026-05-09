package com.qalqan.antifraud.database.manual

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.domain.CallDirection
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class PrivacyBoundaryTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val manual = ManualEntry.create(ctx, repos, InMemoryCryptoBox())

    @After
    fun close() = repos.close()

    @Test
    fun `submitted phone is hashed, not persisted in plaintext`() {
        runBlocking {
            val id =
                manual.calls.submit(
                    rawNumber = "+77001234567",
                    direction = CallDirection.INCOMING,
                    startedAt = Instant.parse("2026-05-08T10:00:00Z"),
                    durationSec = 60,
                    isKnownContact = false,
                )
            val saved = repos.calls.find(id)!!
            saved.phoneHash.value shouldNotBe "+77001234567"
            saved.phoneHash.value shouldNotBe "77001234567"
        }
    }
}
