@file:Suppress("MaxLineLength")

package com.qalqan.antifraud.crypto

import android.content.Context

/**
 * Spec §7.4 — loads the embedded Ed25519 public key shipped at
 * `res/raw/sync_public_key.bin`. The file is exactly 32 bytes (Ed25519 public-key
 * size); any other size fails verification at construction time of any [BundleVerifier]
 * that consumes it.
 *
 * Key-rotation procedure: see `docs/plans/stage6/sync-keygen.md`. Rotation requires a
 * new APK release per the §7.4 simplification note ("MVP uses a single embedded public
 * key … key rotation … is post-MVP").
 */
object EmbeddedPublicKey {
    fun load(context: Context): ByteArray = context.resources.openRawResource(R.raw.sync_public_key).use { it.readBytes() }
}
