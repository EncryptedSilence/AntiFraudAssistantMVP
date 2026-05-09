package com.qalqan.antifraud.database.log

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.AntifraudDatabase
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApplicationActionLogDaoTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val db = AntifraudDatabase.inMemory(ctx)
    private val dao = db.applicationActionLogDao()

    @After
    fun close() = db.close()

    @Test
    fun `listRecent returns rows newest first`() =
        runBlocking {
            dao.insert(ApplicationActionLogEntity("l1", 1_000, "APP_START", "{}"))
            dao.insert(ApplicationActionLogEntity("l2", 2_000, "APP_STOP", "{}"))
            val rows = dao.listRecent(10)
            rows shouldHaveSize 2
            check(rows[0].id == "l2") { "expected newest first" }
        }
}
