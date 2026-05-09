package com.qalqan.antifraud.database.log

import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.domain.ApplicationActionLogEntry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class ApplicationActionLogMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `round trip preserves details json`() {
        val entry =
            ApplicationActionLogEntry(
                id = "l1",
                createdAt = t,
                action = AppAction.SYNC_VERIFY_OK,
                details = mapOf("packageVersion" to "1.0.3"),
            )
        entry.toEntity().toDomain() shouldBe entry
    }
}
