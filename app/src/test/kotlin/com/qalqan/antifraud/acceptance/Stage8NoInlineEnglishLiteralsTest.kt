package com.qalqan.antifraud.acceptance

import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.Test
import java.io.File

/**
 * §24 #11 — every user-facing string lives in res/values/strings.xml. New Stage 8 UI
 * packages must not embed raw English literals; the source-scan asserts this.
 */
class Stage8NoInlineEnglishLiteralsTest {
    private val repoRoot = File(System.getProperty("user.dir")!!).parentFile!!

    @Test
    fun `every Stage 8 ui package uses stringResource(R_string_xxx) for user-facing copy`() {
        val targets =
            listOf(
                "app/src/main/kotlin/com/qalqan/antifraud/ui/home",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/campaign",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/patterns",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/references",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/privacy",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/settings",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/onboarding",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/question",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/pause",
                "app/src/main/kotlin/com/qalqan/antifraud/ui/education",
            )
        val violations = mutableListOf<String>()
        targets.forEach { dir ->
            val root = File(repoRoot, dir)
            if (!root.exists()) return@forEach
            root.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                val text = file.readText()
                // Flag any `Text("Capitalized…")` literal whose payload looks like user
                // copy. Permits §-citations, "[…]" placeholders, single-char strings.
                val literalRegex = Regex("""Text\("([A-Z][a-z][^"]{2,})"""")
                literalRegex.findAll(text).forEach { match ->
                    val literal = match.groupValues[1]
                    if (!literal.contains("§") && !literal.startsWith("[")) {
                        violations += "${file.relativeTo(repoRoot).path}: '$literal'"
                    }
                }
            }
        }
        violations.shouldBeEmpty()
    }
}
