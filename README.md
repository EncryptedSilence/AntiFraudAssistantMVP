# AntiFraud Assistant — Android MVP

A privacy-respecting Android app that helps users recognize multi-stage social-engineering fraud **before** they share an SMS code, install an attacker's app, transfer money, or open a phishing link.

The app does not use AI of any kind. It does not record audio. It does not read messengers. It does not send user data anywhere by default. It correlates calls, SMS, and other observable signals locally, into multi-day risk campaigns, and intervenes at the moment of risk with a real-time alert.

## Status

Stages 1–7 + Stage 9 complete on `main` (Stage 8 in parallel). Stage 1 (local core) delivered project scaffolding, domain entities, deterministic scoring engine, correlation engine, SQLCipher-encrypted Room database, manual-entry fallback path, application action log, and demo-data import. Stage 2 (pattern engine) added the declarative scenario-pattern engine: typed pattern model, Moshi parser, condition evaluator, AND-of-conditions matcher, ≥3-reasons explainability, pattern state storage (Room schema v2), five in-APK seed patterns, orchestrator integration, and §23 #12 / #14 / #17 acceptance tests. Stage 3 (auto call observer) shipped `:feature:calls`: foreground service of type `phoneCall`, modern (`TelephonyCallback`, API 31+) + legacy (`PhoneStateListener`) listeners, per-`SubscriptionId` multi-SIM registration on Android 12+, `CallLog` reader with five-column allowlisted projection, `CallEventBuilder` reusing the §5.1 normalize-then-salt-hash path shared with `ManualEntry`, `RiskCounterUpdater` for `ContactProfile.riskCounter`, §17.0.3 ongoing transparency notification, permission flow (§4.2.1 runtime pair + `POST_NOTIFICATIONS` on API 33+) and battery-optimization-exemption prompt, with `:app` integration. §23 #25–#28 + §23 #22 (no `RECORD_AUDIO`) + §17.0.3 + §23 #45 + §20.1 all have passing acceptance tests. Stage 4 (auto SMS observer) shipped `:feature:sms`: a manifest-static `BroadcastReceiver` for `SMS_RECEIVED_ACTION` (no default-SMS role), a foreground-only `SmsContentProviderSweeper` recovering broadcasts missed during app standby, `SmsKeywordDetector` + `SmsCategoryClassifier` for on-device body classification, salted-hash sender path shared with `ManualEntry.SmsSubmitter` via `SmsEntryDigest`, multi-SIM `simSlot` propagation, the §17.0.3 transparency notification extended to sum calls + SMS, and `:app` integration via `SmsSweepCoordinator` driven from `MainActivity.onResume`. §23 #29–#34 + §17.0.3 (with SMS counts) + §20.1 (no sender / body in action log) + §2.1 (no `SEND_SMS`, no `RECEIVE_MMS`, no `RECEIVE_WAP_PUSH`, no SMS-provider writes, body excerpt ≤ 200 chars on the auto path) all have passing acceptance tests. Stage 5 (manual web-channel observer) shipped `:feature:web`: deterministic eTLD+1 normalization built on a curated in-APK public-suffix snapshot, Levenshtein-≤-2 lookalike detection against an in-APK catalog of KZ banks / authorities / telecoms, a salted-hash `WebEntryDigest` that shares its per-install salt with `ManualEntry.WebSubmitter` so manual and any future automatic capture produce byte-identical `DomainHash` values, a `WebManualCapture` orchestrator wiring normalization → seen-check → lookalike → status resolution → persist, a `PostSiteQuestionTrigger` that records the §5.5.1 trigger condition at high+ scores (the Q1/Q2/Q3 UI itself stays Stage 8/9), and `:app` integration via a minimal Compose `WebEntrySheet`, a "Web visits captured: N" status-screen counter row, and an "I had a suspicious site" button reachable in one tap. No new permissions are introduced — the web channel is manual-only per v2 §4.3. §23 #5 + #18 + #24 + #45 + §5.4 / §16.4 / §20.1 / §22 Stage-5 budget + §2.1 source-scan (no `Intent.ACTION_VIEW`, no HTTP fetcher, no `WebView`) all have passing acceptance tests. Stage 6 (signed-bundle update channel) shipped two new modules: `:core:crypto` (Tink-backed Ed25519 verifier + SHA-256 helper + `BundleManifest` Moshi parser + canonical sorted-keys JSON serializer + `BundleArchiveReader` with 256 KB / 1 MB DoS caps + `BundleVerifier` enforcing the four §7.4 checks (signature, per-file SHA-256, supported `schemaVersion`, `minAppVersion ≤ appVersionCode`) + `EmbeddedPublicKey` loader from `res/raw`) and `:core:sync` (HTTPS-only `HttpURLConnection` downloader with 5 s connect / 10 s read timeouts and 1 MB body cap + atomic `BundleStore` activate / N=1 rollback / wipe + `SyncSettings` SharedPreferences-backed disable switch defaulting to `enabled=false` + `SyncOrchestrator` that short-circuits before constructing any HTTP socket when disabled + `LocalBundleImporter` for the §7.5 `local` channel + `SeedPatternLoader` overlay reading `filesDir/sync/current/patterns/*.json`). `INTERNET` is declared exclusively in `:core:sync`'s manifest. The `:app` status screen surfaces a sync toggle (default off), a Sync-now button (enabled only when the toggle is on), an Import-local-bundle button via SAF, and a last-synced timestamp. §23 #4 (zero outbound on a default install) + §23 #1 (airplane-mode launch unchanged) + §7.4 (signature / checksum / schema / minAppVersion rejection) + §7.4 (N=1 rollback) + §7.5 (`local` channel tagged regardless of manifest source) + §2.1 (INTERNET only in `:core:sync`, no analytics SDK literals in any production source) + §23 #20 (`Repositories.wipeAll(bundleStore = ...)` clears the synced bundle store) all have passing acceptance tests. Channels `beta` / `enterprise`, PKI / key rotation, N > 1 rollback, scheduled background sync, and update categories beyond patterns are deferred to later stages. Stage 7 (manual data export) shipped `:feature:export`: three §8.2 export categories (`SUSPICIOUS_NUMBERS`, `RISK_CAMPAIGNS`, `TRIGGERED_PATTERNS`) × four §8.5 formats (`TXT`, `MARKDOWN`, `JSON`, `CSV`) × three §8.4 anonymization options (`NumbersLast4`, `DomainZoneOnly`, `DatesDayOnly`) with a mandatory preview-then-write orchestration (§17.5). The `export_profile` table (§16.10) lands in Room schema v4 (`MIGRATION_3_4`). The `ExportSheet` Compose bottom sheet gates the "Save" button on a confirmed preview that matches the current picker selection. No new permissions are introduced; no production manifest declares `WRITE_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`; `:feature:export` is the only `ContentResolver.openOutputStream` consumer (§2.1). `Repositories.wipeAll(...)` now clears `export_profile` rows (§23 #20). §23 #15 (SHA-256 byte-equality between preview and file), §23 #16 (anonymization options strip their target values from every format), §23 #20 (wipeAll clears export rows), §2.1 source scan (no storage permissions in any manifest, only `:feature:export` calls `openOutputStream`), and §23 #1 (export works without network) all have passing acceptance tests. Deferred (post-MVP): user-created pattern export; six remaining §8.4 anonymization options; PDF format; `summary` / `full` / `custom` detail levels; cross-device DB transfer; remaining §8.2 categories. Stage 9 (real-time intervention surface) shipped `:feature:alerts`: a `NotificationChannel` layer (`AlertChannels.CHANNEL_CRITICAL` at `IMPORTANCE_HIGH` + `CATEGORY_CALL` + vibration + alert sound; `CHANNEL_MEDIUM` at `IMPORTANCE_DEFAULT`), a privacy-redacted `AlertContent` data type that rejects raw phone numbers, OTPs, and domains at construction, an `AlertNotificationBuilder` that attaches `Notification.fullScreenIntent` for HIGH / CRITICAL bands and a heads-up only for MEDIUM, a §17.0.1 Compose `CriticalAlertActivity` (`setShowWhenLocked` + `setTurnScreenOn` on API 27+) with a "Pause and verify" deep-link to `MainActivity` and a 5-minute `DismissalCooldown`, an §17.0.2 Compose `OverlayBannerActivity` (single-instance, transparent theme, 30 s auto-dismiss) gated by `OverlayGate.shouldFire(...)` (permission AND band == CRITICAL AND relevant foreground app via `RelevantForegroundAppDetector` with a curated KZ-bank + browser allowlist), an `AlertPipeline.onCallCaptured` / `onSmsCaptured` entry point that runs `CorrelationOrchestrator.absorb` + `CampaignRiskScorer.compute` → `RiskLevel.fromScore` → `AlertBand` → `AlertDispatcher`, a `CampaignCooldown` enforcing the §4.4.4 ≤ 2 alerts / campaign / 24 h ceiling, capture-hook plumbing via `CallObserverService.captureHookFactory` + `SmsBroadcastReceiver.captureHookFactory` companion `var`s installed from `AntiFraudApplication.onCreate` via `AlertWiring.installInto(this)`, an `AppAction.PATTERN_APPLIED` log entry (`source=alert`) per dispatched alert so `PassiveCounters.alertsLast24h` reflects reality and the §17.0.3 ongoing notification refreshes, a `NotificationPermissionGate` enforcing the §4.4.3 loud failure when `POST_NOTIFICATIONS` is denied, a `FullScreenIntentPermissionGate` that falls through to heads-up on Android 14+ when `canUseFullScreenIntent()` is false, an `AlertPermissionRequester` (Settings deep-links + plain-language justification strings for the two §22 Stage-8 alert permissions) and an `OnboardingPermissionSteps` / `AlertPermissionResultLogger` pair as the Stage-8 sequencer's wiring contract, and a JVM latency harness (`AlertLatencyTest` + `AlertLatencySmsTest`) plus an on-device verification script (`scripts/verify-alert-latency.ps1`). `:feature:alerts` adds only `USE_FULL_SCREEN_INTENT` + `SYSTEM_ALERT_WINDOW`; no `RECORD_AUDIO`, no accessibility, no default-Phone / default-SMS / `BIND_INCALL_SERVICE`, no network. §23 #35 (critical-during-call < 2 s), #36 (critical-on-SMS < 3 s), #37 (high < 5 s), #38 (no alert below high), #39 (overlay gating across all four conditions), #40 (loud failure on denied notifications), #41 (passive transparency keeps the §17.0.3 channel and the alert counter live), plus the §2.1 source-scan and merged-manifest scan all have passing acceptance tests. Deferred (post-MVP): per-pattern alert sound customization, standalone "Why this alert" page, snooze beyond the 5-minute cooldown, OEM-aggressive-kill wake-locks. Stage 8 (UX + settings) is in parallel; the Stage 8 `OnboardingSequencer` integration of the Stage 9 alert-permission steps is intentionally left as a wiring contract (`OnboardingPermissionSteps` + `AlertPermissionResultLogger`) for merge-time integration. See [docs/plans/stage1/IMPLEMENTATION_REPORT.md](docs/plans/stage1/IMPLEMENTATION_REPORT.md), [docs/plans/stage2/IMPLEMENTATION_REPORT.md](docs/plans/stage2/IMPLEMENTATION_REPORT.md), [docs/plans/stage3/IMPLEMENTATION_REPORT.md](docs/plans/stage3/IMPLEMENTATION_REPORT.md), [docs/plans/stage4/IMPLEMENTATION_REPORT.md](docs/plans/stage4/IMPLEMENTATION_REPORT.md), [docs/plans/stage5/IMPLEMENTATION_REPORT.md](docs/plans/stage5/IMPLEMENTATION_REPORT.md), [docs/plans/stage6/IMPLEMENTATION_REPORT.md](docs/plans/stage6/IMPLEMENTATION_REPORT.md), [docs/plans/stage7/IMPLEMENTATION_REPORT.md](docs/plans/stage7/IMPLEMENTATION_REPORT.md), and [docs/plans/stage9/IMPLEMENTATION_REPORT.md](docs/plans/stage9/IMPLEMENTATION_REPORT.md) for the per-phase logs.

