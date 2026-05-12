package com.qalqan.antifraud.sync

import com.qalqan.antifraud.crypto.BundleArchiveReader
import com.qalqan.antifraud.crypto.BundleVerifier
import com.qalqan.antifraud.crypto.VerifiedBundle
import com.qalqan.antifraud.database.log.ApplicationActionLogger

/**
 * Spec §7.4 — orchestrates a single sync attempt. Steps in order:
 *
 *   1. Short-circuit to [SyncOutcome.Disabled] when [SyncSettings.enabled] is false.
 *      This is the §23 #4 zero-egress boundary: the downloader is not invoked when the
 *      user has not opted in.
 *   2. Fetch bytes via [SyncDownloader].
 *   3. Read archive via [BundleArchiveReader].
 *   4. Verify via [BundleVerifier].
 *   5. Activate via [BundleStore].
 *   6. Action-log a state marker (sync_completed or sync_failed) — never a URL,
 *      never an exception message.
 */
class SyncOrchestrator(
    private val settings: SyncSettings,
    private val downloader: SyncDownloader,
    private val archiveReader: BundleArchiveReader,
    private val verifier: BundleVerifier,
    private val store: BundleStore,
    private val actionLogger: ApplicationActionLogger?,
) {
    suspend fun runOnce(channelUrl: String): SyncOutcome {
        if (!settings.enabled) return SyncOutcome.Disabled
        // Full pipeline lands in T31.
        return SyncOutcome.Failed("not_implemented")
    }
}

/** Spec §7.4 — outcome of one [SyncOrchestrator.runOnce] invocation. */
sealed class SyncOutcome {
    data object Disabled : SyncOutcome()
    data class Downloaded(val bytes: ByteArray) : SyncOutcome()
    data class Verified(val bundle: VerifiedBundle) : SyncOutcome()
    data class Activated(val bundle: VerifiedBundle) : SyncOutcome()
    data class Failed(val reason: String) : SyncOutcome()
}
