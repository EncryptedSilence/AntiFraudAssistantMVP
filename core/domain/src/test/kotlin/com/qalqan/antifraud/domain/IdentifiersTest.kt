package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IdentifiersTest {
    @Test
    fun `PhoneHash rejects empty value`() {
        shouldThrow<IllegalArgumentException> { PhoneHash("") }
    }

    @Test
    fun `PhoneHash preserves value`() {
        PhoneHash("abc123").value shouldBe "abc123"
    }

    @Test
    fun `DomainHash rejects whitespace`() {
        shouldThrow<IllegalArgumentException> { DomainHash("   ") }
    }

    @Test
    fun `SenderHash trims and validates`() {
        SenderHash("token").value shouldBe "token"
    }

    @Test
    fun `EventId rejects empty`() {
        shouldThrow<IllegalArgumentException> { EventId("") }
    }
}