## What it does

- Captures incoming, outgoing, and missed calls automatically — no default-Phone role required.
- Captures incoming SMS automatically — no default-SMS role required.
- Correlates events into short-window risk sessions and multi-day risk campaigns.
- Matches against a local catalog of fraud-scenario patterns (data, not code).
- Delivers a real-time alert (full-screen notification or overlay banner) at the moment risk crosses the critical threshold.
- Lets the user maintain trusted / suspicious phone-number and domain lists locally.
- Lets the user export selected data manually, with an explicit redaction preview.

## What it never does

- No cloud or on-device AI.
- No audio recording, no speech recognition, no voice analysis.
- No reading of messenger conversations.
- No accessibility-service abuse.
- No background upload of contacts, calls, SMS, domains, or risk history.
- No advertising or tracking SDKs.
- No legal verdict — the app says "possible fraud", never "fraud confirmed".
- No call blocking, no SMS provider writes, no banking-app interference.

## Core architecture

| Pillar | What it means |
|---|---|
| **Passive observation** | Calls and SMS are captured automatically using standard Android runtime permissions (`READ_PHONE_STATE`, `READ_CALL_LOG`, `RECEIVE_SMS`, `READ_SMS`). Manual entry exists only as a permission-denied fallback. |
| **Real-time intervention** | When risk reaches the critical threshold, the app fires a full-screen notification with full-screen-intent (Android `CATEGORY_CALL`); when granted, an overlay banner backs that up if the user is in another foreground app. |
| **Local-only privacy** | All data lives on-device in an encrypted Room + SQLCipher database with keys in Android Keystore. Sync is download-only and user-toggled. |
| **Declarative fraud patterns** | Scenario rules are JSON Schema documents, not executable code. They can be updated independently from the app, signed, validated, enabled / disabled, and locally curated. |

