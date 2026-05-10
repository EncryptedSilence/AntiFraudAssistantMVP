# AntiFraud Assistant — Wiki

This is the entry point for project documentation. The repository's [README](../README.md) covers the elevator pitch and the published surface area; this wiki is for architecture, design rationale, and the moving parts a contributor or reviewer needs to navigate the codebase.

> The detailed technical specification is currently kept private while it is in active revision. Stable sections will be migrated here as they settle.

## Stage status

| Stage | Scope | Status |
|---|---|---|
| 1 | Local core: project scaffold, domain entities, deterministic scoring, correlation engine, encrypted Room DB, manual-entry fallback, action log, demo fixtures | **Complete** |
| 2 | Declarative pattern engine: typed model, Moshi parser, condition evaluator, AND-of-conditions matcher, ≥3-reasons explainer, pattern state storage (Room v2), 5 in-APK seed patterns, orchestrator integration | **Complete** |
| 3 | Calls — automatic capture (`READ_PHONE_STATE` + `READ_CALL_LOG` + `TelephonyCallback`, foreground service `phoneCall`) | Planned |
| 4 | SMS — automatic capture (`RECEIVE_SMS` + `READ_SMS` broadcast + content-provider sweep) | Planned |
| 5 | Web channel | Planned — see [Web channel](Web-channel.md) (decision open) |
| 6 | Sync + signing — HTTPS download-only client, Ed25519 signature verification, signed pattern catalog updates | Planned — see [Update packages and signing](Update-packages-and-signing.md) |
| 7 | Export — TXT / Markdown / JSON / CSV with mandatory redaction preview | Planned |
| 8 | Settings UX — sensitivity, retention, permission state, pattern enable/disable UI | Planned |
| 9 | Real-time intervention — full-screen intent + overlay banner + ongoing notification | Planned — see [Real-time alert surface](Real-time-alert-surface.md) |

