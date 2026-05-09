package com.qalqan.antifraud.acceptance

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.io.File

class NoNetworkAcceptanceTest {
    private val moduleManifests: List<File> =
        listOf(
            "app/src/main/AndroidManifest.xml",
            "core/database/src/main/AndroidManifest.xml",
            "core/demo/src/main/AndroidManifest.xml",
        ).map { File(System.getProperty("user.dir")!!).resolve("../$it").canonicalFile }

    @Test
    fun `Stage 1 manifests do not request INTERNET (spec §23 #1, #4)`() {
        moduleManifests
            .filter { it.exists() }
            .filter { "android.permission.INTERNET" in it.readText(Charsets.UTF_8) }
            .shouldBeEmpty()
    }

    @Test
    fun `app launcher activity is declared (spec §23 #1)`() {
        val appManifest =
            File(System.getProperty("user.dir")!!)
                .resolve("../app/src/main/AndroidManifest.xml").canonicalFile
        val text = appManifest.readText(Charsets.UTF_8)
        text shouldContain "android.intent.action.MAIN"
        text shouldContain "android.intent.category.LAUNCHER"
    }
}
