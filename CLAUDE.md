# AntiFraud — Personal Anti-Fraud Assistant (Android)

## Project status

Stages 1–9 complete. Stage 1 initialized the Kotlin multi-module project: `app/`, `core/{domain,scoring,correlation,database,demo}/`, `build-logic/convention/` (composite build), the version catalog, and ktlint/detekt/JUnit-5/Robolectric tooling wired through convention plugins. The local risk-scoring engine + correlation + encrypted Room database + manual entry + demo fixtures + Stage-1 acceptance tests all land in commits T01–T85. Stage 2 added `:core:patterns` (the declarative pattern engine) and Room schema v2 (`pattern_state` table). The five in-APK seed patterns, condition evaluator, AND-of-conditions matcher, `PatternExplainer`, and `CorrelationOrchestrator` integration land in T01–T52 (Stage 2 numbering). Stage 3 added `:feature:calls`: the auto call observer — foreground service of type `phoneCall`, modern `TelephonyCallback.CallStateListener` (Android 12+) + legacy `PhoneStateListener` (8–11) selected at runtime, per-`SubscriptionId` registration for multi-SIM, `CallLog` reader with allowlisted five-column projection, `CallEventBuilder` reusing the §5.1 normalize-then-salt-hash path shared with `ManualEntry` via the new `CallEntryDigest`, `RiskCounterUpdater`, §17.0.3 ongoing transparency notification, first-run permission flow + battery-optimization-exemption prompt, and `:app` integration. §23 #25–#28 + §23 #22 (no `RECORD_AUDIO`) + §17.0.3 + §23 #45 + §20.1 all have passing acceptance tests. Stage 4 added `:feature:sms`: the auto SMS observer — manifest-static `BroadcastReceiver` for `SMS_RECEIVED_ACTION` (no default-SMS role), foreground-only `SmsContentProviderSweeper` recovering broadcasts missed during app standby (`READ_SMS`), local on-device classification (`SmsKeywordDetector` + `SmsCategoryClassifier`), salted-hash sender path shared with `ManualEntry.SmsSubmitter` via `SmsEntryDigest`, multi-SIM `simSlot` propagation, extended `PassiveCounters` so the §17.0.3 transparency-notification body sums calls + SMS, `SmsSweepCoordinator` driven from `MainActivity.onResume`, and §23 #45 SMS manual-paste affordance. §23 #29–#34 + §17.0.3 (with SMS counts) + §20.1 (no sender / body in action log) + §2.1 (no `SEND_SMS`, no `RECEIVE_MMS`, no `RECEIVE_WAP_PUSH`, no SMS-provider writes, body excerpt ≤ 200 chars on the auto path) all have passing acceptance tests. Stage 5 added `:feature:web`: the manual web-channel observer — deterministic eTLD+1 normalization with a curated public-suffix snapshot, Levenshtein-≤-2 lookalike detection against an in-APK KZ banks/authorities/telecoms catalog, salted-hashed domain identity via `WebEntryDigest` (byte-identical with the existing `ManualEntry.WebSubmitter`), `DomainSeenChecker` for NEW vs KNOWN, `WebManualCapture` orchestrator, `WebObserverActionLog` (records only state markers, never the domain/seed/url), `PostSiteQuestionTrigger` gating Q1/Q2/Q3 at high+ per §5.5.1, and `:app` integration via a minimal Compose `WebEntrySheet` + home-screen counter row + suspicious-site button. §23 #5, #18, #24, #45 + §5.4/§16.4/§20.1/§22 Stage 5 + §2.1 (no URL fetcher, no browser launch, no WebView) all have passing acceptance tests. Stage 6 added `:core:crypto` (Tink-backed Ed25519 verifier + SHA-256 helper + `BundleManifest` Moshi parser + canonical sorted-keys JSON serializer + `BundleArchiveReader` with 256 KB / 1 MB DoS caps + `BundleVerifier` enforcing the four §7.4 checks + `EmbeddedPublicKey` from `res/raw`) and `:core:sync` (HTTPS `HttpURLConnection` downloader + atomic `BundleStore` with N=1 rollback + default-OFF `SyncSettings` + orchestrator short-circuiting when disabled + `LocalBundleImporter` for the §7.5 `local` channel + `SeedPatternLoader` overlay reading `filesDir/sync/current/patterns/*.json`). `INTERNET` is declared exclusively in `:core:sync`'s manifest. §23 #4 (zero outbound on default install) + §23 #1 (airplane-mode launch unchanged) + §7.4 (signature / checksum / schemaVersion / minAppVersion rejection + N=1 rollback) + §7.5 (`local` channel tags regardless of manifest `source`) + §2.1 (`INTERNET` only in `:core:sync`, no analytics SDK literals) + §23 #20 (`Repositories.wipeAll(bundleStore=…)` extension) all have passing acceptance tests. Stage 7 added `:feature:export`: three §8.2 export categories × four §8.5 formats × three §8.4 anonymization options (`NumbersLast4`, `DomainZoneOnly`, `DatesDayOnly`) with a mandatory redaction-preview → SAF-write orchestration (§17.5). `export_profile` table added in Room schema v4 (§16.10). No new permissions; no production manifest declares `WRITE_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE` (§2.1); `:feature:export` is the only `openOutputStream` consumer. `Repositories.wipeAll(...)` clears `export_profile` rows (§23 #20). §23 #15 (SHA-256 byte-equality preview vs. file) + §23 #16 (anonymization strips target values across all formats) + §23 #20 + §2.1 source scan + §23 #1 (export works offline) all have passing acceptance tests. Stage 9 added `:feature:alerts`: notification channels (`CHANNEL_CRITICAL` IMPORTANCE_HIGH + CATEGORY_CALL + vibration + alert sound; `CHANNEL_MEDIUM` IMPORTANCE_DEFAULT), privacy-redacted `AlertContent`, `AlertNotificationBuilder` with full-screen intent for HIGH/CRITICAL bands, §17.0.1 Compose `CriticalAlertActivity` (`setShowWhenLocked` + `setTurnScreenOn` on API 27+, "Pause and verify" deep-link to MainActivity, 5-min DismissalCooldown on dismiss), §17.0.2 Compose `OverlayBannerActivity` (transparent, 30 s auto-dismiss) gated by `OverlayGate.shouldFire` (permission AND CRITICAL AND `RelevantForegroundAppDetector` on a curated KZ-bank + browser allowlist), `AlertPipeline.onCallCaptured` / `onSmsCaptured` → `CorrelationOrchestrator.absorb` + `CampaignRiskScorer.compute` → `RiskLevel.fromScore` → `AlertBand.from` → `AlertDispatcher`, `CampaignCooldown` enforcing §4.4.4 ≤ 2 alerts / campaign / 24 h, capture-hook plumbing via `CallObserverService.captureHookFactory` + `SmsBroadcastReceiver.captureHookFactory` companion vars installed from `AntiFraudApplication.onCreate` via `AlertWiring.installInto(this)`, an `AppAction.PATTERN_APPLIED` log entry (`source=alert`) per dispatched alert so `PassiveCounters.alertsLast24h` reflects reality and the §17.0.3 ongoing notification refreshes, `NotificationPermissionGate` enforcing the §4.4.3 loud failure when `POST_NOTIFICATIONS` is denied, `FullScreenIntentPermissionGate` falling through to heads-up on Android 14+ when `canUseFullScreenIntent()` is false, an `AlertPermissionRequester` + `OnboardingPermissionSteps` + `AlertPermissionResultLogger` stub trio that Stage 8's `OnboardingSequencer` will consume at merge time, a JVM latency harness (`AlertLatencyTest` + `AlertLatencySmsTest`) pinning the four §4.4.2 budgets, and an on-device verification script. `:feature:alerts` declares only `USE_FULL_SCREEN_INTENT` + `SYSTEM_ALERT_WINDOW`; no `RECORD_AUDIO`, no accessibility, no default-Phone / default-SMS / `BIND_INCALL_SERVICE`, no network. §23 #35–#41 + §4.4.4 cooldown + §17.0.1 privacy redaction + §2.1 source / merged-manifest scans all have passing acceptance tests. Stage 8 shipped `:feature:settings` (`UserSettings` `SharedPreferences`-backed 15-property persistence, `OnboardingSequencer` SDK-aware §22 step list, `QuestionFatigueGate` §5.5 rules, `EducationalCardScheduler` §19A cap, `RetentionDisplay`) and the full Compose UI surface in `:app`: five top-level routes (Home / Campaigns / Patterns / References / Privacy) + Onboarding + Settings + CampaignDetail hosted by `AntifraudNavGraph` under a single `MainActivity`. The §17.1 Home renders the passive status board with risk band, 24 h summary, active-campaign card, permission/battery/sync status row, three quick-action buttons routing to Suspicious{Call,Sms}Sheet + the existing `WebEntrySheet`, plus the §11.5 `PauseBeforeActionModal` at CRITICAL and the §19A `EducationalCardPager` (once-per-24 h, settings-gated). §17.2 Campaign list + detail render four tabs / ≥3 reasons via `PatternExplainer.explain(...)` (tagged for §23 #17) / `QuestionPromptCard` (§5.5.2 Q1/Q2/Q3) / action row gated on `advancedRulesEnabled` for §23 #44. §17.3 Patterns detects SEED/BUNDLE source via `filesDir/sync/current/patterns/*.json` overlay + per-pattern toggle + reset-to-defaults. §17.4 References surfaces lookalike-seed domains + `SmsCategory` enum + bundle manifest `createdAt`/`source`. §17.6 Privacy + §18 Settings round-trip through `UserSettings` and log `DATA_DELETED` / `SETTING_CHANGED` via `ApplicationActionLogger`. §22 Stage 8 Onboarding's `OnboardingViewModel` routes FULL_SCREEN_INTENT and OVERLAY_WINDOW grants/denials through Stage 9's `AlertPermissionResultLogger.log(...)` (canonical permission name); the onboarding justifications mirror `AlertPermissionRequester.justifications` byte-for-byte. §23 #2 (no registration), #14 (disabled pattern via UI), #17 (≥3 reasons), #18 (no duplicate question per campaign), #20 (delete-all via UI), #42 (no question below HIGH), #43 (3-per-campaign cap), #44 (wizard gating), #45 (manual entry one-tap) + §22 Stage 8 ordering + §11.5 pause-modal gating + §19A 24 h cap all have passing acceptance tests aggregated into `Stage8AcceptanceSuite`. §2.1 source-scan extended to confirm `:feature:settings` and `:feature:alerts` declare zero permissions (only `:feature:calls` / `:feature:sms` / `:core:sync` declare `<uses-permission>`). See `docs/plans/stage1/`, `docs/plans/stage2/`, `docs/plans/stage3/`, `docs/plans/stage4/`, `docs/plans/stage5/`, `docs/plans/stage6/`, `docs/plans/stage7/`, `docs/plans/stage8/`, and `docs/plans/stage9/` for per-task plans. Per-phase truth: [docs/plans/stage1/IMPLEMENTATION_REPORT.md](docs/plans/stage1/IMPLEMENTATION_REPORT.md), [docs/plans/stage2/IMPLEMENTATION_REPORT.md](docs/plans/stage2/IMPLEMENTATION_REPORT.md), [docs/plans/stage3/IMPLEMENTATION_REPORT.md](docs/plans/stage3/IMPLEMENTATION_REPORT.md), [docs/plans/stage4/IMPLEMENTATION_REPORT.md](docs/plans/stage4/IMPLEMENTATION_REPORT.md), [docs/plans/stage5/IMPLEMENTATION_REPORT.md](docs/plans/stage5/IMPLEMENTATION_REPORT.md), [docs/plans/stage6/IMPLEMENTATION_REPORT.md](docs/plans/stage6/IMPLEMENTATION_REPORT.md), [docs/plans/stage7/IMPLEMENTATION_REPORT.md](docs/plans/stage7/IMPLEMENTATION_REPORT.md), [docs/plans/stage8/IMPLEMENTATION_REPORT.md](docs/plans/stage8/IMPLEMENTATION_REPORT.md), and [docs/plans/stage9/IMPLEMENTATION_REPORT.md](docs/plans/stage9/IMPLEMENTATION_REPORT.md).

