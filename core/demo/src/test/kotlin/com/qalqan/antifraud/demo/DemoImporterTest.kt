package com.qalqan.antifraud.demo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class DemoImporterTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val manual = ManualEntry.create(ctx, repos, InMemoryCryptoBox())
    private val importer = DemoImporter(manual)

    @After fun close() = repos.close()

    @Test fun `imports fast-attack fixture`() {
        runBlocking {
            val summary = importer.importBuiltin(ctx, BuiltInScenario.FAST_ATTACK)
            summary.callsImported shouldBe 1
            summary.smsImported shouldBe 1
        }
    }

    @Test fun `imports trust-grooming fixture`() {
        runBlocking {
            val summary = importer.importBuiltin(ctx, BuiltInScenario.TRUST_GROOMING)
            summary.callsImported shouldBe 3
            summary.smsImported shouldBe 1
        }
    }

    @Test fun `events anchor relative to fixture anchorAt`() {
        runBlocking {
            importer.importBuiltin(ctx, BuiltInScenario.FAST_ATTACK)
            val calls = repos.calls.listSince(Instant.EPOCH)
            // First call should be at the anchor
            calls.first().startedAt shouldBe Instant.parse("2026-05-08T10:00:00Z")
        }
    }
}
