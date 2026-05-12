package com.qalqan.antifraud.acceptance

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.File

/**
 * Spec §2.1 + CLAUDE.md hard rule — no production manifest declares
 * `WRITE_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`. The Stage 7 export pipeline
 * routes every write through SAF (`ACTION_CREATE_DOCUMENT`), which is permission-less.
 *
 * The Stage 6 `Acceptance2NoTelemetrySdkTest` already covers `INTERNET` (only in
 * `:core:sync`). This test extends the manifest source scan to the two external-storage
 * permissions Stage 7 must never request.
 */
class Acceptance2NoExternalStorageTest {
    private val repoRoot = File(System.getProperty("user.dir")!!).parentFile!!

    private fun manifestFiles(): List<File> =
        listOf(
            "app/src/main/AndroidManifest.xml",
            "core/correlation/src/main/AndroidManifest.xml",
            "core/crypto/src/main/AndroidManifest.xml",
            "core/database/src/main/AndroidManifest.xml",
            "core/demo/src/main/AndroidManifest.xml",
            "core/domain/src/main/AndroidManifest.xml",
            "core/patterns/src/main/AndroidManifest.xml",
            "core/scoring/src/main/AndroidManifest.xml",
            "core/sync/src/main/AndroidManifest.xml",
            "feature/calls/src/main/AndroidManifest.xml",
            "feature/sms/src/main/AndroidManifest.xml",
            "feature/web/src/main/AndroidManifest.xml",
            "feature/export/src/main/AndroidManifest.xml",
        ).map { File(repoRoot, it) }.filter { it.exists() }

    @Test
    fun `no production manifest declares WRITE_EXTERNAL_STORAGE`() {
        val violations = mutableListOf<String>()
        manifestFiles().forEach { manifest ->
            val text = manifest.readText()
            if (text.contains("android.permission.WRITE_EXTERNAL_STORAGE")) {
                violations += "${manifest.path} declares WRITE_EXTERNAL_STORAGE"
            }
        }
        violations shouldBe emptyList()
    }

    @Test
    fun `no production manifest declares MANAGE_EXTERNAL_STORAGE`() {
        val violations = mutableListOf<String>()
        manifestFiles().forEach { manifest ->
            val text = manifest.readText()
            if (text.contains("android.permission.MANAGE_EXTERNAL_STORAGE")) {
                violations += "${manifest.path} declares MANAGE_EXTERNAL_STORAGE"
            }
        }
        violations shouldBe emptyList()
    }

    @Test
    fun `feature_export manifest declares no permissions at all (SAF is permission-less)`() {
        val manifest = File(repoRoot, "feature/export/src/main/AndroidManifest.xml")
        if (!manifest.exists()) return
        val text = manifest.readText()
        text.contains("uses-permission") shouldBe false
    }
}
