package com.qalqan.antifraud.debug

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Self-registering debug initializer. A [ContentProvider.onCreate] runs before the app's
 * `Application.onCreate`, so this is a dependency-free way for the `src/debug` source set to wire
 * [DebugHooks] before any screen renders. Declared only in the debug manifest, so release builds
 * never see it and the hooks stay null.
 *
 * It backs no data; every operation returns the empty result.
 */
class DebugInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        DebugHooks.seedDemoChains = { repos -> ChainSeeder.seed(repos) }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
