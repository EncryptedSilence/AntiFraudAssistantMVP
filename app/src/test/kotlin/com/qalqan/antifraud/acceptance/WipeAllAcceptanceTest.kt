package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.demo.DemoImporter
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.collections.shouldBeEmpty
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WipeAllAcceptanceTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val manual = ManualEntry.create(ctx, repos, InMemoryCryptoBox())
    private val importer = DemoImporter(manual)

    @After
    fun close() = repos.close()

    // Inline fixture (mirrors :core:demo/fast-attack.json). Robolectric in :app cannot reach the
    // library module's assets directory, so we feed the JSON directly via DemoImporter.importFromJson.
    private val fastAttackJson =
        """
        {
          "name": "Fast attack",
          "specReference": "§13.1",
          "anchorAt": "2026-05-08T10:00:00Z",
          "events": [
            {
              "type": "Call",
              "rawNumber": "+77001234567",
              "direction": "INCOMING",
              "offsetSeconds": 0,
              "durationSec": 240,
              "isKnownContact": false
            },
            {
              "type": "Sms",
              "sender": "BANK24",
              "offsetSeconds": 360,
              "body": "Halyk: code 482917 — do not share. Call us back at 7575 for help."
            }
          ]
        }
        """.trimIndent()

    @Test
    fun `wipeAll empties calls, sms, web, sessions, campaigns, contacts, and action log (spec §23 #20)`() {
        runBlocking {
            importer.importFromJson(fastAttackJson)
            repos.actionLogger.log(AppAction.APP_START)

            repos.wipeAll()

            repos.calls.listSince(Instant.EPOCH).shouldBeEmpty()
            repos.sms.listSince(Instant.EPOCH).shouldBeEmpty()
            repos.actionLog.recent(50).shouldBeEmpty()
        }
    }
}
