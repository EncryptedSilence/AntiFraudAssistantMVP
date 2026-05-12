package com.qalqan.antifraud.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExportGathererDispatchTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `empty repositories return an empty record list for any single-category request`() {
        val gatherer = ExportGatherer.default(context)
        ExportCategory.entries.forEach { category ->
            val request = ExportRequest(setOf(category), ExportFormat.JSON)
            runBlocking {
                val records = gatherer.gather(request, repos)
                records.size shouldBe 0
            }
        }
    }

    @Test
    fun `multi-category request combines the per-category gatherer results`() {
        val gatherer = ExportGatherer.default(context)
        val request = ExportRequest(ExportCategory.entries.toSet(), ExportFormat.JSON)
        runBlocking {
            val records = gatherer.gather(request, repos)
            records.size shouldBe 0
        }
    }
}
