# Documentation & Comment Refactor — Task Checklist

Execution checklist for [PLAN.md](PLAN.md). Phases are ordered so the repo is consistent after each one.
Findings referenced as A1-A10 (stale), B1-B10 (duplication), C1-C15 (comments) are defined in PLAN.md.

---

## Phase 0 — Baseline

- [x] Record starting counts: `wc -l docs/*.md */README.md */*/README.md *.md` — **13,490 lines / 22 files** in `docs/`, **2,753 lines / 17 files** in module READMEs
- [x] `./gradlew dokkaGeneratePublicationHtml` passes **before** any change — BUILD SUCCESSFUL
- [ ] `pip install mkdocs mkdocs-material mkdocs-github-admonitions-plugin Pygments && mkdocs build --strict` passes before any change — **does not pass**: 54 pre-existing warnings, almost all cross-directory links (e.g. `docs/architecture.md` → `../data/README.md`) that mkdocs' strict validator won't resolve outside `docs_dir`, plus a handful of dead anchors. Tracked as finding A11 in PLAN.md; fix belongs to Phase 1/2, not this baseline commit.
- [x] Note current alert count per file: `grep -rc "> \[!" docs/*.md` (baseline was guessed at ~45 across 13 files; **actual measured baseline is 88 across 18 files**)

## Phase 1 — Delete and re-nav

Do the deletions and the `mkdocs.yml` nav rewrite in **one commit** so the site never has dangling entries.

- [x] `git rm --cached docs/index.md` — already in `.gitignore:55` and overwritten by CI (A7)
- [x] Delete: `faq.md`, `tips.md`, `quick-reference.md`, `philosophy.md`, `data-flow.md`, `plugins.md`, `spotless.md`, `github.md`, `fastlane.md`, `dependency.md`, `performance.md`, `license.md`
- [x] Rewrite `mkdocs.yml` nav — 8 of the 10 target files exist today and are in nav (plus the still-live `dependency-injection.md`, absorbed into `data.md` in Phase 2); `data.md` and `build-and-tooling.md` get nav entries in Phase 2 when those files are created, per principle 5 ("no aspirational content" — no nav entry should point at a file that doesn't exist yet). Kept the API Reference entry; License now points at the GitHub blob URL (`https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/LICENSE`), same pattern already used in `README.md:113`.
- [x] Pin the plugin version in `.github/workflows/docs.yml` (currently installs unpinned; Beta-stage 0.1.1) — pinned to `==0.1.1`
- [ ] **Accept:** `mkdocs build --strict` passes — **does not fully pass yet**: 55 → 50 warnings after this phase's deletions. Remaining 50 are (a) dangling links *inside* surviving files (`guide.md`, `getting-started.md`, `troubleshooting.md`, `firebase.md`, `architecture.md`, `components.md`, `state-management.md`, `navigation.md`) that still reference now-deleted docs by name — fixed as each file is rewritten in Phase 2, not by deletion alone; (b) the cross-directory README link limitation from A11 (`../data/README.md` etc., unresolved by mkdocs' strict validator outside `docs_dir`); (c) a handful of dead in-page anchors, also A11. None of these are introduced by this phase's changes — full pass is a Phase 2/Phase 6 outcome.

> [!TIP]
> Salvage before deleting. Each deleted file has unique content that must land somewhere — work through
> Phase 2 with the deleted files still available in git history, not from memory.

## Phase 2 — Rewrite `docs/`

One item per target file. Each states its source material and its acceptance criterion.

- [x] **`navigation.md`** — from-scratch rewrite for Navigation 3 (A1). The single largest item; nothing in the current file is reusable. Cover `NavKey`, `Navigator`, `rememberNavigationState`, multi-back-stack `NavigationState`, `NavDisplay`, `ListDetailSceneStrategy`, and the assisted-inject pattern Nav3 forces for keyed args (`ItemViewModel`).
      **Accept:** zero occurrences of `NavController`/`NavHost`; every API named appears in `core/navigation/` or `app/src/main/kotlin/dev/atick/compose/ui/` — verified: 1,284 → 303 lines, 2 alerts, `mkdocs build --strict` warnings 50 → 46 (no regression; remaining warnings belong to unrewritten files)
