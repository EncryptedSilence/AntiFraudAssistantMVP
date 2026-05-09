package com.qalqan.antifraud.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EnumsTest {
    @Test fun `trust statuses include the spec set`() {
        TrustStatus.entries.map { it.name }.toSet() shouldBe setOf(
            "TRUSTED", "NEUTRAL", "SUSPICIOUS", "BLOCKED", "UNKNOWN"
        )
    }

    @Test fun `domain statuses include the spec set`() {
        DomainStatus.entries.map { it.name }.toSet() shouldBe setOf(
            "NEW", "KNOWN", "TRUSTED", "SUSPICIOUS", "BLOCKED", "IGNORED"
        )
    }

    @Test fun `call directions include the spec set`() {
        CallDirection.entries.map { it.name }.toSet() shouldBe setOf(
            "INCOMING", "OUTGOING", "MISSED"
        )
    }

    @Test fun `session statuses match spec section 3 dot 2`() {
        SessionStatus.entries.map { it.name }.toSet() shouldBe setOf(
            "OPEN", "CLOSED_AUTO", "CLOSED_BY_USER", "ARCHIVED", "FALSE_POSITIVE"
        )
    }

    @Test fun `campaign statuses match spec section 16 dot 7`() {
        CampaignStatus.entries.map { it.name }.toSet() shouldBe setOf(
            "ACTIVE", "CLOSED", "ARCHIVED", "FALSE_POSITIVE"
        )
    }
}
