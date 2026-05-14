package com.qalqan.antifraud.alerts

import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import java.time.Duration
import java.time.Instant

class CampaignCooldownTest {
    @After
    fun clear() {
        CampaignCooldown.clearAllForTest()
    }

    @Test
    fun `first two alerts allowed within 24 h`() {
        val now = Instant.parse("2026-05-14T12:00:00Z")
        CampaignCooldown.allow("c1", now) shouldBe true
        CampaignCooldown.allow("c1", now.plus(Duration.ofMinutes(SHORT_OFFSET))) shouldBe true
    }

    @Test
    fun `third alert within 24 h denied`() {
        val now = Instant.parse("2026-05-14T12:00:00Z")
        CampaignCooldown.allow("c1", now)
        CampaignCooldown.allow("c1", now.plus(Duration.ofHours(1)))
        CampaignCooldown.allow("c1", now.plus(Duration.ofHours(2))) shouldBe false
    }

    @Test
    fun `expires after 24 h`() {
        val now = Instant.parse("2026-05-14T12:00:00Z")
        CampaignCooldown.allow("c1", now)
        CampaignCooldown.allow("c1", now.plus(Duration.ofMinutes(SHORT_OFFSET)))
        CampaignCooldown.allow("c1", now.plus(Duration.ofHours(OFFSET_AFTER_WINDOW))) shouldBe true
    }

    @Test
    fun `independent per campaign`() {
        val now = Instant.parse("2026-05-14T12:00:00Z")
        CampaignCooldown.allow("c1", now)
        CampaignCooldown.allow("c1", now)
        CampaignCooldown.allow("c2", now) shouldBe true
    }

    private companion object {
        const val SHORT_OFFSET = 10L
        const val OFFSET_AFTER_WINDOW = 25L
    }
}
