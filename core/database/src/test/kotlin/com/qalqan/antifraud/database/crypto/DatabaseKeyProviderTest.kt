package com.qalqan.antifraud.database.crypto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DatabaseKeyProviderTest {
    @Test
    fun `key is stable across calls`(
        @TempDir dir: File,
    ) {
        val provider = DatabaseKeyProvider(File(dir, "antifraud.dbkey.enc"), InMemoryCryptoBox())
        val a = provider.obtainKey()
        val b = provider.obtainKey()
        a.contentEquals(b) shouldBe true
    }

    @Test
    fun `key is 32 bytes (256 bit)`(
        @TempDir dir: File,
    ) {
        val provider = DatabaseKeyProvider(File(dir, "antifraud.dbkey.enc"), InMemoryCryptoBox())
        provider.obtainKey().size shouldBe DB_KEY_BYTES
    }

    @Test
    fun `deleteKey removes the persisted envelope`(
        @TempDir dir: File,
    ) {
        val keyFile = File(dir, "antifraud.dbkey.enc")
        val provider = DatabaseKeyProvider(keyFile, InMemoryCryptoBox())
        provider.obtainKey()
        keyFile.exists() shouldBe true
        provider.deleteKey()
        keyFile.exists() shouldBe false
    }

    private companion object {
        const val DB_KEY_BYTES = 32
    }
}
