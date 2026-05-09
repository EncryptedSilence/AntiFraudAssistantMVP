package com.qalqan.antifraud.demo

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DemoFixtureTest {
    private val json = """
        {
          "name": "Fast attack",
          "specReference": "§13.1",
          "anchorAt": "2026-05-08T10:00:00Z",
          "events": [
            { "type": "Call", "rawNumber": "+1", "direction": "INCOMING", "offsetSeconds": 0, "durationSec": 60, "isKnownContact": false },
            { "type": "Sms",  "sender": "BANK", "offsetSeconds": 60, "body": "code" }
          ]
        }
    """.trimIndent()

    @Test fun `parses Call and Sms variants`() {
        val fixture = DemoFixture.fromJson(json)
        fixture.name shouldBe "Fast attack"
        fixture.events shouldHaveSize 2
        check(fixture.events[0] is DemoEvent.Call)
        check(fixture.events[1] is DemoEvent.Sms)
    }
}
