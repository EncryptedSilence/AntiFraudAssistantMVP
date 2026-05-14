package com.qalqan.antifraud.alerts

import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import java.time.Duration
import java.time.Instant

class DismissalCooldownTest {
    @After
    fun clear() {
        DismissalCooldown.clearAllForTest()
    }

    @Test
    fun `not cooling down by default`() {
        DismissalCooldown.isCoolingDown("c1", now = Instant.parse("2026-05-14T12:00:00Z")) shouldBe false
    }

    @Test
    fun `still cooling down at 4 min 59 s`() {
        val t0 = Instant.parse("2026-05-14T12:00:00Z")
        DismissalCooldown.record("c1", at = t0)
        val almost = t0.plus(Duration.ofSeconds(SECONDS_BEFORE_EXPIRY))
        DismissalCooldown.isCoolingDown("c1", now = almost) shouldBe true
    }

    @Test
    fun `expires after 5 min`() {
        val t0 = Instant.parse("2026-05-14T12:00:00Z")
        DismissalCooldown.record("c1", at = t0)
        val after = t0.plus(Duration.ofSeconds(SECONDS_AFTER_EXPIRY))
        DismissalCooldown.isCoolingDown("c1", now = after) shouldBe false
    }

    @Test
    fun `independent per campaign`() {
        val t0 = Instant.parse("2026-05-14T12:00:00Z")
        DismissalCooldown.record("c1", at = t0)
        DismissalCooldown.isCoolingDown("c2", now = t0) shouldBe false
    }

    private companion object {
        const val SECONDS_BEFORE_EXPIRY = 299L
        const val SECONDS_AFTER_EXPIRY = 301L
    }
}
