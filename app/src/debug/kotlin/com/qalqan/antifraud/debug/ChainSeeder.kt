package com.qalqan.antifraud.debug

import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.ScenarioCategory
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Debug-only chain seeder. Inserts synthetic [RiskCampaign]s plus linked call/SMS events so the
 * Campaigns ("Рисковые цепочки") list and detail UX can be browsed without the auto call/SMS
 * pipeline. Compiled only into debug builds (lives in `src/debug`), so it never ships in release.
 *
 * Reached two ways, both debug-only: the in-app "Seed demo chains" button on the Campaigns screen
 * (via [DebugHooks.seedDemoChains], registered by DebugInitProvider) and the [DebugSeedReceiver]
 * adb broadcast. Both call [seed] with a live [Repositories]. Clear seeded rows with the existing
 * Privacy "Удалить все данные".
 */
internal object ChainSeeder {
    @Suppress("MagicNumber")
    suspend fun seed(repos: Repositories): Int {
        val now = Instant.now()
        // Top active chain is HIGH (not CRITICAL) so Home does not trigger the §11.5 pause
        // modal that blocks navigation; a CRITICAL chain is still present but CLOSED.
        val specs =
            listOf(
                ChainSpec("seed-high", CampaignStatus.ACTIVE, RiskBand.HIGH, 74, ScenarioCategory.BANK_FRAUD, 0, 2, 1),
                ChainSpec("seed-actmed", CampaignStatus.ACTIVE, RiskBand.MEDIUM, 45, ScenarioCategory.AUTHORITY_SPOOF, 1, 1, 1),
                ChainSpec("seed-crit", CampaignStatus.CLOSED, RiskBand.CRITICAL, 92, ScenarioCategory.INVESTMENT_SCHEME, 3, 1, 1),
                ChainSpec("seed-arch", CampaignStatus.ARCHIVED, RiskBand.LOW, 22, ScenarioCategory.DELIVERY_SCAM, 9, 0, 1),
                ChainSpec("seed-fp", CampaignStatus.FALSE_POSITIVE, RiskBand.MEDIUM, 40, ScenarioCategory.TECH_SUPPORT_SCAM, 5, 1, 1),
            )
        specs.forEach { spec -> seedChain(repos, spec, now) }
        return specs.size
    }

    @Suppress("MagicNumber")
    private suspend fun seedChain(
        repos: Repositories,
        spec: ChainSpec,
        now: Instant,
    ) {
        val start = now.minus(spec.startDaysAgo.toLong(), ChronoUnit.DAYS)
        val eventIds = mutableListOf<EventId>()
        val phoneHashes = mutableSetOf<PhoneHash>()
        val senderHashes = mutableSetOf<SenderHash>()
        var last = start

        for (i in 0 until spec.calls) {
            val id = EventId("${spec.id}-call-$i")
            val phone = PhoneHash("${spec.id}-phone-$i")
            val at = start.plus((i * 10).toLong(), ChronoUnit.MINUTES)
            repos.calls.save(
                CallEvent(
                    id = id,
                    phoneHash = phone,
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = at,
                    endedAt = at.plusSeconds(CALL_DURATION_SEC),
                    durationSec = CALL_DURATION_SEC,
                    isKnownContact = false,
                    isRepeated = i > 0,
                    callRiskScore = spec.score,
                    linkedSessionId = null,
                    linkedCampaignId = CampaignId(spec.id),
                ),
            )
            eventIds += id
            phoneHashes += phone
            last = at
        }

        for (i in 0 until spec.sms) {
            val id = EventId("${spec.id}-sms-$i")
            val sender = SenderHash("${spec.id}-sender-$i")
            val at = start.plus((spec.calls * 10 + i * 5).toLong(), ChronoUnit.MINUTES)
            repos.sms.save(
                SmsEvent(
                    id = id,
                    senderHash = sender,
                    senderDisplayNameLocal = null,
                    simSlot = null,
                    receivedAt = at,
                    smsCategory = SmsCategory.BANK,
                    containsCode = true,
                    containsLink = true,
                    containsFinancialKeyword = true,
                    containsSecurityKeyword = false,
                    bodyExcerptEnc = ByteArray(0),
                    smsRiskScore = spec.score,
                    linkedSessionId = null,
                    linkedCampaignId = CampaignId(spec.id),
                ),
            )
            eventIds += id
            senderHashes += sender
            last = at
        }

        repos.campaigns.save(
            RiskCampaign(
                campaignId = CampaignId(spec.id),
                startedAt = start,
                lastEventAt = last,
                status = spec.status,
                scenarioType = spec.scenario,
                relatedPhoneHashes = phoneHashes,
                relatedSmsSenderHashes = senderHashes,
                relatedDomainHashes = emptySet(),
                relatedEventIds = eventIds,
                relatedSessionIds = emptyList(),
                userAnswerIds = emptyList(),
                triggeredPatternIds = emptyList(),
                campaignRiskScore = spec.score,
                campaignRiskBand = spec.band,
                explanation = "Demo chain (${spec.scenario.name})",
            ),
        )
    }

    @Suppress("LongParameterList")
    private data class ChainSpec(
        val id: String,
        val status: CampaignStatus,
        val band: RiskBand,
        val score: Int,
        val scenario: ScenarioCategory,
        val startDaysAgo: Int,
        val calls: Int,
        val sms: Int,
    )

    private const val CALL_DURATION_SEC = 95L
}
