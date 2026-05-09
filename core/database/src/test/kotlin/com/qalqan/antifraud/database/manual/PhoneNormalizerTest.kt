package com.qalqan.antifraud.database.manual

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PhoneNormalizerTest {
    private val kz = PhoneNormalizer(defaultCountryCode = 7)

    @Test fun `Kazakh number with leading 8 normalizes to plus 7 prefix`() {
        val result = kz.normalize("87001234567")
        result.normalizedE164 shouldBe "+77001234567"
        result.last4 shouldBe "4567"
        result.isShortCode shouldBe false
    }

    @Test fun `formatted number with spaces and dashes normalizes`() {
        val result = kz.normalize("+7 (700) 123-45-67")
        result.normalizedE164 shouldBe "+77001234567"
    }

    @Test fun `short code 1414 is preserved as-is`() {
        val result = kz.normalize("1414")
        result.normalizedE164 shouldBe "1414"
        result.last4 shouldBe null
        result.isShortCode shouldBe true
    }

    @Test fun `empty input throws`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> { kz.normalize("") }
    }
}
