@file:Suppress("MaxLineLength", "LoopWithTooManyJumpStatements")

package com.qalqan.antifraud.sync

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncDownloaderUnitTest {
    @Test
    fun `interface contract — Result type compiles`() {
        runBlocking {
            val d: SyncDownloader =
                object : SyncDownloader {
                    override suspend fun fetchLatest(url: String): Result<ByteArray> = Result.success("payload".toByteArray())
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

    /**
     * Minimal HTTP/1.1 responder backed by a raw [java.net.ServerSocket]. Used instead of
     * `com.sun.net.httpserver.HttpServer` because the Android-library test classpath does
     * not expose JDK 17's `jdk.httpserver` module by default.
     */
    private class MiniServer(
        private val onRequest: (java.io.OutputStream) -> Unit,
    ) : AutoCloseable {
        private val socket = java.net.ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))
        val port: Int = socket.localPort
        private val thread =
            Thread {
                try {
                    socket.accept().use { client ->
                        // Drain request headers up to blank line.
                        val reader = client.getInputStream().bufferedReader(Charsets.US_ASCII)
                        while (true) {
                            val line = reader.readLine() ?: break
                            if (line.isEmpty()) break
                        }
                        onRequest(client.getOutputStream())
                    }
                } catch (_: Throwable) {
                    // socket closed during shutdown is expected
                }
            }.apply {
                isDaemon = true
                start()
            }

        override fun close() {
            try {
                socket.close()
            } catch (_: Throwable) {
                // ignore
            }
            thread.join(1_000)
        }
    }

    private fun writeResponse(
        out: java.io.OutputStream,
        status: Int,
        statusText: String,
        body: ByteArray,
    ) {
        val header =
            (
                "HTTP/1.1 $status $statusText\r\n" +
                    "Content-Length: ${body.size}\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
            ).toByteArray(Charsets.US_ASCII)
        out.write(header)
        out.write(body)
        out.flush()
    }

    @Test
    fun `200 with small body returns Success`() {
        val payload = "hello-bundle".toByteArray()
        MiniServer { out -> writeResponse(out, 200, "OK", payload) }.use { srv ->
            runBlocking {
                val url = "http://127.0.0.1:${srv.port}/"
                val r = HttpUrlConnectionSyncDownloader().fetchLatest(url)
                r.isSuccess shouldBe true
                r.getOrThrow().contentEquals(payload) shouldBe true
            }
        }
    }

    @Test
    fun `404 returns Failure with Http(404)`() {
        MiniServer { out -> writeResponse(out, 404, "Not Found", ByteArray(0)) }.use { srv ->
            runBlocking {
                val url = "http://127.0.0.1:${srv.port}/"
                val r = HttpUrlConnectionSyncDownloader().fetchLatest(url)
                r.isFailure shouldBe true
                val err = (r.exceptionOrNull() as SyncDownloadErrorException).error
                (err is SyncDownloadError.Http) shouldBe true
                (err as SyncDownloadError.Http).code shouldBe 404
            }
        }
    }

    @Test
    fun `body larger than 1 MB returns Failure with BodyTooLarge`() {
        val oversize = ByteArray(HttpUrlConnectionSyncDownloader.MAX_BODY_BYTES.toInt() + 1) { 'A'.code.toByte() }
        MiniServer { out -> writeResponse(out, 200, "OK", oversize) }.use { srv ->
            runBlocking {
                val url = "http://127.0.0.1:${srv.port}/"
                val r = HttpUrlConnectionSyncDownloader().fetchLatest(url)
                r.isFailure shouldBe true
                val err = (r.exceptionOrNull() as SyncDownloadErrorException).error
                (err === SyncDownloadError.BodyTooLarge) shouldBe true
            }
        }
    }

    @Test
    fun `slow handler triggers read timeout`() {
        MiniServer { out ->
            Thread.sleep((HttpUrlConnectionSyncDownloader.READ_TIMEOUT_MS + 2_000).toLong())
            writeResponse(out, 200, "OK", ByteArray(0))
        }.use { srv ->
            runBlocking {
                val url = "http://127.0.0.1:${srv.port}/"
                val r = HttpUrlConnectionSyncDownloader().fetchLatest(url)
                r.isFailure shouldBe true
                val err = (r.exceptionOrNull() as SyncDownloadErrorException).error
                (err === SyncDownloadError.Timeout) shouldBe true
            }
        }
    }
}
