package com.qalqan.antifraud.export

import android.content.Context
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.patterns.ScenarioPattern
import com.qalqan.antifraud.patterns.SeedPatternLoader

/**
 * Spec §8.2 — emits a [ExportRecord.TriggeredPattern] for every pattern that has fired at
 * least once. Joins [com.qalqan.antifraud.database.patterns.PatternStateRepository.listTriggered]
 * with the in-APK seed catalog loaded by [SeedPatternLoader] to get the pattern display name,
 * category, and version.
 *
 * Takes a [Context] to read the synced overlay (Stage 6 §7) when present; falls back to the
 * in-APK seeds otherwise.
 *
 * User-created patterns are post-MVP; this arm covers only system-triggered patterns.
 */
internal class TriggeredPatternsGathererArm(private val context: Context) : GathererArm {
    override suspend fun gather(repositories: Repositories): List<ExportRecord> {
        val triggered = repositories.patternState.listTriggered()
        if (triggered.isEmpty()) return emptyList()
        val catalog: Map<String, ScenarioPattern> =
            SeedPatternLoader
                .load(syncedPatternsDir = resolveSyncedPatternsDir())
                .associateBy { it.patternId.value }
        return triggered.mapNotNull { info ->
            val pattern = catalog[info.patternId] ?: return@mapNotNull null
            ExportRecord.TriggeredPattern(
                patternId = info.patternId,
                name = pattern.name,
                scenarioCategory = pattern.category.name.lowercase(),
                version = pattern.version,
                triggeredAt = info.lastTriggeredAt,
                timesTriggered = info.timesTriggered,
            )
        }
    }

    private fun resolveSyncedPatternsDir(): java.io.File? {
        val dir = java.io.File(context.filesDir, "sync/current/patterns")
        return if (dir.exists()) dir else null
    }
}
