package com.qalqan.antifraud.database.manual

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OtpAndIdGuardTest {
    @Test fun `rejects 4 digit code`() {
        OtpAndIdGuard.isLikelySensitive("1234") shouldBe true
    }

    @Test fun `rejects 6 digit code`() {
        OtpAndIdGuard.isLikelySensitive("123456") shouldBe true
    }

    @Test fun `rejects 12 digit Kazakh ID`() {
        OtpAndIdGuard.isLikelySensitive("123456789012") shouldBe true
    }

    @Test fun `rejects 16 digit card`() {
        OtpAndIdGuard.isLikelySensitive("4242424242424242") shouldBe true
    }

    @Test fun `rejects spaced 16 digit card`() {
        OtpAndIdGuard.isLikelySensitive("4242 4242 4242 4242") shouldBe true
    }

    @Test fun `accepts a normal note`() {
        OtpAndIdGuard.isLikelySensitive("Caller said they were from the bank") shouldBe false
    }

    @Test fun `accepts a year`() {
        OtpAndIdGuard.isLikelySensitive("2026") shouldBe true
    } // conservative reject; year shaped like OTP
}
