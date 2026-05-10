# Update packages and signing — **stub, lands with Stage 6**

> Stage status: **planned.** Spec §6.4 + §7 are authoritative. This page documents the pinned decisions and the items that are open.

## Why this is a separate subsystem

The pattern catalog has to be updatable without shipping a new APK every time a new fraud variant appears. At the same time, anything that the matcher reads at runtime is effectively privileged — a malicious pattern bundle could fabricate or suppress warnings. The two requirements together force a small but uncompromising design: download-only, signed, schema-validated, and locally curatable.

## Pinned decisions (from §6.4, §7, §24)

- **Download-only.** No background egress of user data. The sync client pulls from a single signed source over HTTPS. Sync is user-toggled.
- **Single embedded public key.** One Ed25519 public key compiled into the APK. Updates not signed by that key are rejected.
- **Schema validated.** The downloaded bundle parses through `PatternCatalogParser` (Stage 2). Any schema violation is rejected at parse time before the matcher sees it.
- **Checksum verified.** SHA-256 of the bundle, separate from the signature.
- **Rollback semantics.** Rejected updates do not partially apply. The app keeps its previous catalog state.
- **Two channels.** `stable` (signed remote source) and `local` (user imports a file from the share sheet, validated identically).
- **Patterns cannot contain executable code.** §6.4. The Stage 2 typed model already makes this structurally impossible — `ScenarioPattern` carries no field that the matcher can interpret as code. Signing is a defense in depth, not the primary safety.

## Stage 2 already locked in

- `:core:patterns` parses bundles via Moshi reflective adapters with `init {}`-block invariants — runtime JSON Schema is not a dependency.
- The parser rejects unknown operators, unsupported event types, malformed JSON, and missing required fields. See [Pattern engine](Pattern-engine.md).
- In-APK seed patterns are trusted as part of the signed APK; signature verification is a no-op on them.

## Open questions (Stage 6 work)

- **`:core:crypto` shape.** Where the Ed25519 verify lands — pure-JVM module reusing `java.security` providers? BouncyCastle? Conscrypt for performance? The decision feeds back into APK size and dependency footprint.
- **Bundle format.** Is a bundle a single JSON array (`[{pattern1}, {pattern2}, ...]`) or a manifest file plus per-pattern files? Single array is simpler; manifest+files lets the signer revoke individual patterns by version.
- **Signature placement.** Detached signature alongside the bundle, or in-band as a leading manifest? Detached is easier to handle in HTTPS responses; in-band is easier to share via the file-import path.
- **Update cadence and trigger.** Periodic `WorkManager` job? On-demand only via Settings? §7 is permissive; user expectation is the deciding factor.
- **Failure modes.** Network failure, signature failure, schema failure, partial download — each needs defined behavior and a test. Failure must never silently disable a pattern that was previously enabled.
- **Public key rotation.** What's the path when the signing key is compromised? Probably: ship a new APK with a new embedded key. No in-band rotation in the MVP.
- **`signatureStatus` field on `ScenarioPattern`.** Stage 2 parses without this field; it's listed in spec §6.2 as deferred. When it lands, the matcher needs to surface it to the UI ("signed pattern" indicator — Stage 8).
- **Catalog version pinning.** When the user disables a pattern, do we persist the disable across signed updates that change the pattern's `version`? Probably yes (the override key is `patternId`, not `patternId+version`), but the question deserves an explicit decision.

## Permissions and surfaces

- **`INTERNET`** — only this. No `ACCESS_NETWORK_STATE`, no `READ_PHONE_STATE` for sync purposes.
- **No background egress.** The sync client is a foreground or user-triggered job; it does not run on app start.
- **UX.** A Settings → Updates section. Manual "check now" button. Last-checked timestamp. Last-installed bundle version. Clear "off by default" if the policy decision goes that way.

## Out of scope for this page

- The lookalike-domain Levenshtein detector (Stage 5). It uses a separate static reference list, not the pattern catalog.
- The export pipeline (Stage 7). Export and update are independent surfaces.
- Real-time sync of trusted/suspicious phone numbers — this is the user's local list and never round-trips remotely.

## When this page becomes a design doc

When Stage 6 ships, this page replaces open questions with:

- bundle format (with example payload);
- the embedded public key fingerprint (rotated by APK release only);
- the failure-mode matrix and the test that proves each branch;
- the rate limit on `WorkManager` retries;
- the privacy-boundary test that confirms the sync client sends nothing in the request body.
