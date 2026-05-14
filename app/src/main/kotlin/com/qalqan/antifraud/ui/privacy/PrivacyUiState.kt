package com.qalqan.antifraud.ui.privacy

/**
 * Spec §17.6 — Privacy screen state. Modules/permissions are pre-rendered as
 * locale-independent labels (the privacy route does not translate them); retention
 * rows come from [com.qalqan.antifraud.settings.RetentionDisplay].
 */
data class PrivacyUiState(
    val modulesEnabled: List<String> = emptyList(),
    val permissionsGranted: List<String> = emptyList(),
    val retentionRows: List<RetentionRow> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isLoading: Boolean = false,
) {
    data class RetentionRow(val key: String, val days: Int)

    enum class SyncStatus { IDLE, FETCHING, VERIFYING, APPLYING, FAILED, PAUSED }
}
