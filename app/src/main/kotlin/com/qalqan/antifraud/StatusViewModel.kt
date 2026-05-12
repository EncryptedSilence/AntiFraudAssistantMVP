@file:Suppress("TooManyFunctions")

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
        val syncEnabled: Boolean = false,
        val lastSyncAt: Instant? = null,
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

    fun recordSuspiciousSmsStub() {
        viewModelScope.launch {
            // Spec §17.1.2 — fallback button that opens the manual-SMS sheet.
            // Stage 4 ships only the wiring: a no-op insert that proves the button reaches
            // the manual path. The full sheet UI lands in Stage 8.
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
                mapOf("setting" to "manual_sms_button", "state" to "tapped"),
            )
        }
    }

    fun recordSuspiciousSiteStub() {
        viewModelScope.launch {
            // Spec §17.1.2 — fallback button that opens the manual-site sheet.
            // Stage 5 ships only the wiring marker here; the actual capture happens via
            // submitSiteFromSheet(rawInput, visitedAt) below, called by WebEntrySheet.
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
                mapOf("setting" to "manual_site_button", "state" to "tapped"),
            )
        }
    }

    fun submitSiteFromSheet(
        rawInput: String,
        onResult: (com.qalqan.antifraud.web.WebCaptureOutcome) -> Unit,
    ) {
        viewModelScope.launch {
            val detector =
                com.qalqan.antifraud.web.LookalikeDetector(
                    com.qalqan.antifraud.web.LookalikeSeedCatalog.seeds,
                )
            val builder =
                com.qalqan.antifraud.web.WebEventBuilder(
                    com.qalqan.antifraud.database.manual.WebEntryDigest.create(
                        getApplication(),
                    ),
                )
            val capture =
                com.qalqan.antifraud.web.WebManualCapture(
                    normalizer = com.qalqan.antifraud.web.DomainNormalizer(),
                    detector = detector,
                    seenChecker = com.qalqan.antifraud.web.DomainSeenChecker(repos.web),
                    builder = builder,
                    repo = repos.web,
                    actionLog = com.qalqan.antifraud.web.WebObserverActionLog(repos.actionLogger),
                )
            val outcome = capture.submit(rawInput, java.time.Instant.now())
            onResult(outcome)
            refresh()
        }
    }

    private suspend fun refresh() {
        val callsCount = repos.calls.listSince(Instant.EPOCH).size
        val smsCount = repos.sms.listSince(Instant.EPOCH).size
        val webCount = repos.web.listSince(Instant.EPOCH).size
        val enabledPatterns = loadEnabledPatterns()
        val (warningLevel, warningReason) = computeWarning(enabledPatterns)
        val app = getApplication<Application>()
        val syncSettings = com.qalqan.antifraud.sync.SyncSettings(app)
        _state.value =
            State(
                calls = callsCount,
                sms = smsCount,
                web = webCount,
                campaigns = 0,
                patternsEnabledCount = enabledPatterns.size,
                latestWarningLevel = warningLevel,
                latestWarningReason = warningReason,
                callPermissionsState = com.qalqan.antifraud.calls.CallObserverPermissions(app).state(),
                smsPermissionsState = com.qalqan.antifraud.sms.SmsObserverPermissions(app).state(),
                batteryOptimizationExempt = com.qalqan.antifraud.calls.BatteryOptimizationPrompt.isExempt(app),
                syncEnabled = syncSettings.enabled,
                lastSyncAt = syncSettings.lastSyncAt,
            )
    }

    fun toggleSync() {
        viewModelScope.launch {
            val settings = com.qalqan.antifraud.sync.SyncSettings(getApplication())
            settings.enabled = !settings.enabled
            repos.actionLogger.log(
                com.qalqan.antifraud.domain.AppAction.SETTING_CHANGED,
                mapOf(
                    "setting" to "sync_enabled",
                    "state" to if (settings.enabled) "on" else "off",
                ),
            )
            refresh()
        }
    }

    fun runSyncNow() {
        viewModelScope.launch {
            val settings = com.qalqan.antifraud.sync.SyncSettings(getApplication())
            val orchestrator =
                com.qalqan.antifraud.sync.SyncOrchestrator(
                    settings = settings,
                    downloader = com.qalqan.antifraud.sync.HttpUrlConnectionSyncDownloader(),
                    archiveReader = com.qalqan.antifraud.crypto.BundleArchiveReader(),
                    verifier =
                        com.qalqan.antifraud.crypto.BundleVerifier(
                            com.qalqan.antifraud.crypto.Ed25519SignatureVerifier(),
                            com.qalqan.antifraud.crypto.EmbeddedPublicKey.load(getApplication()),
                        ),
                    store = com.qalqan.antifraud.sync.BundleStore(getApplication()),
                    actionLogger = repos.actionLogger,
                )
            // TODO Stage 8 / 9: replace with a real channel URL once provisioned.
            orchestrator.runOnce("https://example.invalid/")
            settings.lastSyncAt = Instant.now()
            refresh()
        }
    }

    fun importLocalBundle(uri: android.net.Uri) {
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val stream = resolver.openInputStream(uri) ?: return@launch
            val importer =
                com.qalqan.antifraud.sync.LocalBundleImporter(
                    archiveReader = com.qalqan.antifraud.crypto.BundleArchiveReader(),
                    verifier =
                        com.qalqan.antifraud.crypto.BundleVerifier(
                            com.qalqan.antifraud.crypto.Ed25519SignatureVerifier(),
                            com.qalqan.antifraud.crypto.EmbeddedPublicKey.load(getApplication()),
                        ),
                    store = com.qalqan.antifraud.sync.BundleStore(getApplication()),
                    actionLogger = repos.actionLogger,
                )
            stream.use { importer.import(it) }
            refresh()
        }
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
