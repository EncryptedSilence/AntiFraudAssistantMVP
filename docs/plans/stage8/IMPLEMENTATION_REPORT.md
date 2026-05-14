# Stage 8 — Implementation Report

> Status: **Complete** — all twelve phases landed; `./gradlew checkAll` is green.

This file is the cumulative truth for what Stage 8 actually landed. One section per phase, appended as that phase closed. The Stage 8 close summary at the bottom enumerates the full surface.

---

## Phase 1 (T01–T05) — `:feature:settings` module + `UserSettings` + `RetentionDisplay`

Complete. `:feature:settings` Android-library module added via the `antifraud.android.library` convention plugin, registered in `settings.gradle.kts`, with a permission-less manifest. `androidx.navigation:navigation-compose 2.7.7` entry added to `gradle/libs.versions.toml`. `UserSettings` exposes 17 properties (sensitivity + 14 booleans + onboarding flag + `lastEducationalCardAtMs` Long) backed by `SharedPreferences("antifraud_user_prefs")`, mirroring the Stage 6 `SyncSettings` pattern. `RetentionDisplay.rows()` returns the four §15.2 / §16.10 / §20.1 retention rows.

Tests: `UserSettingsTest`, `UserSettingsTogglesTest`, `RetentionDisplayTest` — all passing.

Commits: T01, T02, T03, T04, T05 + T05-chore (ktlint format).

---

## Phase 2 (T06–T09) — Strings + Nav scaffold

Complete. Stage 1–7 English literals extracted to `app/src/main/res/values/strings.xml`. Empty `values-ru/strings.xml` and `values-kk/strings.xml` stubs land per §24 #11. `AntifraudDestination` sealed class defines five top-level routes + Onboarding + Settings + CampaignDetail. `AntifraudNavGraph` ships the Scaffold + bottom `NavigationBar` shell. `AntifraudApp` Compose root replaces the legacy `StatusScreen`; `MainActivity.setContent { AntifraudApp() }`.

Tests: `AntifraudAppSmokeTest` (5 top-level labels rendered + no account-prompt copy).

Commits: T06, T07, T08, T09.

---

## Phase 3 (T10–T13) — Common UI states

Complete. `EmptyState`, `LoadingState`, `ErrorState` (with action-log "report" button), `PermissionDeniedState` (with Open Settings deep-link), `OfflineState` composables live in `app/src/main/kotlin/com/qalqan/antifraud/ui/state/`. `accessibleTouchTarget` Modifier extension enforces 48 dp minimum (§17.7). Per-composable unit tests pin the rendered text and callbacks.

Commits: T10, T11, T12, T13.

---

## Phase 4 (T15–T18) — Home screen

Complete. `HomeRoute` renders the §17.1 passive status board: current risk band ("All clear" when null), 24 h summary (events/alerts/dismissed), active-campaign card, three quick-action buttons. `HomeUiState` carries `callPermissionState` / `smsPermissionState` / `batteryExempt` / `syncEnabled` / `educationalCardVisible`. `HomeViewModel.refresh` aggregates `Repositories.calls/sms/web.listSince`, `repos.campaigns.listActive()`, `CallObserverPermissions.state()`, `SmsObserverPermissions.state()`, `BatteryOptimizationPrompt.isExempt(...)`, `SyncSettings.enabled`, and the `EducationalCardScheduler.shouldShow(...)` decision. `PermissionStatusRow` composable renders the tri-state call/SMS labels + battery warning + sync status. `HomeHost` inside `AntifraudNavGraph` wires the three quick-action buttons to `SuspiciousCallSheet`, `SuspiciousSmsSheet`, and the existing `WebEntrySheet` — each submission flows through `ManualEntry.calls/sms/web.submit(...)` or `WebManualCapture`.

