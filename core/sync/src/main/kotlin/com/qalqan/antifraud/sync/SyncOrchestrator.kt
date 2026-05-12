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

        val fetched = downloader.fetchLatest(channelUrl)
        if (fetched.isFailure) return fail("download")
        val rawBytes = fetched.getOrThrow()

        val readResult = archiveReader.read(rawBytes.inputStream())
        if (readResult.isFailure) return fail("read")
        val archive = readResult.getOrThrow()

        val verifyResult = verifier.verify(archive, appVersionCode = APP_VERSION_CODE)
        if (verifyResult.isFailure) return fail("verify")
        val verified = verifyResult.getOrThrow()

        val activateResult = store.activate(rawBytes, verified)
        if (activateResult.isFailure) return fail("activate")

        logSuccess()
        return SyncOutcome.Activated(verified)
    }

    private suspend fun fail(step: String): SyncOutcome {
        actionLogger?.log(
            com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
            mapOf("setting" to "sync_failed", "step" to step),
        )
        return SyncOutcome.Failed(step)
    }

    private suspend fun logSuccess() {
        actionLogger?.log(
            com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
            mapOf("setting" to "sync_completed"),
        )
    }

    companion object {
        // Stage 6 hardcodes appVersionCode = 1 because :core:sync cannot see :app's
        // BuildConfig. The orchestrator is constructed in :app where the real
        // appVersionCode is injected; this constant is the safe default for tests.
        const val APP_VERSION_CODE = 1
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
