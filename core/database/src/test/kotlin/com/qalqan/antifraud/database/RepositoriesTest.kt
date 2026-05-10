package com.qalqan.antifraud.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class RepositoriesTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)

    @After
    fun close() = repos.close()

    @Test
    fun `factory exposes all repositories`() {
        repos.calls shouldNotBe null
        repos.sms shouldNotBe null
        repos.web shouldNotBe null
        repos.answers shouldNotBe null
        repos.sessions shouldNotBe null
        repos.campaigns shouldNotBe null
        repos.contacts shouldNotBe null
        repos.patternState shouldNotBe null
    }

    @Test
    fun `patternState accessor returns a working repository`() {
        runBlocking {
            repos.patternState.setEnabled("p1", enabled = false, at = Instant.now())
            repos.patternState.isEnabled("p1", default = true) shouldBe false
        }
    }
}