## Planned tech stack

- Kotlin
- Jetpack Compose
- Room + SQLCipher
- Android Keystore
- WorkManager + foreground service (`FOREGROUND_SERVICE_PHONE_CALL`)
- Notification full-screen intent (`USE_FULL_SCREEN_INTENT`) and `SYSTEM_ALERT_WINDOW`
- Ed25519 signatures + SHA-256 checksums for update packages
- HTTPS download-only update client

## Repository layout (today)

```
.
├── .editorconfig
├── .gitignore
├── CLAUDE.md                        (agent-facing project instructions)
├── README.md
├── settings.gradle.kts              (Gradle root + module includes)
├── build.gradle.kts                 (root — clean + checkAll only)
├── gradle.properties
├── gradlew, gradlew.bat
├── gradle/
│   ├── libs.versions.toml           (single source of dependency versions)
│   └── wrapper/
├── build-logic/                     (composite build — convention plugins)
├── config/detekt/                   (detekt rules)
├── app/                             (Android application — Compose status screen, demo importer, pattern-warning surface, §17.7 permission banner)
├── core/
│   ├── domain/                      (JVM — entities per spec §16)
│   ├── scoring/                     (JVM — risk math per §11–12 + Appendix B)
│   ├── correlation/                 (JVM — session + campaign correlators, 14-day horizon, pattern-provider integration)
│   ├── database/                    (Android library — Room v2 + SQLCipher, manual entry, action log, retention purger, pattern_state, CallEntryDigest)
│   ├── demo/                        (Android library — JSON fixture importer for §13 scenarios)
│   ├── patterns/                    (JVM — declarative pattern engine: Moshi parser, condition evaluator, matcher, explainer, 5 in-APK seeds, synced-overlay loader)
│   ├── crypto/                      (Android library — Ed25519 verifier (Tink), SHA-256, `.afpkg` manifest + archive + verifier, embedded public key from R.raw)
│   └── sync/                        (Android library — `INTERNET`-using HTTPS downloader, atomic bundle store + N=1 rollback, user-toggled settings (default off), orchestrator, local-file importer)
├── feature/
│   ├── calls/                       (Android library — auto call observer: foreground service `phoneCall`, telephony listeners, CallLog reader, multi-SIM, action log)
│   ├── sms/                         (Android library — auto SMS observer: manifest-static SMS_RECEIVED receiver, content-provider sweep, classifier, multi-SIM, action log)
│   ├── web/                         (Android library — manual web-channel capture: eTLD+1 normalization, Levenshtein lookalike detection over in-APK KZ catalog, seen-list, salted-hash domain identity, action log)
│   ├── export/                      (Android library — manual data export: three categories × four formats × three §8.4 anonymization options, preview-gated write via SAF, export_profile Room table)
│   └── alerts/                      (Android library — real-time alert surface: notification channels (CRITICAL + MEDIUM), full-screen-intent CriticalAlertActivity, OverlayBannerActivity, AlertPipeline + AlertDispatcher, CampaignCooldown, NotificationPermissionGate, FullScreenIntentPermissionGate)
└── docs/                            (private — gitignored — plans, specs, instructions)
```

(Wiki pages live in a separate repository; see `docs/plans/stage3/WIKI_UPDATES.md` and `docs/plans/stage4/WIKI_UPDATES.md` for the per-stage updates that need to land there.)

## Building

```bash
./gradlew checkAll              # local quality gate: ktlintCheck + detekt + JVM test + Android testDebugUnitTest
./gradlew assembleDebug         # build the debug APK
./gradlew test                  # JVM unit tests across :core:* modules
./gradlew testDebugUnitTest     # Android unit tests (Robolectric)
./gradlew connectedAndroidTest  # device / emulator tests (none yet — used from Stage 3)
```

Windows-equivalent commands and one-time environment setup live in [docs/instructions/dev-setup-windows.md](docs/instructions/dev-setup-windows.md). Test layers and conventions are described in [docs/instructions/test-instructions.md](docs/instructions/test-instructions.md).

## Distribution

- The MVP ships as a stand-alone APK. Google Play distribution is a separate, later decision and will require additional Play-policy work for the restricted SMS / call-log permissions.

## License

To be decided before the first public release. Until a license file is added, no rights are granted to redistribute or modify the code.

## More information

Wiki pages (architectural overview, glossary, roadmap, and topic pages) live in a separate repository.