Acceptance: `Acceptance45ManualEntryReachableHomeTest` (§23 #45 — three quick-action buttons reachable with all auto-capture permissions denied).

Commits: T15, T16, T17, T18.

---

## Phase 5 (T19–T24) — Campaign list + detail

Complete. `CampaignListRoute` renders four tabs (Active / Closed / Archived / False alarm). `CampaignsViewModel` reads `RiskCampaignRepository.listByStatus(...)`. New repo methods: `listByStatus(status)`, `updateStatus(id, status)`, `findById(String)`. New DAO queries match. `CampaignDetailRoute` renders title, start date, last event, risk band, linked events, triggered patterns, ≥ 3 reasons (each tagged `contentDescription = "Reason"` for §23 #17 source-scan), pending questions, recommendations, plus the action row (Close / False alarm / Mark suspicious / Export / Create-pattern gated by `advancedRulesEnabled`). `CampaignDetailViewModel.load` runs `BatchPatternMatcher.matchAll(patterns, events)` over the campaign's events and feeds the triggered pairs into `PatternExplainer.explain(...)` to populate reasons.

Acceptance: `Acceptance17ExplainabilityRendersAtLeastThreeReasonsTest` (§23 #17) + `Acceptance44WizardGatingTest` (§23 #44).

Commits: T19, T20, T21, T22, T23, T24.

---

## Phase 6 (T25–T28) — Patterns screen

Complete. `PatternsRoute` lists every pattern from `SeedPatternLoader.load(syncedPatternsDir = filesDir/sync/current/patterns)` with name / category / version / source (SEED/BUNDLE) / trigger info / enable toggle / "Reset to defaults". `PatternsViewModel` derives the source by inspecting `filesDir/sync/current/patterns/<patternId>.json`. `PatternStateRepository.triggerInfo(patternId)` + `deleteAll()` added; `setEnabled` preserves trigger counters via copy-merge.

Acceptance: `PatternsBundleOverlayTest` (Robolectric drops a JSON into `filesDir/sync/current/patterns/authority_spoof_call_v1.json` and asserts the row tags as `BUNDLE` with the overlaid name + version). `Acceptance14DisablePatternUiTest` (§23 #14 — UI toggle disables the pattern in `PatternStateRepository`).

Commits: T25, T26, T27, T28.

---

## Phase 7 (T29–T31) — References screen

Complete. `ReferencesRoute` renders four tabs (Numbers / Domains / SMS categories / Official). `ReferencesViewModel` reads `ContactProfileRepository.listAll()`, `LookalikeSeedCatalog.seeds`, `SmsCategory.entries`, and the current `BundleManifest` (`createdAt` + `source`) from `filesDir/sync/current/manifest.json` via `BundleManifestJson.parse(...)`. Official-contacts surfaces the `.gov.kz` / `egov.kz` entries from `LookalikeSeedCatalog`.

Acceptance: `ReferencesViewModelTest` (SMS-category enum + lookalike-seeds loading) + `ReferencesBundleManifestTest` (last-update + source from current manifest).

Commits: T29, T30, T31.

---

## Phase 8 (T32–T37) — Privacy + Settings screens

Complete. `PrivacyRoute` renders the four §17.6 sections (what is stored, where it is stored, modules enabled, permissions granted), retention rows from `RetentionDisplay.rows()`, sync status, three action buttons (Delete all data / Disable sync / Reset permissions), plus an Open Settings deep-link. `PrivacyViewModel.deleteAll()` calls `Repositories.wipeAll(bundleStore = BundleStore(app))` and logs `DATA_DELETED`. `disableSync()` flips `SyncSettings.enabled = false`. `resetPermissions()` flips `UserSettings.onboardingCompleted = false`. `SettingsRoute` renders the four sensitivity radios + 14 toggle switches per §18. `SettingsViewModel.setSensitivity` and `setToggle` round-trip through `UserSettings` and log `SETTING_CHANGED`.

Acceptance: `Acceptance20DeleteAllViaPrivacyTest` (§23 #20) + `SensitivityAppliedToScoreTest` (Appendix B coefficients via `UserSettings`).

Commits: T32, T33, T34, T35, T36, T37.

---

## Phase 9 (T38–T42) — Onboarding flow

Complete. `OnboardingStep` enum + pure `OnboardingSequencer` live in `:feature:settings`. §22 Stage 8 ordered list, SDK-aware: NOTIFICATIONS only on API 33+, FULL_SCREEN_INTENT only on API 34+; the other five steps always present. `OnboardingRoute` renders one step at a time with title + justification + Grant / Skip; a Finish CTA shows when the sequencer reports no remaining step. `OnboardingViewModel` advances on Grant/Skip, writes `UserSettings.onboardingCompleted = true` on Finish. `AntifraudApp` start destination chooses Onboarding when `onboardingCompleted = false` and Home otherwise.

**Wiring-contract adaptation (Stage 9 merged before Stage 8 finished):** `OnboardingViewModel.logResult` routes FULL_SCREEN_INTENT and OVERLAY_WINDOW grants/denials through `AlertPermissionResultLogger.log(...)` so the action-log carries the canonical `android.permission.USE_FULL_SCREEN_INTENT` / `android.permission.SYSTEM_ALERT_WINDOW` permission name. Other steps log a generic `PERMISSION_GRANTED` / `_DENIED` with the step name lowercase. Stage 9's `OnboardingPermissionSteps.nextRequiredStep(ctx)` is available for a future host activity that needs the actual `ActivityResultLauncher` intents. The §22 onboarding justifications in `strings.xml` mirror `AlertPermissionRequester.justifications` byte-for-byte so the user-visible copy matches across the two stages.

Acceptance: `Acceptance2NoRegistrationOnFirstLaunchTest` (§23 #2) + `OnboardingSdkAwareSequenceTest` (§22 Stage 8 ordering for API 34 and API 30).

Commits: T38, T39, T40, T41, T42.

---

## Phase 10 (T43–T48) — Question UX

Complete. `QuestionPromptKind` enum (`CALLER_IDENTITY` / `PRESSURE` / `ACTION_REQUEST`) maps 1:1 to the existing `QuestionCode`. `QuestionFatigueGate` (pure) implements §5.5.1 / §5.5.3 (HIGH+ only, ≤ 3 per campaign per 24 h, no re-ask, don't-ask-again, allowed-kinds filter from §18 toggles). `QuestionPromptCard` Compose card renders one question with three answer buttons + Don't-ask-again link. `QuestionViewModel` bridges the gate with `Repositories.answers.save(...)` (writes a `UserAnswer` with the matching `QuestionCode`). `CampaignDetailRoute` renders the next pending prompt above the recommendations row; `CampaignDetailViewModel.load` populates `pendingPrompt` via the same fatigue-gate computation.

Acceptance: `Acceptance18PostCallSmsSiteQuestionsTest` (§23 #18 — once a kind is answered, it's not re-emitted) + `Acceptance42NoQuestionBelowHighTest` (§23 #42) + `Acceptance43AtMostThreeQuestionsPerCampaignTest` (§23 #43).

Commits: T43, T44, T45, T46, T47, T48.

---

## Phase 11 (T49–T53) — Pause modal + Educational cards

Complete. `PauseBeforeActionModal` is a non-cancelable AlertDialog with "I'll pause" + "Show details" actions; surfaces on Home when the active-campaign band is `RiskBand.CRITICAL`. `EducationalCardScheduler` (pure) enforces the §19A 24 h cap; gated by `UserSettings.educationalCardsEnabled`. Five educational cards live in `strings.xml` (`edu_card_1_*` … `edu_card_5_*`). `EducationalCardPager` Compose `HorizontalPager` renders them with a Dismiss button. `HomeViewModel.refresh` populates `educationalCardVisible` and `dismissEducationalCard()` writes `UserSettings.lastEducationalCardAtMs = nowMs`.

Acceptance: `PauseBeforeActionAtCriticalTest` (§11.5 — CRITICAL shows the modal; HIGH hides it) + `EducationalCardOncePer24hTest` (§19A — second show within 24 h suppressed; show after 24 h gap).

Commits: T49, T50, T51, T52, T53.

---

## Phase 12 (T54–T60) — Acceptance harness + Stage close

Complete. `Stage8AcceptanceSuite` aggregates the 12 Stage 8 acceptance tests via `@RunWith(Suite::class)`. `Acceptance2NoExternalStorageTest` extended to include `:feature:settings` and `:feature:alerts` in the manifest source-scan; explicit assertion that `:feature:settings` declares no permissions. `Stage8NoInlineEnglishLiteralsTest` walks the ten new Stage 8 UI packages and confirms every user-facing `Text("…")` literal goes through `stringResource(...)`. ktlint format + detekt suppressions applied; `:app:testReleaseUnitTest` excludes the new Compose-driven Stage 8 UI tests (matches the existing `:app` testOptions pattern). `./gradlew checkAll` is green across every module. `:app:assembleDebug` builds `app/build/outputs/apk/debug/app-debug.apk`. `IMPLEMENTATION_REPORT.md`, `README.md`, `CLAUDE.md`, and `WIKI_UPDATES.md` updated.

Commits: T54, T55, T56, T57, T58, T59, T60.

---

## Stage 8 close summary

- **New module:** `:feature:settings`. Android library; no third-party SDKs beyond what was already on the classpath. Empty manifest — declares zero permissions. Holds `UserSettings`, `OnboardingStep` + `OnboardingSequencer`, `QuestionPromptKind` + `QuestionFatigueGate`, `EducationalCardScheduler`, `RetentionDisplay`.
- **Nav graph:** five top-level destinations (Home / Campaigns / Patterns / References / Privacy) + Onboarding + Settings + CampaignDetail, hosted by a single `MainActivity` (matches §23 #2 single-activity surface).
- **Screens shipped:** §17.1 Home, §17.2 Campaign list + detail, §17.3 Patterns, §17.4 References, §17.6 Privacy, §18 Settings, §22 Stage 8 Onboarding, §5.5 question prompts, §11.5 pause modal, §19A educational cards.
- **Reusable composables:** `EmptyState`, `LoadingState`, `ErrorState`, `PermissionDeniedState`, `OfflineState`, `accessibleTouchTarget`, `QuestionPromptCard`, `PauseBeforeActionModal`, `EducationalCardPager`.
- **§23 acceptance harness:** #2 (no registration), #14 (disabled pattern via UI), #17 (≥ 3 reasons), #18 (no duplicate question per campaign), #20 (delete-all via UI), #42 (no question below HIGH), #43 (3-per-campaign cap), #44 (wizard gating), #45 (manual entry one-tap). Plus §22 Stage 8 onboarding ordering, §11.5 pause-modal gating, §19A 24 h cap.
- **§2.1 boundary:** `:feature:settings` declares no permissions. Source-scan extended to cover both `:feature:settings` and `:feature:alerts`. The only three production manifests declaring `<uses-permission>` are `:feature:calls`, `:feature:sms`, `:core:sync`.
- **Stage 9 wiring contract:** `:feature:alerts`'s `OnboardingPermissionSteps`, `AlertPermissionResultLogger`, and `AlertPermissionRequester.justifications` are the canonical source for the FULL_SCREEN_INTENT + OVERLAY_WINDOW grant flow. The Stage 8 onboarding viewmodel delegates the corresponding step logging to `AlertPermissionResultLogger.log(...)` so the action-log carries the canonical permission name.
- **Deferred:** real-time alert UI primitives (Stage 9 owns the full-screen-intent activity, overlay banner, heads-up notification fallback — all already merged); user-pattern wizard UI (post-MVP §19.17); Russian / Kazakh translations (post-MVP §24 #11); full §8.3 detail levels in `ExportSheet`; the `ActivityResultLauncher` wiring in the onboarding host (placeholder treats Grant / Skip as no-op state advances).
