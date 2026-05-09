package com.qalqan.antifraud.database.log

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "application_action_log",
    indices = [Index("createdAtMs")],
)
internal data class ApplicationActionLogEntity(
    @PrimaryKey val id: String,
    val createdAtMs: Long,
    val action: String,
    val detailsJson: String,
)
