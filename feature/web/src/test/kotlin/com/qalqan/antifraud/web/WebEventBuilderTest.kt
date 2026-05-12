package com.qalqan.antifraud.web

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.domain.DomainStatus
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class WebEventBuilderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val digest = WebEntryDigest.create(context, InMemoryCryptoBox())
    private val builder = WebEventBuilder(digest)

    @Test
    fun `build composes the expected WebEvent fields`() {
        val now = Instant.parse("2026-05-12T10:15:30Z")
        val ev =
            builder.build(
                canonical = "halykbank.kz",
                visitedAt = now,
                isNew = true,
                status = DomainStatus.NEW,
            )

        ev.domainDisplayLocal shouldBe "halykbank.kz"
        ev.visitedAt shouldBe now
        ev.isNewDomain shouldBe true
        ev.domainStatus shouldBe DomainStatus.NEW
        ev.webRiskScore shouldBe 0
        ev.linkedSessionId shouldBe null
        ev.linkedCampaignId shouldBe null
    }

    @Test
    fun `domainHash matches digest hash of canonical`() {
        val ev =
            builder.build(
                canonical = "kaspi.kz",
                visitedAt = Instant.now(),
                isNew = false,
                status = DomainStatus.KNOWN,
            )
        ev.domainHash.value shouldBe digest.hash("kaspi.kz")
    }
}
