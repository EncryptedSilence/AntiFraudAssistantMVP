package com.qalqan.antifraud.patterns

import java.io.File

object SeedPatternLoader {
    private val seedFileNames: List<String> =
        listOf(
            "bank_security_otp_after_call_v1.json",
            "unknown_call_then_link_sms_v1.json",
            "authority_spoof_call_v1.json",
            "new_lookalike_domain_visit_v1.json",
            "multistage_pressure_campaign_v1.json",
        )

    /**
     * Loads the scenario-pattern catalog. When [syncedPatternsDir] is provided and
     * contains at least one `*.json` file, those synced patterns replace the in-APK
     * seeds (Stage 6 §7 overlay). Otherwise the original in-APK seed catalog is loaded.
     */
    fun load(syncedPatternsDir: File? = null): List<ScenarioPattern> {
        if (syncedPatternsDir != null && syncedPatternsDir.exists()) {
            val jsonFiles = syncedPatternsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
                ?.toList()
                .orEmpty()
            if (jsonFiles.isNotEmpty()) {
                return jsonFiles.flatMap { f -> parseJsonBytes(f.readBytes()) }
            }
        }
        return loadFromApk()
    }

    private fun loadFromApk(): List<ScenarioPattern> =
        seedFileNames.map { name ->
            val path = "/patterns/seed/$name"
            val stream =
                SeedPatternLoader::class.java.getResourceAsStream(path)
                    ?: throw PatternParseException("seed pattern resource not found: $path")
            val json = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            PatternCatalogParser.fromJson(json)
        }

    private fun parseJsonBytes(bytes: ByteArray): List<ScenarioPattern> {
        val json = bytes.toString(Charsets.UTF_8)
        // Synced overlay files may contain either a single pattern object (matching the
        // in-APK seed shape) or an array of patterns.
        val trimmed = json.trimStart()
        return if (trimmed.startsWith("[")) {
            PatternCatalogParser.listFromJson(json)
        } else {
            listOf(PatternCatalogParser.fromJson(json))
        }
    }
}