Per-phase truth for a completed stage lives in `docs/plans/stage<N>/IMPLEMENTATION_REPORT.md` (gitignored — local on the maintainer's clone).

## What this project is

A multi-stage fraud is rarely a single bad call or a single phishing SMS. It is a sequence: an unknown call with no immediate ask, then an SMS with a code two minutes later, then another call referencing that SMS, then a "support agent" asking the user to install an app or read a code aloud. By the time an individual step looks alarming, the user is already three steps into the funnel.

This Android app builds a local memory of those steps and intervenes at the moment one of them becomes dangerous. It does so without AI, without recording anything, and without sending the user's data anywhere by default.

## Three pillars

### 1. Passive observation

Real users will not paste their calls or SMS into a fraud-detection app. Manual entry is dead weight in production. The app observes the relevant signals automatically, using standard Android runtime permissions, and never asks the user to log anything as a primary flow.

| Signal | Mechanism | Permission set |
|---|---|---|
| Calls (incoming, outgoing, missed) | `TelephonyCallback.CallStateListener` + call-log read on idle | `READ_PHONE_STATE`, `READ_CALL_LOG`, `FOREGROUND_SERVICE_PHONE_CALL` |
| SMS (incoming) | `BroadcastReceiver` for `SMS_RECEIVED_ACTION`, plus a content-provider sweep to recover broadcasts missed during app standby | `RECEIVE_SMS`, `READ_SMS` |
| New domains | Decision currently open: VPN-based DNS sinkhole, accessibility-based address-bar reading, or accepted blind spot. See [Web channel](Web-channel.md). | t.b.d. |

What the app explicitly does **not** do:

- request the default-Phone, default-Call-Screening, or default-SMS roles;
- record or analyze call audio;
- read messenger conversations or use the accessibility service for that purpose;
- store full URLs, query parameters, cookies, logins, passwords, or banking data.

### 2. Real-time intervention

Observation without intervention has the same failure mode as manual entry: the user finds out too late. The app therefore treats the alert surface as a first-class part of the architecture, not as a notification afterthought.

- **Primary surface** — a full-screen notification with a full-screen intent (`USE_FULL_SCREEN_INTENT`) on a dedicated channel marked `CATEGORY_CALL`. The OS treats it with the same priority as an incoming call, which is what is needed: the user must be interrupted while the risky action is happening.
- **Secondary surface** — a `SYSTEM_ALERT_WINDOW` overlay banner, fired only at the critical-risk level and only when the user is currently in a relevant foreground app (a banking app, a browser on a known-fraud lookalike domain). Detected via `UsageStatsManager`.
- **Passive transparency** — the always-running observer foreground service shows a low-priority ongoing notification (`Watching for fraud signals — last 24 h: N events, M alerts`) so the user can see at a glance what the app is doing and why it has the permissions it has.

Latency budgets are concrete and tested:

| Trigger | Target |
|---|---:|
| Critical-risk decision during an active call | < 2 s after the score crosses 80 |
| Critical-risk decision triggered by SMS arrival | < 3 s after the broadcast |
| High-risk decision on call IDLE or SMS arrival | < 5 s |
| Medium / low | no notification with sound or vibration |

### 3. Local-only privacy

- All data lives on-device in an encrypted Room + SQLCipher database, with keys held in Android Keystore.
- Phone numbers and domains are stored as a salted-hash pair plus an encrypted plaintext display value. Display works; comparison and export-anonymization both work without leaking the original.
- SMS bodies are stored truncated to 200 characters, encrypted, and clearable per-message.
- Sync is download-only and user-toggled. Update packages are signed (Ed25519, single embedded public key) and rejected if signature, schema version, or checksum is wrong.
- Export is always manual, always shows a redaction preview before writing the file, and never includes data the user did not see in that preview.

## Risk model — at a glance

The app uses a deliberately simple, deterministic scoring model. There is no AI, no heuristic that requires explanation. The contributing scores are auditable and the cap behavior is explicit.

```
EventRisk          = min(100, BaseRisk + ContextRisk + UserAnswerRisk)

SessionRisk        = round(0.35*CallRisk + 0.30*SmsRisk + 0.20*WebRisk + 0.15*AnswerRisk)

CampaignRiskScore  = round(min(100,
                       sum_i(EventRisk_i * TimeDecay_i * LinkStrength_i)
                       + PatternRisk))
```

| Score | Level | App action |
|---:|---|---|
| 0–30 | low | log only |
| 31–60 | medium | soft notification |
| 61–80 | high | full-screen alert + at most three yes/no questions per campaign |
| 81–100 | critical | full-screen alert + overlay banner if granted; "pause before action" modal |

`UserAnswerRisk` only contributes via the event the answer is attached to — it is not double-counted at the campaign level. `PatternRisk` is the aggregate weight of triggered scenario patterns, capped at 60 per campaign.

## Scenario patterns

Fraud scenarios are not hard-coded. Each is a JSON document parsed into a typed `ScenarioPattern`. A pattern declares:

- the kinds of events it cares about (`CallEvent`, `SmsEvent`, `WebEvent`, `UserAnswerEvent`; the spec also reserves `ContactEvent`, `ManualEvent`, `PatternEvent` for later stages);
- per-condition fields, operators (`equals`, `in`, `greaterThan`, `lessThan`, `contains`, `matches`), allowed values, weights, and optional time windows;
- a correlation block (max campaign age, link strength);
- a warning level (`medium` / `high` / `critical`) + title + message;
- versioning, source, and a future signature-status field.

Patterns can be enabled or disabled per-pattern and reset to defaults — both backed by a Room table (`pattern_state`) since Stage 2. The signed `stable` channel and the `local` import path land with Stage 6 sync. User-created patterns are a power-user feature behind a Settings toggle and are deferred to post-MVP.

Stage 2 ships five in-APK seed patterns covering bank-fraud, authority-spoof, delivery-scam, lookalike-domain, and multi-stage pressure scenarios. See the [Pattern engine](Pattern-engine.md) page for the matcher semantics, weight cap, and explanation surface.

## Module layout

### Modules shipped

```
:app                 Compose status screen, demo importer, pattern-warning surface
:core:domain         entities per spec §16 — pure JVM
:core:scoring        risk math per §11–12 + Appendix B — pure JVM
:core:correlation    session + campaign correlators, 14-day horizon, pattern-provider integration — pure JVM
:core:database       Room v2 + SQLCipher, manual entry, action log, retention purger, pattern_state — Android library
:core:demo           JSON fixture importer for the §13 scenarios — Android library
:core:patterns       declarative pattern engine: parser, condition evaluator, matcher, explainer, 5 in-APK seeds — pure JVM
build-logic          composite build with three convention plugins (KotlinJvm, AndroidLibrary, AndroidApplication)
```

### Modules planned

```
:core:crypto         Keystore, Ed25519 verify, SHA-256 — Stage 6 (sync) + Stage 7 (export)
:feature:calls       call observer (TelephonyCallback + foreground service phoneCall) — Stage 3
:feature:sms         SMS broadcast receiver + content-provider sweep — Stage 4
:feature:web         web-channel module — Stage 5 (decision open: see Web-channel.md)
:feature:alerts      full-screen intent activity, overlay banner, ongoing notification — Stage 9
:feature:export      redaction preview + writers (TXT / Markdown / JSON / CSV) — Stage 7
:feature:settings    sensitivity, retention, permission state — Stage 8
:feature:patterns    system-pattern enable / disable UI — Stage 8
```

`RiskSession` / `RiskCampaign` correlation lives inside `:core:correlation` rather than a `:feature:campaigns` module — pure-JVM unit-test speed (no Android deps) was the deciding factor.

## Roadmap

The MVP scope is intentionally narrow. Features marked **post-MVP** are valuable but deferred to keep the demo focused.

### MVP

- automatic call capture (no default-Phone role)
- automatic SMS capture (no default-SMS role)
- local risk-session and risk-campaign correlation
- shipped + signed scenario-pattern catalog with at least five seed patterns
- real-time intervention surface (full-screen notification + overlay banner + ongoing notification)
- pause-before-dangerous-action modal at critical risk
- educational cards (5)
- lookalike-domain Levenshtein check against ~20 known KZ banks and services
- redaction-preview-mandatory export to TXT / Markdown / JSON / CSV
- demo data import for pilot demonstrations

### Post-MVP

- web-channel detection (decision pending: VpnService vs accessibility vs documented blind spot)
- default-SMS role for SMS-provider writes (only if the product later needs to write to the inbox)
- default-Phone or default-Call-Screening roles (only if blocking / pre-screening is added)
- SMS format-authenticity module (signed local catalog of canonical bank / authority SMS templates, format-deviation scoring without AI)
- user-pattern wizard (gated behind a Settings "Enable advanced rules" toggle)
- enterprise / authority bundle channel
- formal export templates for institutions
- simplified UI for elderly users
- encrypted local backup
- family mode

## Glossary

- **RiskEvent** — a single observed occurrence (call, SMS, web visit, user answer, manual entry, pattern trigger).
- **RiskSession** — a short-window grouping of related events (5 min – 24 h).
- **RiskCampaign** — a multi-day grouping of related events with a 14-day active correlation horizon.
- **EventRisk** — a single event's score, capped at 100, combining base, contextual, and user-answer signals.
- **CampaignRiskScore** — the aggregated 0–100 score for a campaign, applied against the four risk levels above.
- **LinkStrength** — coefficient (0–1) describing how strongly two events are correlated.
- **TimeDecay** — coefficient (0–1) reducing the contribution of older events.
- **ScenarioPattern** — a declarative JSON document describing a fraud scheme. Cannot contain executable code.
- **System pattern** — pattern shipped with the app or via a signed update.
- **User pattern** — pattern created locally by the user (post-MVP).
- **`stable` channel** — pattern / reference data downloaded from the trusted single signed source.
- **`local` channel** — pattern / reference data the user imports from a file.
- **MVP** — the demo deliverable scoped in this project; not the production / Google Play release.
- **Active correlation horizon** — 14 days. Events older than this do not affect current risk, although they may still be in the 30-day archive.

## Topic pages

Pages cover one concept each. Stubs are explicit about what is decided vs. what is open; they fill in as the relevant stage closes.

- [Pattern engine](Pattern-engine.md) — matcher semantics, weight cap, explainability surface. Reflects Stage 2 reality.
- [Privacy boundaries](Privacy-boundaries.md) — the §2.1 hard rules and the tests that prove they hold.
- [Permissions and onboarding](Permissions-and-onboarding.md) — what each permission unlocks, what is requested when, what happens on denial. Stub.
- [Web channel](Web-channel.md) — VPN sinkhole vs. accessibility vs. accepted blind spot. **Decision open.**
- [Real-time alert surface](Real-time-alert-surface.md) — channel configuration, full-screen-intent activity, overlay banner, OEM matrix. Stub.
- [Update packages and signing](Update-packages-and-signing.md) — package format, Ed25519 verification, rollback semantics. Stub.
- [Acceptance test plan](Acceptance-test-plan.md) — §23's 47 testable criteria mapped to automated and manual checks.
