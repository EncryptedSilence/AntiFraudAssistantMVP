package com.qalqan.antifraud.sync

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncDownloaderUnitTest {
    @Test
    fun `interface contract — Result type compiles`() {
        runBlocking {
            val d: SyncDownloader = object : SyncDownloader {
                override suspend fun fetchLatest(url: String): Result<ByteArray> =
                    Result.success("payload".toByteArray())
            }
            val r = d.fetchLatest("https://example.invalid/")
            r.isSuccess shouldBe true
            r.getOrThrow().toString(Charsets.UTF_8) shouldBe "payload"
        }
    }

    @Test
    fun `SyncDownloadError variants are distinguishable`() {
        val http: SyncDownloadError = SyncDownloadError.Http(500)
        val timeout: SyncDownloadError = SyncDownloadError.Timeout
        val tooBig: SyncDownloadError = SyncDownloadError.BodyTooLarge
        val net: SyncDownloadError = SyncDownloadError.Network
        (http is SyncDownloadError.Http) shouldBe true
        (http as SyncDownloadError.Http).code shouldBe 500
        (timeout === SyncDownloadError.Timeout) shouldBe true
        (tooBig === SyncDownloadError.BodyTooLarge) shouldBe true
        (net === SyncDownloadError.Network) shouldBe true
    }
}
