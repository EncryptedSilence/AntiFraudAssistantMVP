package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

/**
 * Spec Appendix A field set for `eventType: "CallEvent"`. Unknown fields return null.
 * Enums are surfaced as their `name` (String) so JSON `value` strings can match.
 */
internal object CallEventFields : EventFieldAccessor {
    override fun lookup(
        event: RiskEvent,
        field: String,
    ): Any? {
        if (event !is RiskEvent.Call) return null
        val c = event.event
        return when (field) {
            "isKnownContact" -> c.isKnownContact
            "isRepeated" -> c.isRepeated
            "direction" -> c.direction.name
            "durationSec" -> c.durationSec
            "simSlot" -> c.simSlot
            else -> null
        }
    }
}
