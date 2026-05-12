package com.qalqan.antifraud.acceptance

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.File

/**
 * Spec §2.1 — no telemetry, no analytics, no crash reporters that send data. Stage 6
 * introduces the `INTERNET` permission in `:core:sync`; this test pins that the
 * permission appears ONLY in `:core:sync`'s manifest and not anywhere else, and that no
 * common analytics / crash-reporter SDK literal appears in any production source root.
 */
class Acceptance2NoTelemetrySdkTest {
    private val repoRoot = File(System.getProperty("user.dir")!!).parentFile!!

    private fun manifestFiles(): List<File> = listOf(
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
    ).map { File(repoRoot, it) }.filter { it.exists() }

    private fun productionSourceRoots(): List<File> = listOf(
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
    ).map { File(repoRoot, it) }.filter { it.exists() }

    @Test
    fun `INTERNET permission is declared only by core_sync`() {
        val violations = mutableListOf<String>()
        manifestFiles().forEach { manifest ->
            val text = manifest.readText()
            val mentionsInternet = text.contains("android.permission.INTERNET")
            val isCoreSync = manifest.path.replace('\\', '/').contains("core/sync/src/main")
            if (mentionsInternet && !isCoreSync) {
                violations += "${manifest.path} declares INTERNET"
            }
        }
        violations shouldBe emptyList()
    }

    @Test
    fun `production sources contain no analytics or crash-reporter SDK literals`() {
        val forbidden = listOf(
            "Firebase",
            "Crashlytics",
            "Bugsnag",
            "Sentry",
            "Mixpanel",
            "Amplitude",
            "Datadog",
            "Segment",
            "Google Analytics",
            "GoogleAnalytics",
            "androidx.tracing",
        )
        val violations = mutableListOf<String>()
        productionSourceRoots().forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && (it.name.endsWith(".kt") || it.name.endsWith(".xml")) }
                .forEach { file ->
                    val text = file.readText()
                    forbidden.forEach { needle ->
                        if (text.contains(needle)) violations += "${file.path} :: $needle"
                    }
                }
        }
        violations shouldBe emptyList()
    }
}
