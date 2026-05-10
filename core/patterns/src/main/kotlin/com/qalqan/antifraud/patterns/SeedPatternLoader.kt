package com.qalqan.antifraud.patterns

object SeedPatternLoader {
    private val seedFileNames: List<String> = listOf(
        "bank_security_otp_after_call_v1.json",
        "unknown_call_then_link_sms_v1.json",
        "authority_spoof_call_v1.json",
        "new_lookalike_domain_visit_v1.json",
        "multistage_pressure_campaign_v1.json",
    )

    fun load(): List<ScenarioPattern> = seedFileNames.map { name ->
        val path = "/patterns/seed/$name"
        val stream = SeedPatternLoader::class.java.getResourceAsStream(path)
            ?: throw PatternParseException("seed pattern resource not found: $path")
        val json = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        PatternCatalogParser.fromJson(json)
    }
}