## Source of truth

- **Authoritative spec:** [docs/specs/android_antifraud_assistant_spec_v2.md](docs/specs/android_antifraud_assistant_spec_v2.md) (v0.4).
- The earlier v1 ([docs/specs/android_antifraud_assistant_spec_v1.md](docs/specs/android_antifraud_assistant_spec_v1.md)) is kept for traceability only — do not use it for new decisions. v2 §26 lists every diff from v1.
- When the spec and this file disagree, the spec wins. Update this file when spec changes invalidate something here.

## Hard rules — never violate

These come from spec §2.1 and §15. They are non-negotiable; treat any code change that crosses one of these as a stop-and-ask:

- **No AI of any kind.** No cloud AI, no on-device AI, no inference frameworks, no ML model files. Risk scoring is pure rule-based.
- **No background egress.** No telemetry SDKs, no analytics, no crash reporters that send data. Sync is download-only and must be user-toggled.
- **No audio.** Never request `RECORD_AUDIO`. Never analyze voice. Never recognize speech.
- **No accessibility service.** Never request the accessibility role. Never read messenger conversations.
- **No full URLs.** Store eTLD+1 only. No paths, no query strings, no cookies, no logins, no passwords, no banking data.
- **No advertising or tracking SDKs.**
- **No legal verdict.** The app says "possible fraud", never "fraud confirmed".
- **Manual entry is fallback only.** The product value is automatic capture (calls, SMS) plus real-time alerts. Manual paste is a permission-denied / retroactive fallback, not a primary path.

