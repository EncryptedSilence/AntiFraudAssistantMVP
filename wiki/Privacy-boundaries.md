# Privacy boundaries

This page is the contributor-facing companion to spec §2 ("Privacy and constraints"). The list of *what the app must never do* is not aspirational — every item below is a hard rule, and most have a corresponding test that the next person to touch the code must keep passing.

## The eight hard rules (§2.1)

| # | Rule | Why it matters |
|---|---|---|
| 1 | **No AI of any kind.** No cloud AI, no on-device AI, no inference frameworks, no ML model files. Risk scoring is pure rule-based. | The product promise is auditable, deterministic decisions. AI introduces non-determinism and a "trust the model" surface that the user cannot verify. |
| 2 | **No background egress.** No telemetry SDKs, no analytics, no crash reporters that send data. Sync is download-only and user-toggled. | The user must be able to use the app fully offline, and trust that nothing leaves the device they did not explicitly approve. |
| 3 | **No audio.** Never request `RECORD_AUDIO`. Never analyze voice. Never recognize speech. | Voice content is qualitatively more sensitive than metadata. The app does not need it to do its job. |
| 4 | **No accessibility service.** Never request the accessibility role. Never read messenger conversations. | A11y permission lets an app see every screen. Asking for it would invalidate the entire trust model. The web-channel decision (see [Web channel](Web-channel.md)) explicitly rules out a11y for that reason. |
| 5 | **No full URLs.** Store eTLD+1 only. No paths, no query strings, no cookies, no logins, no passwords, no banking data. | Paths and query strings carry tracking IDs, session tokens, and content the user did not consent to share. The eTLD+1 alone is sufficient for lookalike detection. |
| 6 | **No advertising or tracking SDKs.** | Same family as rule 2. The app has no third-party SDK that loads remote code on launch. |
| 7 | **No legal verdict.** The app says "possible fraud", never "fraud confirmed". | The app is a friendly nudge, not an authority. False positives are inevitable; presenting them as verdicts would do more harm than good. |
| 8 | **Manual entry is fallback only.** The product value is automatic capture (calls, SMS) plus real-time alerts. Manual paste is a permission-denied / retroactive fallback, not a primary path. | Real users won't use a paste-it-yourself fraud detector. Automatic observation is what makes the product viable. |

These rules are non-negotiable. A code change that crosses one of these requires explicit re-litigation, not a workaround.

## How rules become tests

Per CLAUDE.md, *"Privacy boundary tests are not optional. Every feature that touches a privacy hard rule (§2.1) must come with a test that proves the hard rule is held."*

Concrete tests that exist or are planned:

| Rule | Test in repo | Status |
|---|---|---|
| #1 (No AI) | dependency check: no inference framework dep on the classpath | implicit via `:core:patterns` having zero Android deps + Stage 6 will block on classpath |
| #2 (No egress) | manifest scan: only `INTERNET` (Stage 6) is declared, no analytics SDK | landing with Stage 6 |
| #3 (No audio) | manifest scan: no `RECORD_AUDIO` permission ever | landing with Stage 3 manifest |
| #4 (No a11y) | manifest scan: no `BIND_ACCESSIBILITY_SERVICE`; manifest has no `<service>` of `AccessibilityService` | landing with Stage 3 manifest |
| #5 (No full URLs) | unit test: a synthetic `WebEvent` with a path-bearing URL stores only the eTLD+1 in `domainDisplayLocal` after normalization | lands with Stage 5 web channel |
| #5 (SMS body excerpt ≤ 200 chars) | unit test in `:core:database` | shipped Stage 1, kept passing through Stage 2 |
| #6 (No tracking SDKs) | dependency-tree check: no GoogleAnalytics / Firebase / Adjust / etc. | implicit; will become explicit pre-Stage-6 |
| #7 (No legal verdict) | UI string scan: warning copy uses "possible" / "may be" wording, never "is fraud" | landing with Stage 8 / Stage 9 UI strings |
| Export contains exactly what redaction preview showed | acceptance test: the bytes of the exported file equal the bytes the redaction preview rendered | landing with Stage 7 export |

## Where the boundary actually lives in code

- **`:core:domain`** — entity types use `PhoneHash`, `SenderHash`, `DomainHash` as inline value classes. Plaintext numbers and senders never appear in entities; the encrypted display name is a separate field that the §17 UI may render but the §8 export must redact.
- **`:core:database`** — SMS body excerpts are encrypted at rest via `CryptoBox`, and bounded to 200 chars at insertion time (Stage 1 Phase 5 / Phase 6). The retention purger deletes events older than 30 days; pattern overrides survive that purge but are wiped by `Repositories.wipeAll()` (Stage 2 T38).
- **`:core:patterns`** — the typed `ScenarioPattern` carries no field that could store executable code. The parser rejects unknown operators and unsupported event types so a downloaded pattern cannot smuggle behavior past the schema. See [Pattern engine](Pattern-engine.md) and [Update packages and signing](Update-packages-and-signing.md).
- **`:app`** — the demo importer reads only in-memory fixtures and the wipe button calls `Repositories.wipeAll()`. The status screen reads in-memory state only; nothing is exported or transmitted.

## What the boundary forbids by construction

The hard rules also dictate things the project structure makes physically impossible:

- The pure-JVM modules (`:core:domain`, `:core:scoring`, `:core:correlation`, `:core:patterns`) can't depend on Android APIs by definition. That includes any Android-only AI / a11y / network surface.
- `WRITE_EXTERNAL_STORAGE` and `MANAGE_EXTERNAL_STORAGE` are never declared. Exports go through `ACTION_CREATE_DOCUMENT` (Stage 7), which puts the user in control of the destination.
- Foreground services declare their type explicitly. Android 14+ enforces this; on older versions we still declare it for honesty about what the service is doing.

## When this page changes

Update this page when:

- a hard rule is restated more precisely (e.g. "200-char SMS excerpt" gets a different cap);
- a new test gates a hard rule;
- a Stage adds a new permission and the table above needs a new row;
- a hard rule is *almost* crossed and the workaround is interesting enough to document so future readers don't repeat the near-miss.

Do **not** update this page just because a non-§2.1 rule changed. This page is for the immovable rules.
