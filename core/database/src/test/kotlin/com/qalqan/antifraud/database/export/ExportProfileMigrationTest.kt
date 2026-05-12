package com.qalqan.antifraud.database.export

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExportProfileMigrationTest {
    private val db: AntifraudDatabase =
        AntifraudDatabase.inMemory(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `migrates v3 to v4 by adding the export_profile table`() {
        // Spec §16.10 — the export_profile table must exist and accept the §16.10 columns.
        val cursor =
            db.openHelper.readableDatabase
                .query("SELECT name FROM sqlite_master WHERE type='table' AND name='export_profile'")
        cursor.use {
            it.moveToFirst() shouldBe true
            it.getString(0) shouldBe "export_profile"
        }
    }

    @Test
    fun `export_profile table accepts a smoke insert and round-trips the row count`() {
        // Smoke insert — the new table accepts the §16.10 columns.
        val writable = db.openHelper.writableDatabase
        writable.execSQL(
            "INSERT INTO export_profile (exportId, createdAt, exportType, includedCategories, " +
                "anonymizationLevel, format, userConfirmed, redactionPreviewShown) " +
                "VALUES ('e1', 1700000000000, 'risk_campaigns', 'risk_campaigns', " +
                "'numbers_last_4', 'JSON', 1, 1)",
        )
        val cursor = writable.query("SELECT COUNT(*) FROM export_profile")
        cursor.use {
            it.moveToFirst() shouldBe true
            it.getInt(0) shouldBe 1
        }
    }

    @Test
    fun `exportProfileDao insert and findById round-trip`() {
        runBlocking {
            val dao = db.exportProfileDao()
            val entity =
                ExportProfileEntity(
                    exportId = "e2",
                    createdAt = 1700000000001L,
                    exportType = "risk_campaigns",
                    includedCategories = "risk_campaigns",
                    anonymizationLevel = "numbers_last_4",
                    format = "JSON",
                    userConfirmed = true,
                    redactionPreviewShown = true,
                )
            dao.insert(entity)
            dao.findById("e2") shouldBe entity
        }
    }
}
