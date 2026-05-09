package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.CallEvent

/**
 * Spec §12.1. Returns the structural part of CallEvent risk; identity-claim and ask-related deltas
 * come from UserAnswerRisk because they are not observable from call metadata alone.
 */
object CallBaseRisk {
    fun compute(call: CallEvent): Int {
        var score = 0
        if (!call.isKnownContact) score += 20
        if (call.isRepeated && !call.isKnownContact) score += 15
        if (call.durationSec > 180) score += 15
        return score
    }
}