## Planned tech stack (per spec §21)

```
Language        Kotlin
UI              Jetpack Compose
DB              Room + SQLCipher
Keys            Android Keystore
Background      WorkManager + foreground service
Calls (auto)    READ_PHONE_STATE + READ_CALL_LOG + TelephonyCallback
SMS (auto)      RECEIVE_SMS + READ_SMS broadcast receiver + content-provider sweep
Alerts          USE_FULL_SCREEN_INTENT + SYSTEM_ALERT_WINDOW + IMPORTANCE_HIGH
Sync            HTTPS download-only client
Patterns        JSON Schema (see spec Appendix A)
Export          TXT / Markdown / JSON / CSV
Crypto          Ed25519 (signatures) + SHA-256 (checksums)
Min SDK         26 / compileSdk 34 (pinned in build-logic convention plugins)
```

## Module layout

Actual modules (Stage 1 complete):

```
:app                — Android application; Compose status screen wired to demo importer + wipe; Phase-9 acceptance harness lives in :app's test source
:core:domain        — Pure-Kotlin entities per §16, inline value classes for hashed identifiers, no Android deps
:core:scoring       — Risk math per §11–12 + Appendix B, deterministic, no Android deps
:core:correlation   — Session window selection, link signals, session + campaign correlators, 14-day horizon (§3, §10), no Android deps
:core:database      — Room + SQLCipher with Keystore-bootstrapped DB key. Subpackages: crypto/ (CryptoBox interface + KeyStoreCryptoBox + InMemoryCryptoBox), manual/ (ManualEntry fallback path + PhoneNormalizer + Hashing + OtpAndIdGuard), log/ (ApplicationActionLogger). Plus retention purger.
:core:demo          — JSON-fixture importer for the three §13 scenarios (fast-attack, sms-then-call, trust-grooming)
build-logic         — Composite build with three convention plugins (KotlinJvm, AndroidLibrary, AndroidApplication)
```

