package com.qalqan.antifraud.ui.references

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ReferencesViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        ReferencesViewModel(
            application = context.applicationContext as Application,
            repos = repos,
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        repos.close()
    }

    private fun await() {
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            if (!viewModel.state.value.isLoading && viewModel.state.value.smsCategories.isNotEmpty()) return
            Thread.sleep(5)
        }
    }

    @Test
    fun `loads all SmsCategory enum values into smsCategories`() {
        viewModel.refresh()
        await()
        viewModel.state.value.smsCategories.size shouldBe SmsCategory.entries.size
    }

    @Test
    fun `loads lookalike seed domains into suspiciousDomains`() {
        viewModel.refresh()
        await()
        val seeded = LookalikeSeedCatalog.seeds.toList()
        viewModel.state.value.suspiciousDomains shouldContainAll seeded
    }
}
