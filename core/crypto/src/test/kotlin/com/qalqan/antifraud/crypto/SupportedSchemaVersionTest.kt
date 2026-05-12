package com.qalqan.antifraud.crypto

import io.kotest.matchers.shouldBe
import org.junit.Test

class SupportedSchemaVersionTest {
    @Test
    fun `CURRENT is 1`() {
        SupportedSchemaVersion.CURRENT shouldBe 1
    }

    @Test
    fun `version 1 is supported`() {
        SupportedSchemaVersion.isSupported(1) shouldBe true
    }

    @Test
    fun `version 0 is not supported`() {
        SupportedSchemaVersion.isSupported(0) shouldBe false
    }

    @Test
    fun `version 2 is not supported in MVP`() {
        SupportedSchemaVersion.isSupported(2) shouldBe false
    }

    @Test
    fun `negative version is not supported`() {
        SupportedSchemaVersion.isSupported(-1) shouldBe false
    }
}
