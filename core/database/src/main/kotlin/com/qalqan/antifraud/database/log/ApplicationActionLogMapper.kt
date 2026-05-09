package com.qalqan.antifraud.database.log

import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.domain.ApplicationActionLogEntry
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.Instant

private val moshi = Moshi.Builder().build()
private val mapAdapter: JsonAdapter<Map<String, String>> =
    moshi.adapter(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java),
    )

internal fun ApplicationActionLogEntry.toEntity() =
    ApplicationActionLogEntity(
        id = id,
        createdAtMs = createdAt.toEpochMilli(),
        action = action.name,
        detailsJson = mapAdapter.toJson(details),
    )

internal fun ApplicationActionLogEntity.toDomain() =
    ApplicationActionLogEntry(
        id = id,
        createdAt = Instant.ofEpochMilli(createdAtMs),
        action = AppAction.valueOf(action),
        details = mapAdapter.fromJson(detailsJson) ?: emptyMap(),
    )
