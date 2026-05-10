package com.qalqan.antifraud.database.patterns

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PatternStateMigrationTest {
    private val db: AntifraudDatabase =
        AntifraudDatabase.inMemory(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `pattern_state table is present in v2 schema`() {
        val cursor = db.openHelper.readableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name='pattern_state'")
        cursor.use {
            it.moveToFirst() shouldBe true
            it.getString(0) shouldBe "pattern_state"
        }
    }
}
