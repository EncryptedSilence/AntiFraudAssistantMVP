package com.qalqan.antifraud.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AntifraudDatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `inMemory factory opens a usable Room database`() {
        val db = AntifraudDatabase.inMemory(context)
        try {
            db.openHelper shouldNotBe null
            db.openHelper.writableDatabase shouldNotBe null
        } finally {
            db.close()
        }
    }
}
