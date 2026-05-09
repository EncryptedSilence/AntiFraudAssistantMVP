package com.qalqan.antifraud.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldNotBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
    }
}
