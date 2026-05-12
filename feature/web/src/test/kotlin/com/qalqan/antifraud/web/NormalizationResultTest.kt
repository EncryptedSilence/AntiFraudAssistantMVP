package com.qalqan.antifraud.web

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class NormalizationResultTest {
    @Test
    fun `Success holds the canonical eTLD plus 1`() {
        val r = NormalizationResult.Success(canonical = "halykbank.kz")
        r.canonical shouldBe "halykbank.kz"
    }

    @Test
    fun `Error variants are distinguishable`() {
        val empty = NormalizationResult.Error.Empty
        val invalid = NormalizationResult.Error.Invalid("garbage")
        empty.shouldBeInstanceOf<NormalizationResult.Error>()
        invalid.shouldBeInstanceOf<NormalizationResult.Error>()
        invalid.input shouldBe "garbage"
    }
}
