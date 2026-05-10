# Pattern engine

The pattern engine is how the app turns a campaign's events into named, explainable risk. Stage 2 shipped the full local pipeline; signing, sync, and a user-pattern UI come later.

> Authoritative source: spec §6 + Appendix A. When this page disagrees with the spec, the spec wins.

## What a pattern is

A `ScenarioPattern` is a JSON document parsed into a typed Kotlin value object. It carries no executable code — only declarative fields the matcher reads.

```json
{
  "patternId": "bank_security_otp_after_call_v1",
  "name": "Bank security service / OTP after a call",
  "category": "bankFraud",
  "version": "1.0.0",
  "enabled": true,
  "userCreated": false,
  "source": "system",
  "conditions": [
    { "eventType": "CallEvent", "field": "isKnownContact", "operator": "equals", "value": false, "weight": 20 },
    { "eventType": "SmsEvent",  "field": "containsCode",   "operator": "equals", "value": true,  "timeWindowHours": 24, "weight": 30 }
  ],
  "correlation": { "maxCampaignAgeDays": 14, "linkStrength": 0.9 },
  "warning": {
    "level": "high",
    "title": "Possible fraud scheme",
    "message": "An unknown call is linked with an SMS code. Do not share the code with anyone."
  },
  "recommendation": "If someone asks for an SMS code, hang up and verify by calling your bank's official number."
}
```

| Field | Bounds (Appendix A) | Notes |
|---|---|---|
| `name` | 1–120 chars | non-blank |
| `description` | ≤ 1000 chars | optional |
| `version` | semver (`\d+\.\d+\.\d+`) | strict — no `-rc1` suffixes |
| `conditions` | ≥ 1 | empty list rejected at parse time |
| `condition.weight` | 0–100 | per-condition |
| `condition.timeWindowHours` | 1–336 (= 14 days) when present | optional |
| `warning.title` | 1–80 chars | |
| `warning.message` | 1–600 chars | |
| `correlation.maxCampaignAgeDays` | 1–14 | default 14, aligned with the 14-day active horizon (§11.4) |
| `correlation.linkStrength` | 0.0–1.0 | default 0.0 |
| `recommendation` | ≤ 600 chars | optional |

Bounds are enforced in the data class `init {}` blocks. The Moshi reflective parser plus those `init` blocks together replace a runtime JSON Schema validator — there is no third-party schema runtime.

## Operators (Appendix A)

The matcher dispatches on six operators. Each takes the value the per-event-type accessor returns (Boolean, Number, String, or null when the field is unknown):

| Operator | Behavior | Type-mismatch behavior |
|---|---|---|
| `equals` | value-equality | type mismatch → false |
| `in` | list contains | non-list `value` → false |
| `greaterThan` / `lessThan` | numeric comparison after `Number.toDouble()` widening | non-numeric on either side → false |
| `contains` | string `haystack.contains(needle)` | non-string on either side → false |
| `matches` | regex `Regex(pattern).matches(actual)` | non-string actual or invalid regex → false |

Non-matches return `false` rather than throwing — pattern interpretation is fault-tolerant by design (§6.4: "the interpreter must reject any unknown operator or field"; we do that at parse time, then keep the matcher permissive at evaluation time).

## Event types

Stage 2 ships interpreters for the four event types Stage 1 already produces data for. The other three Appendix-A types are recognized in JSON but **rejected at parse time** so a downloaded pattern referencing them cannot be silently ignored.

| Event type | Interpreter shipped? | Fields the matcher resolves |
|---|---|---|
| `CallEvent` | yes | `isKnownContact`, `isRepeated`, `direction`, `durationSec`, `simSlot` |
| `SmsEvent` | yes | `containsCode`, `containsLink`, `containsFinancialKeyword`, `containsSecurityKeyword`, `smsCategory`, `simSlot` |
| `WebEvent` | yes | `domainDisplayLocal`, `isNewDomain`, `domainStatus`, `webRiskScore` |
| `UserAnswerEvent` | yes | `questionCode`, `answerCode` |
| `ContactEvent`, `ManualEvent`, `PatternEvent` | no — parse rejects | landing in later stages |

Enums are surfaced as their `.name` (e.g. `"INCOMING"`, `"OTP"`, `"NEW"`) so the JSON `value` can match a string. Unknown fields return `null` from the accessor, which the evaluator treats as `false`.

## Matching semantics

`PatternMatcher.match(pattern, events): MatchResult` — runs one pattern against a campaign's events.

- **AND of conditions.** Every condition must find at least one matching event in the input list.
- **Disabled patterns never trigger.** Acceptance §23 #14.
- **Time-window scoping.** When a condition has `timeWindowHours`, the matcher considers only events whose `occurredAt` is within that window of the *most recent* event in the input list. The boundary is inclusive (an event exactly N hours before the newest is in window).
- **Weight aggregation cap.** Triggered weight is the sum of triggered condition weights, capped at **60** (`MatchResult.PATTERN_RISK_CAP`) — the same cap `:core:scoring`'s `PatternRisk` enforces (§11.1 / §11.4).
- **First-match-per-condition.** The matcher records the first event satisfying each condition into `triggeringEventIds`, in declared condition order. The explainer uses that list.

`BatchPatternMatcher.matchAll(patterns, events)` returns one `MatchResult` per pattern in input order. `BatchPatternMatcher.triggeredWeights(...)` filters to matched patterns and returns their weights — the shape `CampaignRiskScorer.compute(..., triggeredPatternWeights = ...)` already accepts.

