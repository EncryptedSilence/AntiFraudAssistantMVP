package com.qalqan.antifraud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.demo.BuiltInScenario
import com.qalqan.antifraud.demo.DemoImporter
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.patterns.BatchPatternMatcher
import com.qalqan.antifraud.patterns.PatternExplainer
import com.qalqan.antifraud.patterns.ScenarioPattern
import com.qalqan.antifraud.patterns.SeedPatternLoader
import com.qalqan.antifraud.patterns.WarningLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class StatusViewModel(application: Application) : AndroidViewModel(application) {
    private val repos = Repositories.build(application)
    private val manual = ManualEntry.create(application, repos)
    private val importer = DemoImporter(manual)

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    data class State(
        val calls: Int = 0,
        val sms: Int = 0,
        val web: Int = 0,
        val campaigns: Int = 0,
        val patternsEnabledCount: Int = 0,
        val latestWarningLevel: WarningLevel? = null,
        val latestWarningReason: String? = null,
        val callPermissionsState: com.qalqan.antifraud.calls.CallObserverPermissions.State =
            com.qalqan.antifraud.calls.CallObserverPermissions.State.DENIED,
        val smsPermissionsState: com.qalqan.antifraud.sms.SmsObserverPermissions.State =
            com.qalqan.antifraud.sms.SmsObserverPermissions.State.DENIED,
        val batteryOptimizationExempt: Boolean = false,
    )

    fun runDemo() {
        viewModelScope.launch {
            importer.importBuiltin(getApplication(), BuiltInScenario.FAST_ATTACK)
            refresh()
        }
    }

    fun wipe() {
        viewModelScope.launch {
            repos.wipeAll()
            refresh()
        }
    }

    fun recordSuspiciousCallStub() {
        viewModelScope.launch {
            // Spec §17.1.2 — fallback button that opens the manual-call sheet.
            // Stage 3 ships only the wiring: a no-op insert that proves the button reaches
            // the manual path. The full sheet UI lands in Stage 8.
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
                mapOf("setting" to "manual_call_button", "state" to "tapped"),
            )
        }
    }

    private suspend fun refresh() {
        val callsCount = repos.calls.listSince(Instant.EPOCH).size
        val smsCount = repos.sms.listSince(Instant.EPOCH).size
        val enabledPatterns = loadEnabledPatterns()
        val (warningLevel, warningReason) = computeWarning(enabledPatterns)
        val app = getApplication<Application>()
        _state.value =
            State(
                calls = callsCount,
                sms = smsCount,
                web = 0,
                campaigns = 0,
                patternsEnabledCount = enabledPatterns.size,
                latestWarningLevel = warningLevel,
                latestWarningReason = warningReason,
                callPermissionsState = com.qalqan.antifraud.calls.CallObserverPermissions(app).state(),
                smsPermissionsState = com.qalqan.antifraud.sms.SmsObserverPermissions(app).state(),
                batteryOptimizationExempt = com.qalqan.antifraud.calls.BatteryOptimizationPrompt.isExempt(app),
            )
    }

    private suspend fun loadEnabledPatterns(): List<ScenarioPattern> {
        val seeds = SeedPatternLoader.load()
        return seeds.map { p ->
            val isEnabled = repos.patternState.isEnabled(p.patternId.value, default = p.enabled)
            p.copy(enabled = isEnabled)
        }.filter { it.enabled }
    }

    private suspend fun computeWarning(enabledPatterns: List<ScenarioPattern>): Pair<WarningLevel?, String?> {
        val triggeredPairs = findTriggeredPatterns(enabledPatterns)
        if (triggeredPairs.isEmpty()) return null to null
        val explanation = PatternExplainer.explain(triggeredPairs)
        return explanation.level to explanation.reasons.firstOrNull()?.text
    }

    private suspend fun findTriggeredPatterns(
        enabledPatterns: List<ScenarioPattern>,
    ): List<Pair<ScenarioPattern, com.qalqan.antifraud.patterns.MatchResult>> {
        val events = if (enabledPatterns.isNotEmpty()) collectAllEvents() else emptyList()
        if (events.isEmpty()) return emptyList()
        val matchResults = BatchPatternMatcher.matchAll(enabledPatterns, events)
        return enabledPatterns.zip(matchResults).filter { (_, r) -> r.matched }
    }

    private suspend fun collectAllEvents(): List<RiskEvent> {
        val callEvents = repos.calls.listSince(Instant.EPOCH).map { RiskEvent.Call(it) }
        val smsEvents = repos.sms.listSince(Instant.EPOCH).map { RiskEvent.Sms(it) }
        return callEvents + smsEvents
    }

    override fun onCleared() {
        super.onCleared()
        repos.close()
    }
}
