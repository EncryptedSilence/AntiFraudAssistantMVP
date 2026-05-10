package com.qalqan.antifraud.database.patterns

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pattern_state")
internal data class PatternStateEntity(
    @PrimaryKey @ColumnInfo(name = "pattern_id") val patternId: String,
    @ColumnInfo(name = "enabled") val enabled: Boolean,
    // ISO-8601 Instant
    @ColumnInfo(name = "updated_at") val updatedAt: String,
)