## Explanation surface (§14)

Per §14.1: *every warning must explain the cause, listing at least three specific reasons or all available reasons (whichever is fewer).*

`PatternExplainer.explain(matches)` consumes `(pattern, MatchResult)` pairs filtered to matched patterns and produces an `Explanation`:

- `level` is the maximum `WarningLevel` across triggered patterns (`MEDIUM < HIGH < CRITICAL`).
- `reasons` is the deduped (by text), ordered list of per-condition phrases produced by `ConditionPhraser`.
- Reason count = `min(targetMin, totalTriggeredConditions)` where `targetMin = 3`. When fewer than 3 conditions triggered across the matched set, all available reasons are emitted.

`ConditionPhraser` is the single source of truth for what each `(eventType, field, operator, value)` tuple means in human English. Phrases are deliberately terse and locale-neutral; the §17 UI may substitute localized phrasings later.

## Pattern state

Per-pattern enable/disable is persisted in the `pattern_state` Room table (schema v2, Stage 2):

| Column | Type | Notes |
|---|---|---|
| `pattern_id` | TEXT PRIMARY KEY | matches `ScenarioPattern.patternId.value` |
| `enabled` | INTEGER NOT NULL | overrides the seed default |
| `updated_at` | TEXT NOT NULL | ISO-8601 `Instant` |

`PatternStateRepository` exposes `isEnabled(patternId, default)`, `setEnabled(patternId, enabled, at)`, and `resetToDefaults()`. Reset wipes the override table, so subsequent reads fall back to the seed JSON's `enabled` field. `Repositories.wipeAll()` clears `pattern_state` alongside the event tables; the retention purger (Stage 1) does **not** touch this table — overrides survive 30-day retention.

## Seed catalog

Five system patterns ship inside the APK under `core/patterns/src/main/resources/patterns/seed/`:

| `patternId` | Category | Warning level | Trigger sketch |
|---|---|---|---|
| `bank_security_otp_after_call_v1` | bankFraud | high | unknown call + SMS containing code within 24 h |
| `unknown_call_then_link_sms_v1` | deliveryScam | high | unknown call + SMS containing link within 24 h |
| `authority_spoof_call_v1` | authoritySpoof | high | unknown call + user answers Q1 ("caller claimed to be from a bank or authority") = YES |
| `new_lookalike_domain_visit_v1` | bankFraud | high | new domain visit + domain regex matches the major KZ banks (placeholder until Stage 5's Levenshtein lookalike check lands) |
| `multistage_pressure_campaign_v1` | unknownSocialEngineering | critical | unknown call + user answers Q3 ("asked to act now") = YES |

`SeedPatternLoader.load()` reads them from the classpath (`Class.getResourceAsStream`) so unit tests run without an Android context. The list is fixed at compile time; downloadable patterns arrive via the Stage 6 sync channel.

## Wiring into the orchestrator

`CorrelationOrchestrator` accepts a `patternProvider: () -> List<ScenarioPattern>` (defaulting to `null` for callers that don't care). On each `absorb()` it computes triggered weights via `BatchPatternMatcher.triggeredWeights(patternProvider(), campaignEvents)` and passes them into `CampaignRiskScorer.compute(..., triggeredPatternWeights = ...)`. The resulting `Outcome` carries the triggered weights so `:app` can build an `Explanation` for the status screen.

`StatusViewModel` composes the enabled-pattern list at runtime: `SeedPatternLoader.load().map { p -> p.copy(enabled = repos.patternState.isEnabled(p.patternId.value, default = p.enabled)) }`. The lambda is what the orchestrator calls; user changes via Settings (Stage 8) reflect on the next demo run.

## What's deferred

- **Pattern signature verification** (§6.4 + §7.4) — `:core:crypto` (Ed25519 verify) lands in Stage 6 alongside the sync channel. In-APK seed patterns are trusted as part of the signed APK; downloaded patterns will require signature verification when sync ships.
- **Sync channel** (§7) — HTTPS download-only client, signed pattern bundles, rollback semantics. See [Update packages and signing](Update-packages-and-signing.md).
- **User-pattern wizard** (§17.3, §19.17) — post-MVP. The MVP user can enable / disable system patterns and reset to defaults; they cannot author new ones.
- **Pattern-signed indicator** in the UI — Stage 6 / Stage 8.
- **`ContactEvent`, `ManualEvent`, `PatternEvent` interpreters** — the parser already accepts these types as enum values but rejects patterns referencing them so they can't be silently ignored. Interpreters land when the corresponding signal source ships.
- **Lookalike-domain Levenshtein detector** — replaces the placeholder regex in `new_lookalike_domain_visit_v1` when Stage 5 (web channel) ships.

## Acceptance coverage

§23 criteria proven by Stage 2 tests in `:app/src/test/kotlin/com/qalqan/antifraud/acceptance/`:

- **#12** — `PatternMatchAcceptanceTest`: a system pattern matches an example sequence and produces the expected warning.
- **#14** — `DisabledPatternAcceptanceTest`: a disabled pattern does not contribute to scoring.
- **#17** — `ExplainabilityAcceptanceTest`: every warning shows ≥3 specific reasons (or all available when fewer exist).

§6.4 parse-time security check lives in `core/patterns/src/test/kotlin/.../PatternSecurityTest.kt`: the parser rejects unknown operators (e.g. `"operator": "exec"`); unknown top-level JSON fields are silently ignored but structurally unreachable on the typed `ScenarioPattern`.
