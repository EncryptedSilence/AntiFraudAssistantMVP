package com.qalqan.antifraud.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class ExportWriterTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `writes bytes to a file URI verbatim`() {
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            val uri = Uri.fromFile(file)
            val payload = "hello, export".toByteArray(Charsets.UTF_8)
            val result = ExportWriter.writeTo(uri, payload, context.contentResolver)
            result.isSuccess shouldBe true
            file.readBytes().contentEquals(payload) shouldBe true
        } finally {
            file.delete()
        }
    }

    @Test
    fun `an unreachable URI returns Result_failure(IoError) — never throws`() {
        val bogus = Uri.parse("content://com.example.nonexistent.provider/file")
        val resolver = mockk<ContentResolver>()
        every { resolver.openOutputStream(bogus) } throws IOException("provider not found")
        val result = ExportWriter.writeTo(bogus, "x".toByteArray(), resolver)
        result.isFailure shouldBe true
        (result.exceptionOrNull() is ExportWriteError.IoError) shouldBe true
    }
}
