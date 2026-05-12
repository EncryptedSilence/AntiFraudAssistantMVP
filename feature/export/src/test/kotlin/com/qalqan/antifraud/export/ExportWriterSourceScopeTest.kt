package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.File

/**
 * Spec §2.1 / §8 / CLAUDE.md hard rule — `:feature:export` is the only module allowed to
 * call [android.content.ContentResolver.openOutputStream]. Every other production source
 * root must read-only `openInputStream` (for the §7.5 local-bundle import path) or do
 * no SAF I/O at all.
 *
 * The Stage 6 `SyncDownloaderSourceScopeTest` covered `HttpURLConnection`; this test pins
 * the analogous boundary for the SAF writer.
 */
class ExportWriterSourceScopeTest {
    private val repoRoot =
        File(System.getProperty("user.dir")!!)
            .parentFile!!.parentFile!! // feature/export/ → feature/ → repo root

    private fun productionSourceRoots(): List<File> =
        listOf(
            "app/src/main",
            "core/correlation/src/main",
            "core/crypto/src/main",
            "core/database/src/main",
            "core/demo/src/main",
            "core/domain/src/main",
            "core/patterns/src/main",
            "core/scoring/src/main",
            "core/sync/src/main",
            "feature/calls/src/main",
            "feature/sms/src/main",
            "feature/web/src/main",
            // NOTE: feature/export/src/main is intentionally absent — it IS allowed.
        ).map { File(repoRoot, it) }.filter { it.exists() }

    @Test
    fun `feature_export main sources DO import openOutputStream`() {
        val exportMain = File(repoRoot, "feature/export/src/main")
        val text =
            exportMain.walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .joinToString("\n") { it.readText() }
        text.contains("openOutputStream") shouldBe true
    }

    @Test
    fun `no other production source root imports openOutputStream`() {
        val violations = mutableListOf<String>()
        productionSourceRoots().forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .forEach { file ->
                    val text = file.readText()
                    if (text.contains("openOutputStream")) {
                        violations += "${file.path} :: openOutputStream"
                    }
                }
        }
        violations shouldBe emptyList()
    }
}
