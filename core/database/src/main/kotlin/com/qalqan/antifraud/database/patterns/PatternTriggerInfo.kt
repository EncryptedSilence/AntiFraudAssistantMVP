package com.qalqan.antifraud.database.patterns

import java.time.Instant

/** Read-only projection used by [PatternStateRepository.listTriggered]. */
data class PatternTriggerInfo(
    val patternId: String,
    val lastTriggeredAt: Instant,
    val timesTriggered: Int,
)
