package com.qalqan.antifraud.demo

import android.content.Context
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.domain.CallDirection

enum class BuiltInScenario(val assetPath: String) {
    FAST_ATTACK("demo/fast-attack.json"),
    SMS_THEN_CALL("demo/sms-then-call.json"),
    TRUST_GROOMING("demo/trust-grooming.json"),
}

data class ImportSummary(
    val callsImported: Int,
    val smsImported: Int,
    val webImported: Int,
)

class DemoImporter(private val manual: ManualEntry) {
    suspend fun importBuiltin(
        context: Context,
        scenario: BuiltInScenario,
    ): ImportSummary {
        val json = context.assets.open(scenario.assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return importFromJson(json)
    }

    suspend fun importFromJson(json: String): ImportSummary {
        val fixture = DemoFixture.fromJson(json)
        var calls = 0
        var sms = 0
        var web = 0
        fixture.events.forEach { ev ->
            val at = fixture.anchorAt.plusSeconds(ev.offsetSeconds)
            when (ev) {
                is DemoEvent.Call -> {
                    manual.calls.submit(
                        rawNumber = ev.rawNumber,
                        direction = CallDirection.valueOf(ev.direction),
                        startedAt = at,
                        durationSec = ev.durationSec,
                        isKnownContact = ev.isKnownContact,
                    )
                    calls++
                }
                is DemoEvent.Sms -> {
                    manual.sms.submit(
                        sender = ev.sender,
                        receivedAt = at,
                        body = ev.body,
                    )
                    sms++
                }
                is DemoEvent.Web -> {
                    manual.web.submit(
                        domainEtldPlusOne = ev.domain,
                        visitedAt = at,
                    )
                    web++
                }
            }
        }
        return ImportSummary(calls, sms, web)
    }
}
