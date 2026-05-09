package com.qalqan.antifraud.database.log

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import com.qalqan.antifraud.database.RetentionPolicy
import com.qalqan.antifraud.database.RetentionPurger
import io.kotest.matchers.collections.shouldBeEmpty
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ActionLogRetentionTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(ctx)
    private val purger = RetentionPurger(db, RetentionPolicy.DEFAULT)

    @After
    fun close() = db.close()

    @Test
    fun `entries older than 30 days are purged`() {
        runBlocking {
            val now = Instant.parse("2026-05-08T10:00:00Z")
            db.applicationActionLogDao().insert(
                ApplicationActionLogEntity(
                    id = "old",
                    createdAtMs = now.minus(Duration.ofDays(FORTY)).toEpochMilli(),
                    action = "APP_START",
                    detailsJson = "{}",
                ),
            )
            purger.purge(now)
            db.applicationActionLogDao().listRecent(10).shouldBeEmpty()
        }
    }

    private companion object {
        const val FORTY: Long = 40
    }
}
