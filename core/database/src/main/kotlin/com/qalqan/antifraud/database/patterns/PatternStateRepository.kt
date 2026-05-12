package com.qalqan.antifraud.database.patterns

import java.time.Instant

/**
 * Domain-level wrapper over [PatternStateDao]. Internal-visible.
 * Externals access via [com.qalqan.antifraud.database.Repositories.patternState].
 */
class PatternStateRepository internal constructor(private val dao: PatternStateDao) {
    suspend fun isEnabled(
        patternId: String,
        default: Boolean,
    ): Boolean {
        val override = dao.findById(patternId) ?: return default
        return override.enabled
    }

    suspend fun setEnabled(
        patternId: String,
        enabled: Boolean,
        at: Instant,
    ) {
        dao.upsert(PatternStateEntity(patternId, enabled, at.toString()))
    }

    /**
     * Records one trigger event for [patternId] at [triggeredAt]. Creates the row if absent
     * (enabled = true by default); increments [PatternStateEntity.timesTriggered] otherwise.
     */
    suspend fun recordTrigger(
        patternId: String,
        triggeredAt: Instant,
    ) {
        val existing = dao.findById(patternId)
        val updated =
            if (existing != null) {
                existing.copy(
                    lastTriggeredAt = triggeredAt.toString(),
                    timesTriggered = existing.timesTriggered + 1,
                )
            } else {
                PatternStateEntity(
                    patternId = patternId,
                    enabled = true,
                    updatedAt = triggeredAt.toString(),
                    lastTriggeredAt = triggeredAt.toString(),
                    timesTriggered = 1,
                )
            }
        dao.upsert(updated)
    }

    /**
     * Returns all patterns that have been triggered at least once, as [PatternTriggerInfo].
     * Used by the Stage 7 export gather arm (§8.2).
     */
    suspend fun listTriggered(): List<PatternTriggerInfo> =
        dao.listTriggered().map { entity ->
            PatternTriggerInfo(
                patternId = entity.patternId,
                lastTriggeredAt = Instant.parse(entity.lastTriggeredAt!!),
                timesTriggered = entity.timesTriggered,
            )
        }

    suspend fun resetToDefaults() {
        dao.deleteAll()
    }
}
