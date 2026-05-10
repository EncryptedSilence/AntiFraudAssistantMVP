package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.ScenarioCategory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class PatternCatalogParserTest {
    @Test
    fun `parses Appendix A example`() {
        val json = """
            {
              "patternId": "bank_security_otp_after_call_v1",
              "name": "Bank security service / OTP after a call",
              "category": "bankFraud",
              "version": "1.0.0",
              "enabled": true,
              "userCreated": false,
              "source": "system",
              "conditions": [
                { "eventType": "CallEvent", "field": "isKnownContact", "operator": "equals", "value": false, "weight": 20 },
                { "eventType": "SmsEvent", "field": "containsCode", "operator": "equals", "value": true, "timeWindowHours": 24, "weight": 30 }
              ],
              "correlation": { "maxCampaignAgeDays": 14, "linkStrength": 0.9 },
              "warning": { "level": "high", "title": "Possible fraud scheme", "message": "Do not share the code." }
            }
        """.trimIndent()

        val pattern = PatternCatalogParser.fromJson(json)

        pattern.patternId.value shouldBe "bank_security_otp_after_call_v1"
        pattern.category shouldBe ScenarioCategory.BANK_FRAUD
        pattern.conditions.size shouldBe 2
        pattern.conditions[1].timeWindowHours shouldBe 24
        pattern.warning.level shouldBe WarningLevel.HIGH
    }

    @Test
    fun `parses a list of patterns`() {
        val json = """
            [
              {
                "patternId": "p1", "name": "p1", "category": "bankFraud",
                "version": "1.0.0", "enabled": true, "source": "system",
                "conditions": [
                  { "eventType": "CallEvent", "field": "isKnownContact", "operator": "equals", "value": false, "weight": 10 }
                ],
                "warning": { "level": "medium", "title": "t", "message": "m" }
              },
              {
                "patternId": "p2", "name": "p2", "category": "deliveryScam",
                "version": "1.0.0", "enabled": false, "source": "system",
                "conditions": [
                  { "eventType": "SmsEvent", "field": "containsLink", "operator": "equals", "value": true, "weight": 20 }
                ],
                "warning": { "level": "high", "title": "t", "message": "m" }
              }
            ]
        """.trimIndent()

        val patterns = PatternCatalogParser.listFromJson(json)
        patterns.size shouldBe 2
        patterns[0].patternId.value shouldBe "p1"
        patterns[1].enabled shouldBe false
    }

    @Test
    fun `rejects unknown operator`() {
        val json = """
            {
              "patternId": "p", "name": "p", "category": "bankFraud",
              "version": "1.0.0", "enabled": true, "source": "system",
              "conditions": [
                { "eventType": "SmsEvent", "field": "x", "operator": "regex", "value": "y", "weight": 10 }
              ],
              "warning": { "level": "medium", "title": "t", "message": "m" }
            }
        """.trimIndent()

        val ex = io.kotest.assertions.throwables.shouldThrow<PatternParseException> {
            PatternCatalogParser.fromJson(json)
        }
        ex.message!! shouldContain "unknown operator 'regex'"
    }

    @Test
    fun `rejects unsupported event type`() {
        val json = """
            {
              "patternId": "p", "name": "p", "category": "bankFraud",
              "version": "1.0.0", "enabled": true, "source": "system",
              "conditions": [
                { "eventType": "ContactEvent", "field": "x", "operator": "equals", "value": "y", "weight": 10 }
              ],
              "warning": { "level": "medium", "title": "t", "message": "m" }
            }
        """.trimIndent()

        val ex = io.kotest.assertions.throwables.shouldThrow<PatternParseException> {
            PatternCatalogParser.fromJson(json)
        }
        ex.message!! shouldContain "ContactEvent"
        ex.message!! shouldContain "not supported in Stage 2"
    }
}