Modules planned for later stages (per spec §21 + the per-stage plans):

| Module | Stage | Purpose |
|---|---|---|
| `:core:patterns` | 2 | JSON Schema validator, pattern catalog, signature verification (§6) |
| `:core:crypto` | 6 / 7 | Keystore, Ed25519 verify, SHA-256 — needed by sync (§7) and export (§8) |
| `:feature:calls` | 3 | Call observer (foreground service `phoneCall`, TelephonyCallback) — §4.2.1 |
| `:feature:sms` | 4 | SMS broadcast receiver + content-provider sweep — §4.2.2 |
| `:feature:web` | post-MVP | Web channel; the channel decision (VPN / accessibility / fallback) is open per §4.3 |
| `:feature:alerts` | 9 | Full-screen intent activity + overlay banner + ongoing notification (§4.4, §17.0) |
| `:feature:export` | 7 | Redaction preview + writers (§8) |
| ~~`:feature:settings`~~ | ~~8~~ | Landed in Stage 8 — see project status above. |
| `:feature:patterns` | 2 | System pattern UI |

Note on `:core:correlation`: an earlier draft of this section called out a `:feature:campaigns` module for RiskSession / RiskCampaign correlation. That responsibility has been folded into `:core:correlation` for unit-test speed (no Android deps).

