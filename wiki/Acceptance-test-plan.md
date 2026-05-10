# Acceptance test plan

Spec §23 lists 47 testable acceptance criteria. This page is the contributor-facing map: which criterion is covered today, where the test lives, and which stage adds the rest.

> Status as of Stage 2 close. Updated when each subsequent stage closes.

## Coverage today

### Stage 1 — local core (T82–T85)

| §23 # | Criterion | Test |
|---:|---|---|
| #1 | App functions without network connectivity | `app/src/test/.../acceptance/T82` (no-network in Stage 1) |
| #4 | All data stored only on-device | `app/src/test/.../acceptance/T82` (same gate as #1) |
| #19 | Database is encrypted at rest | `app/src/test/.../acceptance/T83` (encrypted DB) |
| #20 | "Wipe all data" reliably clears every storage surface | `app/src/test/.../acceptance/T84` (wipe-all) |
| #21 | False alarm closure clears the active campaign | `app/src/test/.../acceptance/T85` (false-alarm closure) |

### Stage 2 — pattern engine (T47–T51)

| §23 # | Criterion | Test |
|---:|---|---|
| #12 | A system pattern from §A appendix matches an example sequence and produces the expected warning | `app/src/test/.../acceptance/PatternMatchAcceptanceTest` |
| #14 | A disabled pattern does not contribute to scoring | `app/src/test/.../acceptance/DisabledPatternAcceptanceTest` |
| #17 | Every warning shows ≥ 3 specific reasons (or all available reasons if fewer than 3 exist) | `app/src/test/.../acceptance/ExplainabilityAcceptanceTest` |
| §6.4 | Pattern bundles cannot contain executable code (parse-time rejection) | `core/patterns/src/test/.../PatternSecurityTest` |
| (E2E) | End-to-end FAST_ATTACK demo triggers ≥ 1 Stage-2 pattern with explanation | `app/src/test/.../acceptance/PatternEngineEndToEndTest` |

Existing Stage-1 criteria the Stage 2 work strengthens (no new tests, but more coverage):

- **#6** — `RiskSession` from related events. Stage 2's matcher produces additional links the correlator considers.
- **#10** — Recompute < 500 ms. Pattern matching latency is part of this budget; it's measured in `PatternEngineEndToEndTest`.

## What's still open

The remaining 39 §23 criteria fall into one of the planned stages. Each will land with a corresponding test in `:app/src/test/.../acceptance/` (Robolectric + JUnit 4, the established convention).

| §23 # | Criterion | Lands with |
|---:|---|---|
| #2 | Calls observed without default-Phone role | Stage 3 |
| #3 | SMS observed without default-SMS role | Stage 4 |
| #5 | New domain detection (whatever channel choice ships) | Stage 5 |
| #7–#9 | RiskCampaign correlation across multi-day events, link strength, time decay | partly Stage 1 (correlation), partly Stage 9 (real-device timing) |
| #10 | Recompute latency budget on a real device | Stage 9 |
| #11 | Critical-risk → full-screen alert latency targets | Stage 9 |
| #13 | A user-pattern matches an example sequence | post-MVP (user-pattern wizard deferred per §24) |
| #15 | Pattern signature verification rejects forged bundles | Stage 6 |
| #16 | Sync rejects schema-version mismatch | Stage 6 |
| #18 | "Pause before action" modal at critical level | Stage 9 |
| #22 | Trusted / suspicious local lists work without network | Stage 8 |
| #23 | Question UX (3 yes/no, only at high+) wired into the pipeline | Stage 9 |
| #24 | Educational cards (5) accessible from the home screen | Stage 8 |
| #25 | Lookalike domain detector against ~20 KZ banks | Stage 5 |
| #26 | Real call / SMS observation on a real device | Stage 3, Stage 4, Stage 9 |
| #27 | Foreground-service ongoing notification is honest about what's running | Stage 3, Stage 9 |
| #28 | Permission denial path: app still works in degraded mode | Stage 3, Stage 4, Stage 9 |
| #29 | OEM matrix: alert surface fires on Samsung / Xiaomi / stock Pixel | Stage 9 |
| #30 | Battery-saver / Doze does not silently disable the observer service | Stage 9 |
| #31–#34 | Export pipeline: redaction preview ↔ exported bytes equality on TXT / Markdown / JSON / CSV | Stage 7 |
| #35 | Export uses `ACTION_CREATE_DOCUMENT`, never `WRITE_EXTERNAL_STORAGE` | Stage 7 |
| #36 | 30-day retention purge runs and is reversible only by `wipeAll` | Stage 1 (purger shipped); test for the boundary lands with Stage 8 settings |
| #37 | Sensitivity slider changes which level fires the alert | Stage 8 |
| #38 | Settings → permission state mirrors actual granted state | Stage 8 |
| #39 | First-run onboarding works without granting any runtime permission | Stage 8 / Stage 9 |
| #40 | Update-package format rejects bundles missing the manifest | Stage 6 |
| #41 | Update-package check happens only when user-toggled | Stage 6 |
| #42 | Update-package install is atomic (no partial catalog state) | Stage 6 |
| #43 | Local-channel import uses the same validator as the stable channel | Stage 6 |
| #44 | Storage stays on-device; export path is the only way out | Stage 7 |
| #45 | App rejects requests for permissions outside Appendix C | continuous — manifest scan |
| #46 | Build is reproducible from source | Stage 9 / release |
| #47 | All §2.1 rules are covered by automated tests | continuous — see [Privacy boundaries](Privacy-boundaries.md) |

> The numbering above is approximate — the spec's ordering is authoritative. When a stage lands a test, replace the row's "Stage N" with the file path and SHA.

## Test conventions

- **Acceptance tests live in `:app`** under `app/src/test/kotlin/com/qalqan/antifraud/acceptance/` so they exercise the full data path.
- **`@RunWith(RobolectricTestRunner::class) + @Config(sdk = [34]) + org.junit.Test`** — JUnit 4 via the JUnit Vintage engine, matching the convention plugin's setup.
- **`Repositories.inMemory(ctx, InMemoryCryptoBox())`** for setup. Never instantiate `KeyStoreCryptoBox` in tests.
- **`@After` calls `repos.close()`** so each test owns its own database lifecycle.
- **Each criterion gets its own test class** named after the §23 number or the criterion gist. One concern per test.
- **Privacy-boundary tests are mandatory.** A new feature touching §2.1 must land with the test that proves the rule is held. See [Privacy boundaries](Privacy-boundaries.md).

## When this page changes

Update on each stage close: replace the "Stage N" placeholder for any criteria the stage has just covered with the actual test file path. Note any criteria the stage discovered cannot be tested as written (rare; document the deviation here, not in the spec).
