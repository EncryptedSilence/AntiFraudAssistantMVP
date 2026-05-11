package com.qalqan.antifraud.sms

import com.qalqan.antifraud.domain.SmsCategory
import io.kotest.matchers.shouldBe
import org.junit.Test

class SmsCategoryClassifierTest {
    @Test
    fun `authority short code maps to AUTHORITY_SHORTCODE`() {
        SmsCategoryClassifier.classify("1414", "Hello citizen") shouldBe SmsCategory.AUTHORITY_SHORTCODE
    }

    @Test
    fun `body with financial keywords on a non-short-code maps to BANK`() {
        SmsCategoryClassifier.classify("HALYKBANK", "Перевод на сумму 5000 KZT") shouldBe SmsCategory.BANK
    }

    @Test
    fun `body with security-domain keywords maps to SECURITY_WARNING`() {
        SmsCategoryClassifier.classify("Google", "Suspicious sign-in detected") shouldBe SmsCategory.SECURITY_WARNING
    }

    @Test
    fun `body containing a numeric code maps to OTP when not from short code or bank`() {
        SmsCategoryClassifier.classify("Unknown", "Your code is 123456") shouldBe SmsCategory.OTP
    }

    @Test
    fun `body containing a link with no other signal maps to LINK`() {
        SmsCategoryClassifier.classify("Unknown", "Visit https://example.com") shouldBe SmsCategory.LINK
    }

    @Test
    fun `unrecognized sender with plain text maps to UNKNOWN_SENDER`() {
        SmsCategoryClassifier.classify("Unknown", "Hi there!") shouldBe SmsCategory.UNKNOWN_SENDER
    }
}
