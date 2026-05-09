package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class ApplicationActionLogTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `details map cannot contain forbidden keys`() {
        shouldThrow<IllegalArgumentException> {
            ApplicationActionLogEntry(
                id = "l1",
                createdAt = t,
                action = AppAction.EXPORT,
                details = mapOf("phoneNumber" to "+77001234567")
            )
        }
        shouldThrow<IllegalArgumentException> {
            ApplicationActionLogEntry(
                id = "l1",
                createdAt = t,
                action = AppAction.EXPORT,
                details = mapOf("smsBody" to "your code is 1234")
            )
        }
    }

    @Test fun `valid log entry accepted`() {
        ApplicationActionLogEntry(
            id = "l1",
            createdAt = t,
            action = AppAction.SYNC_VERIFY_OK,
            details = mapOf("packageVersion" to "1.0.3")
        ).action shouldBe AppAction.SYNC_VERIFY_OK
    }
}
