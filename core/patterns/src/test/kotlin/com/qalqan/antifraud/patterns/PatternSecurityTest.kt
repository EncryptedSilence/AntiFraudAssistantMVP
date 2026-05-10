package com.qalqan.antifraud.patterns

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

/**
 * Spec §6.4 — patterns must not contain executable code or perform I/O.
 * Stage 2's parser rejects unknown operators and unsupported event types,
 * so any attempt to inject an executable hook (e.g. operator: "exec") fails
 * at parse time before the matcher sees it. This test pins that property.
 */
class PatternSecurityTest {
    @Test
    fun `parser rejects executable operator hook`() {
        val json =
            """
            {
              "patternId": "p", "name": "p", "category": "bankFraud",
              "version": "1.0.0", "enabled": true, "source": "system",
              "conditions": [
                { "eventType": "CallEvent", "field": "isKnownContact", "operator": "exec", "value": "rm -rf /", "weight": 10 }
              ],
              "warning": { "level": "medium", "title": "t", "message": "m" }
            }
            """.trimIndent()

        shouldThrow<PatternParseException> {
            PatternCatalogParser.fromJson(json)
        }
    }

    @Test
    fun `pattern with unknown top-level field still parses required fields`() {
        // additionalProperties=false is NOT enforced (Moshi reflective ignores unknown
        // top-level keys), but the type system makes any unknown field structurally
        // unreachable on the parsed ScenarioPattern. The §6.4 safety: a pattern is
        // purely declarative.
        val json =
            """
            {
              "patternId": "p", "name": "p", "category": "bankFraud",
              "version": "1.0.0", "enabled": true, "source": "system",
              "execute": "rm -rf /",
              "conditions": [
                { "eventType": "CallEvent", "field": "isKnownContact", "operator": "equals", "value": false, "weight": 10 }
              ],
              "warning": { "level": "medium", "title": "t", "message": "m" }
            }
            """.trimIndent()

        // Unknown "execute" field is silently ignored by Moshi. Compile-time check:
        // ScenarioPattern has no `execute` property, so nothing executable can be
        // surfaced from the parsed object.
        PatternCatalogParser.fromJson(json) // should succeed without throwing
    }
}
