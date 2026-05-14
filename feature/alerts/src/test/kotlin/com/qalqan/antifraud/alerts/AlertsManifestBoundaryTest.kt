package com.qalqan.antifraud.alerts

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AlertsManifestBoundaryTest {
    private val manifest by lazy {
        val file = File("src/main/AndroidManifest.xml")
        require(file.exists()) { "manifest missing at ${file.absolutePath}" }
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(file)
    }

    private fun permissionNames(): List<String> {
        val nodes = manifest.getElementsByTagName("uses-permission")
        return (0 until nodes.length).map { i ->
            nodes.item(i).attributes.getNamedItemNS(
                "http://schemas.android.com/apk/res/android",
                "name",
            ).nodeValue
        }
    }

    @Test
    fun `declares the two §4_4_1 alert permissions`() {
        permissionNames() shouldContain "android.permission.USE_FULL_SCREEN_INTENT"
        permissionNames() shouldContain "android.permission.SYSTEM_ALERT_WINDOW"
    }

    @Test
    fun `declares no §2_1 forbidden permissions`() {
        permissionNames() shouldNotContainAnyOf
            listOf(
                "android.permission.RECORD_AUDIO",
                "android.permission.BIND_ACCESSIBILITY_SERVICE",
                "android.permission.BIND_INCALL_SERVICE",
                "android.permission.BIND_SCREENING_SERVICE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MANAGE_EXTERNAL_STORAGE",
                "android.permission.PACKAGE_USAGE_STATS",
                "android.permission.INTERNET",
            )
    }
}
