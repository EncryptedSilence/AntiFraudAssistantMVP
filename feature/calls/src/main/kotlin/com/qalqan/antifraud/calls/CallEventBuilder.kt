package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.manual.CallEntryDigest
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import java.time.Instant
import java.util.UUID

/**
 * Spec §4.2.1 + §16.2 — composes a `CallEvent` from a CallLog row using the
 * privacy-preserving normalize-then-salted-hash path shared with `ManualEntry`.
 * The orchestrator (Phase 8) recomputes session / campaign risk on insert,
 * so `callRiskScore` is left at 0 here.
 */
class CallEventBuilder(
    private val digest: CallEntryDigest,
    private val contacts: IsKnownContactResolver,
    private val repeats: RepeatCallDetector,
) {
    suspend fun build(
        row: CallLogRow,
        simSlot: Int?,
    ): CallEvent {
        val hash = digest.hash(row.rawNumber)
        val startedAt = Instant.ofEpochMilli(row.startedAtMs)
        val endedAt = startedAt.plusSeconds(row.durationSec)
        val direction =
            when (row.direction) {
                CallLogRow.Direction.INCOMING -> CallDirection.INCOMING
                CallLogRow.Direction.OUTGOING -> CallDirection.OUTGOING
                CallLogRow.Direction.MISSED -> CallDirection.MISSED
                CallLogRow.Direction.UNKNOWN -> CallDirection.INCOMING
            }
        return CallEvent(
            id = EventId(UUID.randomUUID().toString()),
            phoneHash = hash,
            simSlot = simSlot,
            direction = direction,
            startedAt = startedAt,
            endedAt = endedAt,
            durationSec = row.durationSec,
            isKnownContact = contacts.isKnown(hash),
            isRepeated = repeats.isRepeated(hash, startedAt),
            callRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )
    }
}
