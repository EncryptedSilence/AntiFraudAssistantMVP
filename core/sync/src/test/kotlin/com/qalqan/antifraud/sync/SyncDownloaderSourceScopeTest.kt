package com.qalqan.antifraud.sync

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.File

/**
 * Spec §2.1 — `:core:sync` is the only module allowed to import [java.net.HttpURLConnection]
 * or construct [java.net.URL]. Every other production source root must be HTTP-free.
 *
 * The existing Stage 5 `Acceptance5NoUrlFetchTest` covers `:feature:web` + `:app`; this
 * test covers every other production source root in the repo. The two tests together
 * pin the boundary across the full codebase.
 */
class SyncDownloaderSourceScopeTest {
    private val repoRoot = File(System.getProperty("user.dir")!!)
        .parentFile!!.parentFile!! // core/sync/ → core/ → repo root

    private fun productionSourceRoots(): List<File> = listOf(
        "app/src/main",
        "core/correlation/src/main",
        "core/crypto/src/main",
        "core/database/src/main",
        "core/demo/src/main",
        "core/domain/src/main",
        "core/patterns/src/main",
        "core/scoring/src/main",
        "feature/calls/src/main",
        "feature/sms/src/main",
        "feature/web/src/main",
        // NOTE: core/sync/src/main is intentionally absent — it IS allowed to import HTTP types.
    ).map { File(repoRoot, it) }.filter { it.exists() }

    @Test
    fun `core_sync main sources DO import HttpURLConnection`() {
        val syncMain = File(repoRoot, "core/sync/src/main")
        val text = syncMain.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .joinToString("\n") { it.readText() }
        (text.contains("HttpURLConnection")) shouldBe true
        (text.contains("java.net.URL")) shouldBe true
    }

    @Test
    fun `no other production source root contains HttpURLConnection or java_net_URL`() {
        val violations = mutableListOf<String>()
        productionSourceRoots().forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .forEach { file ->
                    val text = file.readText()
                    if (text.contains("HttpURLConnection")) violations += "${file.path} :: HttpURLConnection"
                    if (text.contains("java.net.URL")) violations += "${file.path} :: java.net.URL"
                }
        }
        violations shouldBe emptyList()
    }
}
