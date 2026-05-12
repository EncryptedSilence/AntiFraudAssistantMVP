package com.qalqan.antifraud.export

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

@RunWith(RobolectricTestRunner::class)
class RepositoriesExportProfileWipeTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `wipeAll deletes every export_profile row (§23 #20)`() {
        val entity =
            ExportProfileEntity(
                exportId = "e-1",
                createdAt = 1700000000000L,
                exportType = "risk_campaigns",
                includedCategories = "risk_campaigns",
                anonymizationLevel = "",
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
