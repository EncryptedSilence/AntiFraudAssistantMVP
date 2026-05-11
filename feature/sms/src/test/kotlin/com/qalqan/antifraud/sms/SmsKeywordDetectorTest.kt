package com.qalqan.antifraud.sms

import io.kotest.matchers.shouldBe
import org.junit.Test

class SmsKeywordDetectorTest {

    @Test
    fun `containsFinancialKeyword fires on common KZ financial terms`() {
        SmsKeywordDetector.containsFinancialKeyword("Перевод на сумму 5000 KZT") shouldBe true
        SmsKeywordDetector.containsFinancialKeyword("Списание с карты") shouldBe true
        SmsKeywordDetector.containsFinancialKeyword("Halyk Bank: transfer received") shouldBe true
        SmsKeywordDetector.containsFinancialKeyword("Hello, how are you?") shouldBe false
    }

    @Test
    fun `containsSecurityKeyword fires on security-domain terms`() {
        SmsKeywordDetector.containsSecurityKeyword("Подозрительная активность") shouldBe true
        SmsKeywordDetector.containsSecurityKeyword("Введите код безопасности") shouldBe true
        SmsKeywordDetector.containsSecurityKeyword("Suspicious sign-in detected") shouldBe true
        SmsKeywordDetector.containsSecurityKeyword("Lunch at 1?") shouldBe false
    }

    @Test
    fun `isAuthorityShortCode recognizes the §5_1 short codes`() {
        SmsKeywordDetector.isAuthorityShortCode("1414") shouldBe true
        SmsKeywordDetector.isAuthorityShortCode("112") shouldBe true
        SmsKeywordDetector.isAuthorityShortCode("+71234567890") shouldBe false
        SmsKeywordDetector.isAuthorityShortCode("HALYKBANK") shouldBe false
    }

    @Test
    fun `containsLink fires only on http or https schemes`() {
        SmsKeywordDetector.containsLink("Click https://example.com") shouldBe true
        SmsKeywordDetector.containsLink("Visit http://t.me/foo") shouldBe true
        SmsKeywordDetector.containsLink("Plain text only") shouldBe false
    }
}
