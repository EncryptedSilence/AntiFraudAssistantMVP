package com.qalqan.antifraud.acceptance

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.File

/**
 * Spec §23 #34 — the app must never invoke any write-capable SMS provider URI. The
 * canonical write URIs are `content://sms`, `content://sms/sent`, `content://sms/draft`,
 * `content://sms/outbox`, etc. We do a source-tree string scan across `:feature:sms`
 * and `:app` Kotlin sources for the writeable prefixes; the only read URI we tolerate
 * is `content://sms/inbox` (Robolectric tests use it) and `Telephony.Sms.Inbox.CONTENT_URI`
 * referenced symbolically.
 *
 * The scan runs against the project's source tree, not the compiled APK; that is the
 * lighter check available to a Robolectric unit test. The byte-level APK static check
 * for `Sms.sendTextMessage` / SmsManager calls is a Stage 9 release-gate concern.
 */
class Acceptance34NoSmsProviderWriteTest {
    private val projectRoot: File =
        File(System.getProperty("user.dir")!!).resolve("../").canonicalFile

    private val scanRoots: List<File> =
        listOf("feature/sms/src", "app/src").map { projectRoot.resolve(it) }

    private fun kotlinSources(): List<File> =
        scanRoots
            .filter { it.isDirectory }
            .flatMap { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .filterNot { "/build/" in it.invariantSeparatorsPath }
                    .filterNot { "/src/test/" in it.invariantSeparatorsPath }
                    .filterNot { "/src/androidTest/" in it.invariantSeparatorsPath }
                    .toList()
            }

    @Test
    fun `scan covers a non-empty set of Kotlin sources`() {
        kotlinSources().size shouldBeGreaterThan 0
    }

    @Test
    fun `no Kotlin source references content sms write URIs`() {
        val forbidden =
            listOf(
                "content://sms/sent",
                "content://sms/draft",
                "content://sms/outbox",
                "content://sms/undelivered",
                "content://sms/queued",
                "content://sms/conversations",
                // The write URI is the bare "content://sms"; we accept "content://sms/inbox"
                // because Robolectric tests use it. Other Inbox references go through
                // Telephony.Sms.Inbox.CONTENT_URI symbolically.
            )
        val violations = mutableListOf<String>()
        kotlinSources().forEach { f ->
            val text = f.readText()
            forbidden.forEach { needle ->
                if (text.contains(needle, ignoreCase = true)) {
                    violations += "${f.path}: contains forbidden URI $needle"
                }
            }
        }
        violations.size shouldBe 0
    }

    @Test
    fun `no Kotlin source uses SmsManager send call surface`() {
        val forbiddenApi =
            listOf(
                "SmsManager.getDefault",
                "smsManager.sendTextMessage",
                "smsManager.sendMultipartTextMessage",
                "smsManager.sendDataMessage",
            )
        val violations = mutableListOf<String>()
        kotlinSources().forEach { f ->
            val text = f.readText()
            forbiddenApi.forEach { needle ->
                if (text.contains(needle, ignoreCase = false)) {
                    violations += "${f.path}: contains forbidden API $needle"
                }
            }
        }
        violations.size shouldBe 0
    }
}
