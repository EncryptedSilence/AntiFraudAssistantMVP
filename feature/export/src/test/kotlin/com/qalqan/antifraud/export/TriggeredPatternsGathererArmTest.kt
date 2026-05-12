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
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class TriggeredPatternsGathererArmTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `empty pattern_state emits no records`() {
        val arm = TriggeredPatternsGathererArm(context)
        runBlocking {
            arm.gather(repos).size shouldBe 0
        }
    }

    @Test
    fun `emits a record per pattern with a non-null lastTriggeredAt`() {
        val arm = TriggeredPatternsGathererArm(context)
        runBlocking {
            repos.patternState.recordTrigger(
                patternId = "authority_spoof_call_v1",
                triggeredAt = Instant.parse("2026-05-02T11:00:00Z"),
            )
            val records = arm.gather(repos)
            records.size shouldBe 1
            val r = records.first() as ExportRecord.TriggeredPattern
            r.patternId shouldBe "authority_spoof_call_v1"
            // §16.8 ScenarioPattern.name comes from the seed catalog (in-APK JSON).
            r.name shouldBe "Authority impersonation by phone"
            r.timesTriggered shouldBe 1
        }
    }
}
