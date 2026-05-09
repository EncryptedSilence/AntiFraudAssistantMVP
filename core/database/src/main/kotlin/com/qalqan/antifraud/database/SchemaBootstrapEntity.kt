package com.qalqan.antifraud.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Placeholder so Room can compile while real entities land incrementally in T50–T55.
 * Removed once the first real entity exists.
 */
@Entity(tableName = "schema_bootstrap")
internal data class SchemaBootstrapEntity(
    @PrimaryKey val id: Int = 0,
)
