package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.repository.CallEventRepository
import com.qalqan.antifraud.domain.CallEvent

/**
 * Spec §4.2.1 — entry point on the IDLE transition. Reads the latest CallLog row,
 * builds a `CallEvent`, persists it. Stateless on purpose: each IDLE reads the
 * provider once and writes once. No background polling.
 */
class AutoCallCapture(
    private val reader: CallLogReader,
    private val builder: CallEventBuilder,
    private val calls: CallEventRepository,
    private val onCaptured: suspend (CallEvent) -> Unit = {},
) {
    suspend fun onIdle(simSlot: Int?) {
        val row = reader.readLatest() ?: return
        val event = builder.build(row, simSlot)
        calls.save(event)
        onCaptured(event)
    }
}
