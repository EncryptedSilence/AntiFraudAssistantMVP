package com.qalqan.antifraud.database.patterns

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PatternStateRepositoryTest {
    private val db: AntifraudDatabase =
        AntifraudDatabase.inMemory(ApplicationProvider.getApplicationContext())
    private val repo = PatternStateRepository(db.patternStateDao())
    private val now = Instant.parse("2026-05-08T10:00:00Z")

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `setEnabled persists the override`() {
        runBlocking {
            repo.setEnabled("p1", enabled = false, at = now)
            repo.isEnabled("p1", default = true) shouldBe false
        }
    }

    @Test
    fun `isEnabled falls back to default when no override`() {
        runBlocking {
            repo.isEnabled("never_seen", default = true) shouldBe true
            repo.isEnabled("never_seen", default = false) shouldBe false
        }
    }

    @Test
    fun `resetToDefaults wipes overrides`() {
        runBlocking {
            repo.setEnabled("p1", enabled = false, at = now)
            repo.resetToDefaults()
            repo.isEnabled("p1", default = true) shouldBe true
        }
    }
}
