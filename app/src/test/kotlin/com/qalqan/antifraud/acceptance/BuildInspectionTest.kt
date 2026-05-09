package com.qalqan.antifraud.acceptance

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class BuildInspectionTest {
    private val moduleSources: List<File> =
        listOf(
            "app",
            "core/domain",
            "core/scoring",
            "core/correlation",
            "core/database",
            "core/demo",
            "build-logic",
        ).flatMap { module ->
            val root = File(System.getProperty("user.dir")!!).resolve("../").canonicalFile
            val moduleDir = root.resolve(module)
            if (moduleDir.isDirectory) {
                moduleDir.walkTopDown()
                    .filter { it.isFile }
                    .filter { it.extension in setOf("kt", "kts", "xml", "toml") }
                    .filterNot { "build/" in it.invariantSeparatorsPath || "/build/" in it.invariantSeparatorsPath }
                    .filterNot { "/src/test/" in it.invariantSeparatorsPath }
                    .filterNot { "/src/androidTest/" in it.invariantSeparatorsPath }
                    .toList()
            } else {
                emptyList()
            }
        }

    private val sourceText: String by lazy {
        moduleSources.joinToString("\n") { it.readText(Charsets.UTF_8) }
    }

    @Test
    fun `no AI inference dependency or runtime is referenced (spec §23 #3)`() {
        val forbidden =
            listOf(
                "tensorflow", "tflite", "onnxruntime", "mlkit", "ml-kit",
                "openai", "anthropic", "huggingface", "cohere",
            )
        forbidden.filter { it in sourceText.lowercase() } shouldBe emptyList()
    }

    @Test
    fun `no RECORD_AUDIO permission anywhere (spec §23 #22)`() {
        moduleSources
            .filter { it.name == "AndroidManifest.xml" }
            .filter { "android.permission.RECORD_AUDIO" in it.readText(Charsets.UTF_8) }
            .shouldBeEmpty()
    }

    @Test
    fun `no accessibility service intent filter (spec §23 #23)`() {
        moduleSources
            .filter { it.name == "AndroidManifest.xml" }
            .filter { "android.accessibilityservice.AccessibilityService" in it.readText(Charsets.UTF_8) }
            .shouldBeEmpty()
    }
}
