package com.qalqan.antifraud.patterns

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SeedPatternLoaderOverlayTest {
    @Test
    fun `loader reads synced overlay when directory exists and has json files`(
        @TempDir tmp: Path,
    ) {
        val syncedDir = File(tmp.toFile(), "patterns").also { it.mkdirs() }
        val seedJson =
            SeedPatternLoader::class.java
                .getResourceAsStream("/patterns/seed/authority_spoof_call_v1.json")!!
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        val mutatedJson =
            seedJson.replace(
                "authority_spoof_call_v1",
                "synced_overlay_marker_v1",
            )
        File(syncedDir, "sample.json").writeText(mutatedJson)

        val loaded = SeedPatternLoader.load(syncedPatternsDir = syncedDir)
        loaded.any { it.patternId.value == "synced_overlay_marker_v1" } shouldBe true
    }

    @Test
    fun `loader falls back to APK seeds when synced overlay is null or empty`() {
        val loaded = SeedPatternLoader.load(syncedPatternsDir = null)
        (loaded.isNotEmpty()) shouldBe true
    }
}
