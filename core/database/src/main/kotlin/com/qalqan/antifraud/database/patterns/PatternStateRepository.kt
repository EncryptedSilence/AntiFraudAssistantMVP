package com.qalqan.antifraud.database.patterns

import java.time.Instant

/**
 * Domain-level wrapper over [PatternStateDao]. Internal-visible.
 * Externals access via [com.qalqan.antifraud.database.Repositories.patternState].
 */
class PatternStateRepository internal constructor(private val dao: PatternStateDao) {
    suspend fun isEnabled(patternId: String, default: Boolean): Boolean {
        val override = dao.findById(patternId) ?: return default
        return override.enabled
    }

    suspend fun setEnabled(patternId: String, enabled: Boolean, at: Instant) {
        dao.upsert(PatternStateEntity(patternId, enabled, at.toString()))
    }

    suspend fun resetToDefaults() {
        dao.deleteAll()
    }
}
