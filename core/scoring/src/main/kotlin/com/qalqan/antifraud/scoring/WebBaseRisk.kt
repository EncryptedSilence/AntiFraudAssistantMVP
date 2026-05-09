package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.WebEvent

/**
 * Spec §12.3. Lookalike-domain detection (Levenshtein ≤ 2 over the curated whitelist) is run by a
 * separate component because it depends on a list of well-known domains; here we only consume its
 * boolean verdict.
 */
object WebBaseRisk {
    fun compute(web: WebEvent, lookalikeMatch: Boolean): Int {
        var score = 0
        if (web.isNewDomain) score += 10
        if (lookalikeMatch) score += 35
        return score
    }
}
