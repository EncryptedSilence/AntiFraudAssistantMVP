package com.qalqan.antifraud.ui.patterns

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.patterns.SeedPatternLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

/**
 * Spec §17.3 — ViewModel for the Patterns screen.
 *
 * Source detection: a pattern whose JSON file exists under
 * `filesDir/sync/current/patterns/<id>.json` is tagged [PatternsUiState.Source.BUNDLE];
 * otherwise [PatternsUiState.Source.SEED]. This matches the §7 overlay semantics of
 * [SeedPatternLoader.load].
 */
class PatternsViewModel(
    application: Application,
    private val repos: Repositories,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PatternsUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val app = getApplication<Application>()
            val bundleDir = File(app.filesDir, "sync/current/patterns")
            val seeds = SeedPatternLoader.load(syncedPatternsDir = bundleDir)
            val rows =
                seeds.map { p ->
                    val triggerInfo = repos.patternState.triggerInfo(p.patternId.value)
                    val source =
                        if (File(bundleDir, "${p.patternId.value}.json").exists()) {
                            PatternsUiState.Source.BUNDLE
                        } else {
                            PatternsUiState.Source.SEED
                        }
                    PatternsUiState.PatternRow(
                        patternId = p.patternId.value,
                        name = p.name,
                        category = p.category.name.lowercase(),
                        version = p.version,
                        source = source,
                        enabled =
                            repos.patternState.isEnabled(
                                p.patternId.value,
                                default = p.enabled,
                            ),
                        triggerCount = triggerInfo?.timesTriggered ?: 0,
                        lastTriggeredAt = triggerInfo?.lastTriggeredAt,
                    )
                }
            _state.value = PatternsUiState(rows = rows, isLoading = false)
        }
    }

    fun setEnabled(
        patternId: String,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            repos.patternState.setEnabled(patternId, enabled, Instant.now())
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            repos.patternState.deleteAll()
        }
    }
}
