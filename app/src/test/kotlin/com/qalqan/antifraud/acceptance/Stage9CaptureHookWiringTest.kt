package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.AlertWiring
import com.qalqan.antifraud.calls.CallObserverService
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.sms.SmsBroadcastReceiver
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Stage9CaptureHookWiringTest {
    @Test
    fun `installed CallObserverService capture hook produces a new pipeline per call`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val repos = Repositories.inMemory(ctx)
        try {
            AlertWiring.installInto(ctx)
            val factory = CallObserverService.captureHookFactory
            val hookA = factory(ctx, repos)
            val hookB = factory(ctx, repos)
            hookA shouldNotBe hookB
        } finally {
            repos.close()
        }
    }

    @Test
    fun `installed SmsBroadcastReceiver capture hook produces a new pipeline per call`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val repos = Repositories.inMemory(ctx)
        try {
            AlertWiring.installInto(ctx)
            val factory = SmsBroadcastReceiver.captureHookFactory
            val hookA = factory(ctx, repos)
            val hookB = factory(ctx, repos)
            hookA shouldNotBe hookB
        } finally {
            repos.close()
        }
    }
}
