package com.qalqan.antifraud.acceptance

import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.Test
import java.io.File

/**
 * §2.1 source-scan for the Stage 9 `:feature:alerts` module. No `:feature:alerts`
 * source file may reference forbidden AppOp permission strings, analytics SDK
 * literals, or HTTP-client packages — §2.1 bans background egress everywhere
 * except `:core:sync`.
 */
class Stage9PrivacyBoundarySourceScanTest {
    @Test
    fun `feature alerts module declares no forbidden AppOp, analytics, or network literals`() {
        val candidates =
            listOf(
                File("../feature/alerts/src/main"),
                File("feature/alerts/src/main"),
            )
        val root = candidates.firstOrNull { it.exists() }
            ?: error("expected to find :feature:alerts/src/main; tried ${candidates.map { it.absolutePath }}")
        val src = root.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "xml") }.toList()
        val forbidden =
            listOf(
                "RECORD_AUDIO",
                "BIND_ACCESSIBILITY_SERVICE",
                "BIND_INCALL_SERVICE",
                "BIND_SCREENING_SERVICE",
                "WRITE_EXTERNAL_STORAGE",
                "MANAGE_EXTERNAL_STORAGE",
                "FirebaseAnalytics",
                "com.google.firebase",
                "com.crashlytics",
                "okhttp",
                "retrofit",
                "io.ktor.client",
            )
        val violations =
            src.flatMap { f ->
                forbidden.mapNotNull { lit ->
                    if (f.readText().contains(lit)) f.name to lit else null
                }
            }
        violations.shouldBeEmpty()
    }
}