## Commands

Canonical commands for the project — runnable as of Stage 1 close. The aggregated `checkAll` is the recommended pre-commit gate; the rest are for narrower runs. On Windows, the bootstrap JDK is at `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot` and the Android SDK at `C:\AndroidDev`; the gitignored `local.properties` points to the SDK.

`JAVA_HOME` is preset for every PowerShell tool call via `.claude/settings.json` → `env`. Do **not** prepend `$env:JAVA_HOME = ...; $env:Path = ...; cd ...;` to gradle commands — invoke `.\gradlew.bat <task>` (or `./gradlew <task>` under Bash) directly. The working directory is already the project root. Wrapping commands in env-var setup defeats the project's permission allow-list (which is prefix-matched on `.\gradlew.bat`) and forces a permission prompt on every variation.

| Task | Command (Windows / PowerShell) | Command (Bash / WSL) |
|---|---|---|
| Local quality gate (ktlint + detekt + all unit tests) | `.\gradlew.bat checkAll` | `./gradlew checkAll` |
| Build debug APK | `.\gradlew.bat assembleDebug` | `./gradlew assembleDebug` |
| Build release APK (unsigned) | `.\gradlew.bat assembleRelease` | `./gradlew assembleRelease` |
| Unit tests | `.\gradlew.bat test` | `./gradlew test` |
| Instrumented tests (device or emulator attached) | `.\gradlew.bat connectedAndroidTest` | `./gradlew connectedAndroidTest` |
| Lint | `.\gradlew.bat lint` | `./gradlew lint` |
| Kotlin format check | `.\gradlew.bat ktlintCheck` | `./gradlew ktlintCheck` |
| Kotlin format fix | `.\gradlew.bat ktlintFormat` | `./gradlew ktlintFormat` |
| Static analysis | `.\gradlew.bat detekt` | `./gradlew detekt` |
| Dependency check | `.\gradlew.bat dependencies` | `./gradlew dependencies` |
| Install on attached device | `adb install -r app\build\outputs\apk\debug\app-debug.apk` | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |
| Logcat (just our package) | `adb logcat -s AntiFraud:V` | `adb logcat -s AntiFraud:V` |

When you change a build command, update this table and the matching settings.json allow-list entry.

## Spec navigation — fastest paths

| Topic | Spec section |
|---|---|
| Privacy hard limits | §2 |
| Risk model (events / sessions / campaigns) | §3 |
| Operating mode (passive-by-default) | §4.1 |
| Auto call capture (APIs + permissions) | §4.2.1 |
| Auto SMS capture (APIs + permissions) | §4.2.2 |
| **Real-time alert surface (essential)** | **§4.4** |
| Web channel (decision open) | §4.3 |
| Question UX (3 yes/no, only at high+) | §5.5 |
| Pattern format (JSON Schema) | §6 + Appendix A |
| Update / sync model | §7 |
| Export model | §8 |
| Correlation rules | §10 |
| Scoring formulas | §11–12 |
| Local storage + retention | §15 |
| Data model | §16 |
| Outside-the-app surfaces | §17.0 |
| Home screen layout (passive-first) | §17.1 |
| Acceptance criteria (47 testable items) | §23 |
| Pinned MVP / post-MVP decisions | §24 |
| Permission matrix | Appendix C |
| Glossary | Appendix D |

## Conventions

