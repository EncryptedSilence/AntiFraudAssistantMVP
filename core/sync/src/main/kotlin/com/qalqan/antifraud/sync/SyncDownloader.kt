@file:Suppress("ReturnCount")

package com.qalqan.antifraud.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Spec §7.2 + §7.4 — download-only HTTP client for fetching `.afpkg` bundles from the
 * `stable` channel. Single GET via [HttpURLConnection]; never POSTs, never sends cookies,
 * never follows redirects. The result is a raw byte array of the response body; the
 * caller parses it via [com.qalqan.antifraud.crypto.BundleArchiveReader].
 *
 * Returns a typed [SyncDownloadError] on any failure so the orchestrator can log a
 * state marker without inspecting an exception message.
 */
interface SyncDownloader {
    suspend fun fetchLatest(url: String): Result<ByteArray>
}

class HttpUrlConnectionSyncDownloader : SyncDownloader {
    override suspend fun fetchLatest(url: String): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            val connection =
                try {
                    (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = CONNECT_TIMEOUT_MS
                        readTimeout = READ_TIMEOUT_MS
                        instanceFollowRedirects = false
                        requestMethod = "GET"
                        doInput = true
                        doOutput = false
                    }
                } catch (_: Exception) {
                    return@withContext Result.failure(SyncDownloadErrorException(SyncDownloadError.Network))
                }
            try {
                val code = connection.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(SyncDownloadErrorException(SyncDownloadError.Http(code)))
                }
                val bytes = connection.inputStream.use { readBodyBounded(it) }
                Result.success(bytes)
            } catch (_: SocketTimeoutException) {
                Result.failure(SyncDownloadErrorException(SyncDownloadError.Timeout))
            } catch (e: SyncDownloadErrorException) {
                Result.failure(e)
            } catch (_: Exception) {
                Result.failure(SyncDownloadErrorException(SyncDownloadError.Network))
            } finally {
                connection.disconnect()
            }
        }

    private fun readBodyBounded(input: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            val n = input.read(buf)
            if (n == -1) break
            total += n
            if (total > MAX_BODY_BYTES) {
                throw SyncDownloadErrorException(SyncDownloadError.BodyTooLarge)
            }
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    companion object {
        const val CONNECT_TIMEOUT_MS = 5_000
        const val READ_TIMEOUT_MS = 10_000
        const val MAX_BODY_BYTES = 1024L * 1024L
        private const val BUFFER_SIZE = 8192
    }
}

/** Spec §7.4 — typed failure modes for [SyncDownloader.fetchLatest]. */
sealed class SyncDownloadError {
    data class Http(val code: Int) : SyncDownloadError()

    data object Timeout : SyncDownloadError()

    data object BodyTooLarge : SyncDownloadError()

    data object Network : SyncDownloadError()
}

/** Throwable wrapper so [SyncDownloadError] can travel through `Result.failure`. */
class SyncDownloadErrorException(val error: SyncDownloadError) :
    Throwable(error::class.simpleName)
