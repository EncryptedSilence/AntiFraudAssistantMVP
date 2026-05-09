package com.qalqan.antifraud.correlation

import java.time.Duration

/**
 * Spec §3.2 — discrete session windows; finer-grained clues pick smaller windows.
 */
enum class LinkClue {
    SAME_PHONE_HASH,
    SAME_DOMAIN_HASH,
    SAME_SENDER_HASH,
    USER_CONFIRMED_LINK,
    SAME_SCENARIO_CATEGORY
}

object SessionWindowSelector {
    private val windows: List<Pair<Set<LinkClue>, Duration>> = listOf(
        setOf(LinkClue.SAME_PHONE_HASH, LinkClue.SAME_DOMAIN_HASH, LinkClue.SAME_SENDER_HASH) to Duration.ofMinutes(15),
        setOf(LinkClue.USER_CONFIRMED_LINK) to Duration.ofMinutes(60),
        setOf(LinkClue.SAME_SCENARIO_CATEGORY) to Duration.ofHours(24)
    )

    private val DEFAULT: Duration = Duration.ofMinutes(30)

    fun windowFor(clues: Set<LinkClue>): Duration {
        if (clues.isEmpty()) return DEFAULT
        val matched = windows.filter { (set, _) -> set.any { it in clues } }.map { it.second }
        // strongest = smallest window across matches
        return matched.minOrNull() ?: DEFAULT
    }
}
