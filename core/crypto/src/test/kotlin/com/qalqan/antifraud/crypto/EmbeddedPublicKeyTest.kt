package com.qalqan.antifraud.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmbeddedPublicKeyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `loads 32 bytes from R_raw_sync_public_key`() {
        val key = EmbeddedPublicKey.load(context)
        key.size shouldBe Ed25519SignatureVerifier.ED25519_PUBLIC_KEY_BYTES
    }
}
