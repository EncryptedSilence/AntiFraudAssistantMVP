@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.acceptance

import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.telephony.SubscriptionManager
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.SimEnumerator
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.sms.AutoSmsCapture
import com.qalqan.antifraud.sms.SmsContentProviderReader
import com.qalqan.antifraud.sms.SmsContentProviderSweeper
import com.qalqan.antifraud.sms.SmsEventBuilder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboCursor
import org.robolectric.shadows.ShadowSubscriptionManager
import java.time.Instant

/**
 * Spec §23 #31 — :app integration: a dual-SIM sweep produces SmsEvents whose simSlot
 * matches each subscription's slot index.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class Acceptance31MultiSimSmsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `slot 0 and slot 1 each propagate into the persisted SmsEvent`() {
        val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        shadowOf(sm).setActiveSubscriptionInfoList(
            listOf(
                ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                    .setId(1).setSimSlotIndex(0).buildSubscriptionInfo(),
                ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                    .setId(2).setSimSlotIndex(1).buildSubscriptionInfo(),
            ),
        )

        val cursor =
            RoboCursor().apply {
                setColumnNames(SmsContentProviderReader.PROJECTION.toList())
                setResults(
                    arrayOf(
                        arrayOf<Any?>("S1", "B1", 1L, 1, 1L),
                        arrayOf<Any?>("S2", "B2", 2L, 2, 2L),
                    ),
                )
            }
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)

        runBlocking {
            SmsContentProviderSweeper(
                reader = SmsContentProviderReader(context.contentResolver),
                capture = AutoSmsCapture(SmsEventBuilder(digest, box), repos.sms),
                sims = SimEnumerator(context),
            ).sweepSince(0L)

            val events = repos.sms.listSince(Instant.EPOCH).sortedBy { it.receivedAt }
            events[0].simSlot shouldBe 0
            events[1].simSlot shouldBe 1
        }
    }
}
