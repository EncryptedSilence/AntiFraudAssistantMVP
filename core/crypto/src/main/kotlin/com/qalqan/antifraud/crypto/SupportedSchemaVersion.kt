package com.qalqan.antifraud.crypto

/**
 * Spec §7.4 — schema-version compatibility check. MVP supports exactly one schema
 * version (1). Bundles targeting a future schema are rejected at verify time so the
 * user keeps running on the last-known-good catalog instead of silently consuming a
 * payload the parser doesn't fully understand.
 */
object SupportedSchemaVersion {
    const val CURRENT: Int = 1

    fun isSupported(version: Int): Boolean = version == CURRENT
}