- **English everywhere in code and comments.** UI strings live in `strings.xml` resources; the spec assumes Russian primary, with Kazakh / English added post-MVP (§24 #11).
- **Default to writing no comments.** Code should be self-documenting; comment only when *why* is non-obvious (a hidden constraint, a workaround for a specific OEM bug, a privacy boundary that a future reader must not cross).
- **Privacy boundary tests are not optional.** Every feature that touches a privacy hard rule (§2.1) must come with a test that proves the hard rule is held. Examples: a test that the SMS body excerpt never exceeds 200 chars; a test that exported JSON contains exactly what the redaction preview displayed.
- **No `WRITE_EXTERNAL_STORAGE`, no `MANAGE_EXTERNAL_STORAGE`.** Exports go through `ACTION_CREATE_DOCUMENT`.
- **Foreground services declare their type.** On Android 14+ this is mandatory; the call-observer service uses `phoneCall`, the alert service uses `dataSync`. Both must be advertised in the app's foreground notification copy.
- **No commits unless asked.** Default behavior — even when work feels finished. Confirm before committing or pushing.

## Documentation routine

When a phase or stage's exit criteria are met — or when toolchain, test layout, or visible repo state changes mid-stage — update the affected docs in the same change. Don't defer; stale docs lie about the codebase.

| Trigger | Update |
|---|---|
| Phase commit changes visible repo state (new modules, status moved off "pre-implementation") | [README.md](README.md) — "Status" line and "Repository layout" tree |
| Architecture, data flow, or privacy boundary changes | [wiki/Home.md](wiki/Home.md) |
| Phase or stage exit criteria met | append a phase entry to `docs/plans/stage<N>/IMPLEMENTATION_REPORT.md` (create if absent). Cumulative within a stage — earlier entries stay |
| Toolchain (JDK / SDK / env vars / Gradle) changes | [docs/instructions/dev-setup-windows.md](docs/instructions/dev-setup-windows.md) |
| Test layers, commands, quality gates, or categories change | [docs/instructions/test-instructions.md](docs/instructions/test-instructions.md) |
| New §2.1 privacy boundary crossed | confirm a matching privacy-boundary test exists; if not, add it to the failing-test backlog before continuing |

Don't write a doc update for every task — only when something a future reader needs to know has changed.

## When using superpowers skills

The skills bundled with this environment are process-level (TDD, brainstorming, debugging, code review). They are not Android-specific and need no setup. Use them as you would in any project. Notes specific to this codebase:

- For new feature work involving anything in §4.2 / §4.4 (call / SMS observation, alert surfaces), invoke `superpowers:brainstorming` first — those areas have OEM quirks and Android version differences that benefit from explicit option exploration before code.
- `superpowers:test-driven-development` is the default discipline. The scoring module (`:core:scoring`) is the easiest place to start because it has no Android dependencies.
- `superpowers:verification-before-completion` is mandatory for anything claiming an alert latency target (§4.4.2). "I implemented the full-screen intent" is not the same as "I observed a < 2 s alert on a real device".
- `claude-api` is the only bundled tech-aware skill, and it does **not** apply here — `§2.1` forbids any AI integration.

## Repo navigation

The full repo layout tree lives in [README.md](README.md#repository-layout-today) — keep that one in sync, don't duplicate it here. Agent-facing pointers:

- Spec: [docs/specs/android_antifraud_assistant_spec_v2.md](docs/specs/android_antifraud_assistant_spec_v2.md) (authoritative); v1 sibling kept for traceability only.
- Plans: [docs/plans/stage{1..9}/](docs/plans/) — each stage folder holds the plan files plus an `IMPLEMENTATION_REPORT.md` updated as phases close.
- Instructions: [docs/instructions/dev-setup-windows.md](docs/instructions/dev-setup-windows.md), [docs/instructions/test-instructions.md](docs/instructions/test-instructions.md).
- Convention plugins: [build-logic/convention/](build-logic/convention/) — touch these to change build / quality wiring across all modules at once.
- Project permissions: `.claude/settings.json` (tracked), `.claude/settings.local.json` (user-local, not tracked).
