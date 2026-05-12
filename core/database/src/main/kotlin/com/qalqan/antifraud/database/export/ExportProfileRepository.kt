package com.qalqan.antifraud.database.export

/**
 * Spec §16.10 — minimal repository for `export_profile` rows. Stage 7 ships only the
 * shapes the orchestrator needs (insert + findById + count). `listRecent` and per-format
 * filters are deferred to Stage 8 / 9 when the export-history UI surfaces.
 */
class ExportProfileRepository internal constructor(private val dao: ExportProfileDao) {
    suspend fun insert(entity: ExportProfileEntity) = dao.insert(entity)

    suspend fun findById(exportId: String): ExportProfileEntity? = dao.findById(exportId)

    suspend fun count(): Int = dao.count()
}
