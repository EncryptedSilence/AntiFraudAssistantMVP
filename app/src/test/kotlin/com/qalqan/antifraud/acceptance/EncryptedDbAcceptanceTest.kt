package com.qalqan.antifraud.acceptance

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Spec §23 #19 — the on-disk database must not be readable as a stock SQLite file.
 *
 * DEFERRED: Stage 8 (instrumented tests on a real device or emulator).
 *
 * The production code path uses [com.qalqan.antifraud.database.AntifraudDatabase.build] which
 * opens the database via the SQLCipher native library + an Android Keystore-wrapped key.
 * Neither is available under Robolectric:
 *   - SQLCipher's `.so` is not loaded by the JVM-only test runtime.
 *   - AndroidKeyStore is not wired by Robolectric for HMAC/AES key generation.
 *
 * The privacy invariant is therefore enforced at compile / runtime by the production wiring
 * (see `KeyStoreCryptoBox` and `SqlCipherFactory`). The acceptance evidence will be produced by
 * an instrumented test in a later stage that:
 *   1. Calls `AntifraudDatabase.build(context)` on-device.
 *   2. Performs at least one write (e.g. `SELECT 1` via the open helper).
 *   3. Reads the first 16 bytes of the resulting on-disk file and asserts the SQLite plaintext
 *      magic string `SQLite format 3 ` is absent.
 */
class EncryptedDbAcceptanceTest {
    @Test
    @Disabled("Requires SQLCipher + AndroidKeyStore; deferred to Stage 8 instrumented test.")
    fun `the on-disk DB header is not the SQLite plaintext header (spec §23 #19)`() {
        // Intentionally empty: see class-level KDoc for the deferral plan.
    }

    @Test
    @Disabled("Requires SQLCipher + AndroidKeyStore; deferred to Stage 8 instrumented test.")
    fun `database name is the expected value`() {
        // Intentionally empty: see class-level KDoc for the deferral plan.
    }
}
