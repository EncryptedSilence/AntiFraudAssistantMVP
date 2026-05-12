package com.qalqan.antifraud.database.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ExportProfileRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `insert then findById round-trips an ExportProfileEntity`() {
        val entity =
            ExportProfileEntity(
                exportId = "e-1",
                createdAt = Instant.parse("2026-05-12T10:00:00Z").toEpochMilli(),
                exportType = "risk_campaigns,suspicious_numbers",
                includedCategories = "risk_campaigns,suspicious_numbers",
                anonymizationLevel = "numbers_last_4",
                format = "JSON",
                userConfirmed = true,
                redactionPreviewShown = true,
            )
        runBlocking {
            repos.exportProfiles.insert(entity)
            val loaded = repos.exportProfiles.findById("e-1")
            loaded shouldBe entity
        }
    }

    @Test
    fun `wipeAll removes every export_profile row`() {
        val entity =
            ExportProfileEntity(
                exportId = "e-1",
                createdAt = 1700000000000L,
                exportType = "risk_campaigns",
                includedCategories = "risk_campaigns",
                anonymizationLevel = "",
                format = "JSON",
                userConfirmed = true,
                redactionPreviewShown = true,
            )
        runBlocking {
            repos.exportProfiles.insert(entity)
            repos.exportProfiles.count() shouldBe 1
            repos.wipeAll()
            repos.exportProfiles.count() shouldBe 0
        }
    }
}
