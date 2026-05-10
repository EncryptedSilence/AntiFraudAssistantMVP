package com.qalqan.antifraud.database.patterns

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object PatternStateMigration {
    val MIGRATION_1_2: Migration =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pattern_state (
                        pattern_id TEXT NOT NULL PRIMARY KEY,
                        enabled INTEGER NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
}
