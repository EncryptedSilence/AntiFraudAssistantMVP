package com.qalqan.antifraud.sms

/**
 * Spec §4.2.2 — orchestrates the content-provider sweep. `runOneShot` is the unit-testable
 * entry point: log sweep-started, run a single pass, log sweep-stopped. The Compose-side
 * lifecycle code in `MainActivity` (T30) drives periodic invocations on `onResume`.
 */
class SmsSweepCoordinator(
    private val sweeper: SmsContentProviderSweeper,
    private val actionLog: SmsObserverActionLog,
) {
    suspend fun runOneShot(sinceMs: Long) {
        actionLog.sweepStarted()
        try {
            sweeper.sweepSince(sinceMs)
        } finally {
            actionLog.sweepStopped()
        }
    }
}
