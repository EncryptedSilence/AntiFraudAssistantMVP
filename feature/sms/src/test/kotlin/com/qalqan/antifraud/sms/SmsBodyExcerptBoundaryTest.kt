package com.qalqan.antifraud.sms

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

/**
 * Spec §16.3 / §2.1 — body excerpt at rest must NEVER exceed 200 plaintext chars (≤512
 * bytes after AEAD). This is the privacy-boundary test for the auto-capture path; the
 * manual path is already covered in :core:database. Adding the explicit test for the
 * auto path closes the §2.1 hard rule for Stage 4.
 */
@RunWith(RobolectricTestRunner::class)
class SmsBodyExcerptBoundaryTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)
    private val builder = SmsEventBuilder(digest, box)

    @Test
    fun `200-char body is accepted at the cap`() {
        val body = "a".repeat(SmsEvent.MAX_BODY_EXCERPT_CHARS)
        val ev = builder.build(SmsBroadcast("S", body, Instant.now(), null))
        ev.bodyExcerptEnc.size shouldBeLessThanOrEqual SmsEvent.MAX_BODY_EXCERPT_BYTES
    }

    @Test
    fun `1000-char body is silently truncated -- cipher stays within the AEAD cap`() {
        val body = "x".repeat(1000)
        val ev = builder.build(SmsBroadcast("S", body, Instant.now(), null))
        ev.bodyExcerptEnc.size shouldBeLessThanOrEqual SmsEvent.MAX_BODY_EXCERPT_BYTES
    }

    @Test
    fun `empty body is accepted`() {
        val ev = builder.build(SmsBroadcast("S", "", Instant.now(), null))
        ev.bodyExcerptEnc.size shouldBeLessThanOrEqual SmsEvent.MAX_BODY_EXCERPT_BYTES
        // Sanity: an empty body still encrypts (AEAD ciphertext > 0 due to nonce + tag).
        (ev.bodyExcerptEnc.isNotEmpty()) shouldBe true
    }
}
