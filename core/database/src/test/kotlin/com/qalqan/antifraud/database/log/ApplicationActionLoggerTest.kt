package com.qalqan.antifraud.database.log

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApplicationActionLoggerTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val logger = repos.actionLogger

    @After
    fun close() = repos.close()

    @Test
    fun `logs an entry with no forbidden keys`() {
        runBlocking {
            logger.log(AppAction.APP_START, mapOf("buildType" to "debug"))
            repos.actionLog.recent(10).single().action shouldBe AppAction.APP_START
        }
    }

    @Test
    fun `rejects forbidden detail keys at the boundary`() {
        runBlocking {
            shouldThrow<IllegalArgumentException> {
                logger.log(AppAction.EXPORT, mapOf("phoneNumber" to "+77001234567"))
            }
        }
    }
}
