package com.qalqan.antifraud.acceptance

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.File

/**
 * Spec §2.1 / §5.4 — Stage 5 must never fetch the domain, open it in a browser, or
 * inject JS into a WebView. A source-tree scan rejects the obvious API surfaces. This
 * is the same shape as Stage 4's `Acceptance34NoSmsProviderWriteTest`.
 *
 * Resolves the project root via `BuildInspectionTest` precedent so the scan is non-vacuous.
 */
class Acceptance5NoUrlFetchTest {
    private val projectRoot: File =
        File(System.getProperty("user.dir")!!).resolve("../").canonicalFile

    @Test
    fun `scan covers a non-empty set of Kotlin production sources`() {
        kotlinSources().isNotEmpty() shouldBe true
    }

    @Test
    fun `no Kotlin source opens a URL via Intent ACTION_VIEW`() {
        val violations = scanFor(
            forbiddenLiterals = listOf(
                "Intent.ACTION_VIEW",
                "android.intent.action.VIEW",
                "Intent(Intent.ACTION_VIEW,",
            ),
        )
        violations.size shouldBe 0
    }

    @Test
    fun `no Kotlin source fetches HTTP via the standard JVM clients`() {
        val violations = scanFor(
            forbiddenLiterals = listOf(
                "HttpURLConnection",
                "URL(",
                "OkHttpClient",
                "Retrofit",
                "Volley",
            ),
        )
        violations.size shouldBe 0
    }

    @Test
    fun `no Kotlin source instantiates WebView`() {
        val violations = scanFor(
            forbiddenLiterals = listOf(
                "WebView(",
                "WebViewClient",
                "loadUrl(",
                "loadData(",
            ),
        )
        violations.size shouldBe 0
    }

    private fun kotlinSources(): List<File> {
        val roots = listOf(
            projectRoot.resolve("feature/web/src"),
            projectRoot.resolve("app/src"),
        )
        return roots.flatMap { root ->
            if (!root.exists()) emptyList()
            else
                root.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .filter {
                        val p = it.invariantSeparatorsPath
                        "/src/test/" !in p && "/src/androidTest/" !in p && "/build/" !in p
                    }
                    .toList()
        }
    }

    private fun scanFor(forbiddenLiterals: List<String>): List<String> {
        val violations = mutableListOf<String>()
        kotlinSources().forEach { f ->
            val text = f.readText()
            forbiddenLiterals.forEach { needle ->
                if (text.contains(needle)) {
                    violations += "${f.path}: contains forbidden literal $needle"
                }
            }
        }
        return violations
    }
}