- [x] **`architecture.md`** — absorb `philosophy.md` and `data-flow.md` diagrams; state the domain-layer rationale exactly once (B5); delete the false testing claim (A3)
      **Accept:** ≤ 400 lines; "domain layer" rationale appears once in `docs/` — verified: 601 → 261 lines; `grep -rni "domain layer" docs/` matches only `architecture.md` (3 lines, one section); false "testing planned" claim replaced with accurate `:core:testing`/Robolectric facts; `philosophy.md` had no diagrams or domain-layer content to salvage (corrects B5's file list — `data/README.md`'s "why no domain layer" and `data-flow.md`'s general diagrams were the real sources); pattern-specific `data-flow.md` diagrams (network-only/local-only/offline-first/caching) left in git history for the future `data.md` task; the 330-line "Integration Patterns" section (stale pre-Nav3 API in its Navigation+State example) cut to one trimmed end-to-end flow diagram; `mkdocs build --strict` warnings 46 → 41, no regressions
- [ ] **`state-management.md`** — `UiState`, `StatefulComposable`, the three update extensions, context parameters, `OneTimeEvent`; fix the "Kotlin 2.0" attribution (A10)
      **Accept:** ≤ 350 lines; every snippet greppable in `core/ui/`
- [ ] **`guide.md`** — the one canonical feature walkthrough, built from real repo code (B2)
      **Accept:** ≤ 300 lines; the walkthrough exists nowhere else in the repo
- [ ] **`components.md`** — reference table over prose; add Material Expressive theming (A5) and the `:lint` `DesignSystem` check that enforces the wrappers (A8)
      **Accept:** ≤ 400 lines, down from 1,556; every component name resolves in `core/ui/components/`
- [ ] **`data.md`** — new file: data sources, repository pattern, two-way sync, `suspendRunCatching` → `Result` (B7)
      **Accept:** ≤ 400 lines; **no mention of `networkBoundResource`** (A2)
- [ ] **`build-and-tooling.md`** — new file absorbing 6 deleted docs; must cover the previously undocumented subsystems (A8): `:lint`, APK badging, Dependency Guard, Gradle Managed Devices, `compose_compiler_config.conf`, `.run/` configs, Spotless-via-init-script
      **Accept:** ≤ 500 lines; every Gradle task named is runnable
- [ ] **`getting-started.md`** — clone → run in 5 minutes; correct JDK to 21 only (A10)
      **Accept:** ≤ 200 lines; a fresh clone succeeds following only this file
- [ ] **`firebase.md`** — console setup, config files, security rules, Credential Manager specifics
      **Accept:** ≤ 250 lines
- [ ] **`troubleshooting.md`** — rebuild as `<details>` entries, one per symptom, absorbing all six scattered troubleshooting sections (B3); drop the false testing claim (A3)
      **Accept:** **≤ 250 lines**, down from 2,071; no other `docs/` file has a troubleshooting section

## Phase 3 — Module READMEs

Four sections each — *Purpose* → *Key APIs* (table) → *Gotchas* (only if real) → links. **≤ 80 lines.**

> [!IMPORTANT]
> Keep the leading `# Module :name` line verbatim — Dokka consumes it as module documentation.

Rewrites:

- [ ] `data/README.md` (484 → ≤ 80) — remove `networkBoundResource` (A2), drop duplicated repository patterns (B7)
- [ ] `sync/README.md` (331 → ≤ 80) — drop the 229-line troubleshooting section (B3)
- [ ] `core/preferences/README.md` (291 → ≤ 80)
- [ ] `core/room/README.md` (285 → ≤ 80)
- [ ] `core/ui/README.md` (238 → ≤ 80) — drop the duplicated Philosophy section (B5)
- [ ] `core/network/README.md` (205 → ≤ 80) — remove `networkBoundResource` (A2)
- [ ] `core/android/README.md` (152 → ≤ 80) — remove `networkBoundResource` incl. the full fake snippet at :67 (A2)
- [ ] `app/README.md` (150 → ≤ 80)
- [ ] `firebase/firestore/README.md`, `firebase/auth/README.md`, `firebase/analytics/README.md`
- [ ] `feature/settings/README.md`, `feature/auth/README.md`, `feature/home/README.md`, `feature/profile/README.md`

New (A9):

- [ ] `core/navigation/README.md` — highest value; the Nav3 model
- [ ] `core/testing/README.md` — `MainDispatcherRule`, the fakes, and how `AndroidTest.kt` wires test deps into every module
- [ ] `lint/README.md` — the two checks and how `lintPublish` ships them to consumers
- [ ] `benchmarks/README.md` — baseline profile generation and startup benchmark

**Accept:** `./gradlew dokkaGeneratePublicationHtml` passes; `grep -rn "networkBoundResource" .` returns nothing.

## Phase 4 — Root files

- [ ] `README.md` — correct the version block to Kotlin 2.4.10 / AGP 9.3.1 / Gradle 9.6.1 / Java 21 (A6); trim the repeated divider images (B10)
- [ ] `AGENTS.md` (441 lines) — slim to agent-specific rules plus pointers into `docs/`; remove the duplicated feature tutorial (B2); fix the stale navigation guidance (A1)
- [ ] `CHANGELOG.md` — correct the AGP version (A6) and backfill entries for Navigation 3, custom lint, baseline profiles, test harness, Dependency Guard, APK badging
- [ ] `CONTRIBUTING.md` (18 lines) — add the five-entry vocabulary table, the code-marker rules, the prose-why style note, and the "never use `!!!` syntax" rule. Mostly new content.

**Accept:** every version string in the repo matches `gradle/libs.versions.toml` and `gradle-wrapper.properties`.

## Phase 5 — Code comments

Prose-why style. Most items carry **no** marker — the marker column is the exception, not the default.

| # | File | Must explain | Marker |
|---|---|---|---|
| C1 | `firebase/auth/.../AuthDataSourceImpl.kt:50` | What `CredentialManagerMisuse` flags, why it is suppressed class-wide, when to remove it. Model it on `app/lint.xml:20-29`. | ⚠️ |
| C2 | `core/ui/.../StatefulComposable.kt:260-292, 362-382` | Context parameters as the `viewModelScope` mechanism; `if (value.loading) return` as a re-entrancy lock that drops concurrent actions; the `data` snapshot taken before `launch`; the synthesized exception at :278 | ❗ |
| C3 | `core/ui/.../StatefulComposable.kt:101-105` | `getContentIfNotHandled()` mutates during composition; the `LaunchedEffect` key is the callback, not the error | ⚠️ |
| C4 | `feature/home/.../HomeViewModel.kt:56-59` | `onStart` + `WhileSubscribed(5000)` re-subscription starts a new permanent collector; note it is the template's most-copied idiom | ⚠️ |
| C5 | `sync/.../Sync.kt:124` | Add the missing **safety argument**: dropped requests are harmless because the worker drains all pending local changes. Simultaneously **cut** the three verbose Style B blocks at `Sync.kt:55-58`, `Sync.kt:103`, `SyncManagerImpl.kt:140` down to one. | 💡 |
| C6 | `data/.../HomeRepositoryImpl.kt:60-65` | `requestSync()` at :63 is outside the `flow {}` at :65, so it fires at construction, not collection | 💡 |
| C7 | `gradle/init.gradle.kts` | Why Spotless is an init script and not a plugin; that this is why `./gradlew tasks` omits `spotlessApply`; what the header regexes do | 💡 |
| C8 | `app/build.gradle.kts:78-85` | Release silently falls back to the debug key; output is un-shippable and un-upgradable; the fallback exists so the template builds on a fresh clone | ☠️ |
| C9 | `app/proguard-rules.pro:3-4` | The `{ *; }` breadth is deliberate-and-blunt, wider than the linked bug requires; note the overlap with the two consumer-rules files | ⚠️ |
| C10 | `firebase/auth/consumer-rules.pro:1-4` | The `playservices` provider loads via `ServiceLoader`; R8 full mode strips it; symptom is a provider-not-found at runtime. Match the per-line style of `core/network/consumer-rules.pro`. | — |
| C11 | `firebase/auth/.../AuthDataSourceImpl.kt:175,190` | Why `setFilterByAuthorizedAccounts` differs between sign-in and register, and that `true` is what makes a separate register path necessary | 💡 |
| C12 | `feature/settings/.../SettingsViewModel.kt:90-93` | Every non-Arabic locale silently becomes English; coupled to `generateLocaleConfig` at `app/build.gradle.kts:100`; adding `values-fr` will not update this `when` | ⚠️ |
| C13 | `core/ui/theme/{Background,Gradient,Tint}.kt:18` | Replace the three content-free TODOs: name the `io.nlopez.compose.rules` ruleset and the one-line `.editorconfig` fix. Also give `StatefulComposable.kt:80` a rationale (it is a state wrapper, not a layout node). | — |
| C14 | `core/preferences/.../UserDataSerializer.kt:83-108` | State that it is an unreferenced example for adopters, or delete it; note the `valueOf` exception type escapes the `readFrom` catch | ℹ️ |
| C15 | `build-logic/.../AndroidCompose.kt:67` | The `flatMap` + `provider {}` idiom returns an *absent* provider, which is the whole point | 💡 |

Second tier, if time allows: `settings.gradle.kts:20-29`, `core/navigation/NavigationState.kt:78,89`,
`core/network/.../NetworkUtilsImpl.kt:43-93`, `core/ui/.../ActivityExtensions.kt:337-349`,
`sync/.../SyncWorker.kt:150-165, 202`.

**Accept:** marker count within budget; `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` passes.

## Phase 6 — Verify

- [ ] `mkdocs build --strict` — catches dangling nav entries and broken internal links
- [ ] `./gradlew dokkaGeneratePublicationHtml` — a malformed `# Module :x` header breaks the API site
- [ ] `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` — license headers survived
- [ ] `./gradlew build` and `./gradlew test` — no comment edit disturbed code
- [ ] `grep -rn "NavController\|NavHost\|networkBoundResource" docs/ *.md */README.md */*/README.md` → nothing
- [ ] `grep -rn "^!!! \|^??? " . --include='*.md'` → nothing (native Material syntax breaks GitHub rendering)
- [ ] `grep -rhno "^> \[![A-Za-z]*\]" --include='*.md' . | sed 's/.*\[!//;s/\]//' | sort -u` → only `NOTE`/`TIP`/`IMPORTANT`/`WARNING`/`CAUTION` in ALL-CAPS
      *(an unrecognized type silently degrades to a plain blockquote; `--strict` does **not** catch it)*
- [ ] `grep -c "^> \[!" docs/*.md` → no page above ~2, per GitHub's guidance

> [!NOTE]
> Both greps anchor on `^> [!` deliberately. `PLAN.md` and `CONTRIBUTING.md` document the vocabulary
> and therefore contain `` `> [!WARNING]` `` inside inline code and table cells. The plugin correctly
> does not convert those, and an unanchored grep would flag them as violations.
- [ ] `grep -rc "ℹ️\|💡\|❗\|⚠️\|☠️" --include='*.kt' . | grep -v ":0"` → 25-35 total
- [ ] Spot-check ≥10-line snippets in the new docs are greppable in the repo
- [ ] `mkdocs serve` **and** GitHub rendering both checked — dual rendering is the whole point of the plugin. Prioritize `troubleshooting.md` `<details>` and any `IMPORTANT`/`CAUTION` page, whose icon mapping is plugin-specific.
- [ ] Final counts: `docs/` ≤ ~3,500 lines, every module README ≤ 80

---

## Follow-up fixes — out of scope here

Real defects found during the audit. Documented honestly in Phase 5 comments, fixed separately so this
refactor stays free of behavioral changes.

- [ ] `core/ui/.../SwipeToDismiss.kt:78-81` — `onDelete()` is called in the composition body, re-firing on every recomposition while state sits at `EndToStart`. Should be a `LaunchedEffect(dismissState.currentValue)`.
- [ ] `core/network/.../NetworkUtilsImpl.kt:43-93` — `callbackFlow` never emits an initial value, so `isOffline` reports `false` at launch and the offline banner never shows. `ActivityExtensions.kt:337-349` seeds correctly; the asymmetry is unexplained.
- [ ] `sync/.../SyncWorker.kt:181-183` — KDoc claims 10s/20s/40s backoff, but no `setBackoffCriteria` is set and WorkManager's default `MIN_BACKOFF_MILLIS` is 30s. The doc is actively wrong.
- [ ] `data/.../HomeRepositoryImpl.kt:96,141,151` — `System.currentTimeMillis()` used as a distributed sync cursor against `whereGreaterThan("lastUpdated", ...)`. Clock skew silently drops remote records. Correct pattern is `FieldValue.serverTimestamp()`.
- [ ] `feature/home/.../ItemViewModel.kt:94-97` — `toDoubleOrNull() ?: return` freezes the text field while the user types `1.` or clears it. Keep the raw string in state.
- [ ] `core/preferences/.../UserDataSerializer.kt:83-108` — `DarkThemeConfigSerializer` is dead code whose `valueOf` throws the wrong exception type to be caught by `readFrom`.
- [ ] `app/build.gradle.kts:119-126` — `onVariants` block is a no-op that reads the first output's `versionName` and writes it back. Either delete it or mark it as the hook point for Issue #579.
