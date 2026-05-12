package com.qalqan.antifraud.database.export

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Spec §16.10 — the audit row for one accepted export. `redactionPreviewShown` is the
 * Stage 7 invariant: the writer refuses to land bytes unless this flag is `true` for the
 * corresponding profile row.
 *
 * `includedCategories` and `anonymizationLevel` are comma-separated JSON-value strings
 * (e.g. `risk_campaigns,suspicious_numbers` and `numbers_last_4,dates_day_only`) so the
 * schema stays flat and SQLCipher-friendly. The §17.5 export screen renders them back
 * into the typed enums when the user reviews their export history (Stage 8 / 9).
 *
 * `filePathLocal` is intentionally absent from Stage 7 (the user owns the file via SAF;
 * we record only that the export was performed, not where the user saved it). The
 * §16.10 spec mentions `filePathLocal` — we treat that as a Stage 8 / 9 enhancement when
 * the export-history UI surfaces.
 */
@Entity(tableName = "export_profile")
data class ExportProfileEntity(
    @PrimaryKey val exportId: String,
    val createdAt: Long,
    val exportType: String,
    val includedCategories: String,
    val anonymizationLevel: String,
    val format: String,
    val userConfirmed: Boolean,
    val redactionPreviewShown: Boolean,
)
