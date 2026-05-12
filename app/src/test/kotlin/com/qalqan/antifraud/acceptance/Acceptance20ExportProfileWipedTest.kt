package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.export.ExportProfileEntity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #20 — one tap deletes all DB content. Stage 6 extended `Repositories.wipeAll(...)`
 * to clear the synced bundle store; Stage 7 extends it to clear `export_profile` rows.
 * The acceptance test inserts a few rows, calls `wipeAll(...)`, and asserts every table
 * is empty.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance20ExportProfileWipedTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() { repos.close() }

    @Test
    fun `§23 #20 — wipeAll drops every export_profile row alongside the rest of the DB`() {
        val entity = ExportProfileEntity(
            exportId = "e-1",
            createdAt = 1700000000000L,
            exportType = "risk_campaigns",
            includedCategories = "risk_campaigns",
            anonymizationLevel = "numbers_last_4",
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
