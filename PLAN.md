# Documentation & Comment Refactor — Plan

Companion checklist: [TASKS.md](TASKS.md).

## Status — complete

All seven phases are done, and so are the seven defects the audit turned up along the way. Kept as
the record of the analysis behind the change, including where the analysis was wrong.

| Measure | Before | After | Target |
|---|---|---|---|
| `docs/` | 13,490 lines / 22 files | **2,319 lines / 10 files** | ~3,500 / 10 |
| Module READMEs | 2,753 lines / 17 files | **642 lines / 19 files** | ≤ 80 each (max is 48) |
| GitHub alerts in `docs/` | 88 across 18 files | **≤ 2 per page** | ≤ 2 per page |
| Emoji markers in `.kt`/`.kts` | 0 | **17** | 25-35 ceiling |
| `mkdocs build --strict` | 54 warnings (A11) | **0 warnings** | 0 |

`./gradlew build`, `test`, `spotlessCheck` and `dokkaGeneratePublicationHtml` all pass.

Where this plan was wrong, and the checklist entry that corrects it:

- **A11 did not exist when the audit was written.** `mkdocs build --strict` had never been run
  locally; Phase 0 ran it and found 54 warnings. Added to the findings table afterwards.
- **B5's file list was wrong.** `philosophy.md` carried no domain-layer content — the real duplicate
  sources were `data/README.md` and `data-flow.md`.
- **A8 undercounted the `.run/` configs** — 13, not 12.
- **Phase 3 found a drift the audit missed entirely.** All four `feature/*/README.md` documented a
  `{Feature}Route` composable wrapper that the Navigation 3 migration had already deleted.
- **Phase 6 found a defect no phase anticipated.** Markdown nested inside `troubleshooting.md`'s 29
  `<details>` blocks rendered as literal text on the docs site — Python-Markdown needs `md_in_html`
  plus `markdown="1"`, and the bug is invisible on GitHub, whose renderer processes it regardless.
  Dual rendering is the reason that check exists, and it earned its place.
- **Two test drafts for the follow-up fixes passed against the buggy code** and were worthless until
  rewritten. Every fix was verified by reverting it and confirming the new tests fail.

Deliberately not done: the second-tier comment sites listed at the end of Phase 5, under that
section's own "if time allows" scoping.

## Why

**The docs describe a codebase that no longer exists.** A recent burst of work migrated this template to
Jetpack Navigation 3 and added a custom lint module, baseline profiles, a real test harness, Dependency
Guard and APK badging. None of it reached the docs. `docs/navigation.md` — 1,284 lines — still teaches
`NavController`, `NavHost` and `navigation-compose`, a dependency the project no longer declares.
`NavKey` and `rememberNavigationState` appear **zero** times in any markdown file.

**The docs are too verbose to use.** 13,490 lines across 22 files in `docs/`, plus 2,640 lines of module
READMEs. The "add a feature" tutorial is written out three times, the "why no domain layer" rationale
five times, LazyList performance advice four times. An adopter cannot find the important bits, and the
volume is why the docs drifted — nobody can update 16,000 lines in step with a migration.

**The hard code is the least commented.** The build logic and the `:lint` module are the best-commented
code in the repo. The code every adopter will copy — `StatefulComposable`, the sync worker, the
Credential Manager auth flow, the feature ViewModels — carries the least rationale. Where comments do
exist, they often state mechanism at length while omitting the load-bearing fact (see C5 below).

## Principles

1. **One canonical home per topic.** Every other mention is a link, not a restatement.
2. **Show real code.** Every snippet must be greppable in the repo. No invented illustrative examples —
   they are what drifted fastest.
3. **Prefer a table or a 10-line snippet** over three paragraphs of prose.
4. **Document what this template does**, not Android in general. Generic Compose/Kotlin tutoring belongs
   on developer.android.com and is the bulk of what gets deleted.
5. **No aspirational content.** If it is not implemented, it is not documented.
6. **Traps and rationale are typed GitHub alerts**, not bolded paragraphs, so the same idea looks the
   same everywhere.
7. **Comments explain why, not what.** The signature already says what. Match the prose-why style in
   `build-logic/` and `core/navigation/`.

---

## Findings

### A. Stale content

| # | Finding | Evidence |
|---|---|---|
| A1 | **Navigation 3 migration undocumented.** Code uses `NavKey`, `Navigator`, `rememberNavigationState`, `NavDisplay`, `ListDetailSceneStrategy`, multi-back-stack `NavigationState`. Docs teach the removed API. | `NavController`/`NavHost` 93× in `docs/navigation.md` + 5 other docs; `NavHostController` 0 matches in `.kt`; `NavKey` 0 doc mentions vs 11 `.kt` files |
| A2 | **`networkBoundResource` is documented but does not exist.** Presented as a real helper with a full usage snippet. | 0 matches in any `.kt`; cited in `core/android/README.md:24,67`, `core/network/README.md:196`, `data/README.md:124,133,283,285` |
| A3 | **"Testing infrastructure is planned but not yet implemented"** — false. ~110 test methods across 16 files, a `:core:testing` module, and a Robolectric + Compose harness wired into every module by `AndroidTest.kt`. | `docs/architecture.md:225`, `docs/troubleshooting.md:2012` |
| A4 | **"Baseline profile support is coming soon"** — false. `:benchmarks` exists with `BaselineProfileGenerator` and `StartupBenchmark`. | `docs/performance.md:433-438` |
| A5 | **Material Expressive theming undocumented.** `JetpackTheme` is built on `MaterialExpressiveTheme`. | `core/ui/theme/Theme.kt:148`; 0 doc mentions of "Expressive" |
| A6 | **Versions contradict across four files.** | `README.md`: Kotlin 2.4 / Gradle 9.6 · `docs/index.md`: **Kotlin 2.0 / Gradle 8.11.1** · `AGENTS.md`: Kotlin 2.4.10 / AGP 9.3.1 · `CHANGELOG.md`: AGP 9.1.0 · **actual**: Kotlin 2.4.10, AGP 9.3.1, Gradle 9.6.1, Java 21, minSdk 24 / targetSdk 36 / compileSdk 37 |
| A7 | **`docs/index.md` is dead weight.** Listed in `.gitignore:55` *and* overwritten by CI (`cp README.md docs/index.md` in `docs.yml`). Its unique 279 lines have never been deployed, yet are the worst source of stale versions and broken URLs. | `.gitignore:55`, `.github/workflows/docs.yml` |
| A8 | **Whole subsystems undocumented**: the `:lint` module (2 checks, `lintPublish`ed via `core/ui`), APK badging golden file, Dependency Guard baseline, Gradle Managed Devices, `compose_compiler_config.conf`, the 12 `.run/` configs. | no `mkdocs.yml` nav entry for any |
| A9 | **4 modules have no README** — `core/navigation`, `core/testing`, `lint`, `benchmarks` — while `docs/index.md:369-380` claims "Module Coverage: 100%". Its file and line counts are all wrong too. | verified by `wc -l` |
| A10 | Misc: `docs/faq.md:960` cites `v1.2.3`; `docs/troubleshooting.md:14-21` offers JDK 11/17 when the project requires 21; `docs/state-management.md:322,385` attributes context parameters to "Kotlin 2.0"; copyright year 2024 in `spotless.md:67`, 2025 elsewhere. | |
| A11 | **`mkdocs build --strict` currently fails**, 54 warnings. Nearly all are relative links from `docs/*.md` out to module READMEs (`../data/README.md`, `../core/ui/README.md`, etc.) — mkdocs' strict link validator only resolves paths inside `docs_dir` and flags anything outside it as not found, even though the target exists in the repo. The rest are a handful of dead in-page anchors (`performance.md#image-loading-optimization`, `navigation.md#nested-navigation`, etc.). Confirmed by running the baseline check in Phase 0; not previously known because the site had never been built with `--strict` locally. | measured 2026-08-08: `mkdocs build --strict` → "Aborted with 54 warnings in strict mode!" |

### B. Duplication, by lines recoverable

| # | Duplication | Cost |
|---|---|---|
| B1 | `docs/index.md:1-112` is a drifted fork of `README.md` | 391 lines, all deletable (A7) |
| B2 | The 6-step feature tutorial in `docs/guide.md`, `docs/quick-reference.md:379-510`, and `AGENTS.md` | 2 of 3 copies |
| B3 | `docs/troubleshooting.md` (2,071) vs. per-file troubleshooting in `navigation.md:1139+`, `dependency-injection.md:712+`, `sync/README.md:102+` (229 lines), `firebase.md:72+`, `fastlane.md:129+`, `getting-started.md:111+` | ~1,800 |
| B4 | `docs/faq.md` (1,386) restates `architecture.md` + `state-management.md` + `performance.md` + `firebase.md` in Q&A form | ~1,300 |
| B5 | "Why no domain layer" in `philosophy.md`, `architecture.md`, `core/ui/README.md`, `data/README.md`, `faq.md` | 5 copies → 1 |
| B6 | LazyList keys / image loading / recomposition in `performance.md`, `troubleshooting.md`, `components.md`, `tips.md` | 4 copies → 1 |
| B7 | Identical repository-pattern diagrams in `docs/data-flow.md` and `data/README.md` | ~490 |
| B8 | "Further Reading" / "Related Documentation" blocks in 17 of 22 docs + 15 module READMEs | ~250-350 |
| B9 | 14 module READMEs repeating a 9-section template | the template is the bloat |
| B10 | `docs/license.md` duplicates `/LICENSE`; `docs/fastlane.md` duplicates `fastlane/README.md`; 6 repeated divider images in `README.md` | ~250 |

### C. Code needing "why" comments

Verified sites, highest value first.

| # | Site | What is non-obvious |
|---|---|---|
| C1 | `firebase/auth/.../AuthDataSourceImpl.kt:50` | Class-wide `@SuppressLint("CredentialManagerMisuse")` with **zero** explanation. `app/lint.xml:20-29` is the model answer for how this repo documents a suppression. |
| C2 | `core/ui/.../StatefulComposable.kt:260-292, 362-382` | Four decisions in 30 lines: Kotlin **context parameters** (`context(viewModel: ViewModel)`, line 260/362); `if (value.loading) return` (263/365) as a re-entrancy lock that silently drops concurrent actions; `data` snapshotted before `launch`; a synthesized `IllegalStateException` on `Result.success(null)` (278). Every screen routes through this. |
| C3 | `core/ui/.../StatefulComposable.kt:101-105` | `getContentIfNotHandled()` mutates an `AtomicBoolean` **during composition**, so a discarded composition loses the error; and the `LaunchedEffect` key is `onShowSnackbar`, not the error. |
| C4 | `feature/home/.../HomeViewModel.kt:56-59` | `onStart { getJetpacks() }` + `stateInDelayed` (`WhileSubscribed(5000)`) re-subscribes after a >5s collector gap, starting a **new permanent collector** each time. Repeated in `ItemViewModel.kt:86-88` and `SettingsViewModel.kt:53-57`. The most-copied idiom in the template. |
| C5 | `sync/.../Sync.kt:124` + `SyncManagerImpl.kt:140` | `ExistingWorkPolicy.KEEP` is already documented **three times** (`Sync.kt:55-58`, `Sync.kt:103`, `SyncManagerImpl.kt:140`) — but only the mechanism ("requests are ignored"). The missing fact is the *safety argument*: `createOrUpdateJetpack` → `requestSync()` (`HomeRepositoryImpl.kt:101,114`) may be a silent no-op, and that is safe only because the worker drains **all** pending local changes. Exemplifies the Style B problem: volume without the load-bearing fact. |
| C6 | `data/.../HomeRepositoryImpl.kt:60-65` | `syncManager.requestSync()` (line 63) sits **outside** the `flow {}` builder (line 65), so it fires at flow-construction time, not collection time. |
| C7 | `gradle/init.gradle.kts` | Spotless is applied via an **init script**, not a plugin — the most structurally confusing decision in the build, and the why is stated nowhere. Also explains why `./gradlew tasks` does not list `spotlessApply`. |
| C8 | `app/build.gradle.kts:78-85` | Release builds silently fall back to the **debug signing key** when `keystore.properties` is absent, warning only via `println`. Every adopter hits this on day one; the output is un-shippable and un-upgradable. |
| C9 | `app/proguard-rules.pro:3-4` | `-keep @kotlinx.serialization.Serializable class * { *; }` keeps every member of every serializable class — far broader than the linked issuetracker bug requires. |
| C10 | `firebase/auth/consumer-rules.pro:1-4` | The repo's only conditional (`-if`) R8 rule, zero comment, while sibling `core/network/consumer-rules.pro` documents every line. The `playservices` provider loads reflectively; R8 full mode strips it. |
| C11 | `firebase/auth/.../AuthDataSourceImpl.kt:175,190` | `setFilterByAuthorizedAccounts(true)` for sign-in vs `false` for register, 15 lines apart, encodes the entire Credential Manager UX contract in one flag. |
| C12 | `feature/settings/.../SettingsViewModel.kt:90-93` | A hardcoded `when` maps every non-Arabic locale to English, invisibly coupled to `generateLocaleConfig = true` (`app/build.gradle.kts:100`). Adding `values-fr` changes what the system offers but not this `when`. |
| C13 | `core/ui/theme/{Background,Gradient,Tint}.kt:18` | Three identical `@Suppress("ktlint:compose:compositionlocal-allowlist")` with a content-free TODO. Rule comes from `io.nlopez.compose.rules:ktlint` (`gradle/init.gradle.kts`); fix is one `.editorconfig` key. Also `StatefulComposable.kt:80` has a bare `@Suppress` with no rationale. |
| C14 | `core/preferences/.../UserDataSerializer.kt:83-108` | `DarkThemeConfigSerializer` is defined and **never referenced**. Its `valueOf()` would also throw `IllegalArgumentException`, not `SerializationException`, escaping the `readFrom` catch. |
| C15 | `build-logic/.../AndroidCompose.kt:67` | `Provider<String>.onlyIfTrue()` — the "return an absent provider" `flatMap` + `provider {}` idiom is genuinely cryptic. |

Second tier, same treatment if time allows: `settings.gradle.kts:20-29` (repo content filtering),
`core/navigation/NavigationState.kt:78,89` (`derivedStateOf` in a plain constructor),
`core/network/.../NetworkUtilsImpl.kt:43-93` (no initial emission),
`core/ui/.../ActivityExtensions.kt:337-349` (operator ordering),
`sync/.../SyncWorker.kt:150-165` (foreground + retry cap), `SyncWorker.kt:202` (`get()`-only `val`).

---

## Target structure

`docs/` goes from 22 files / 13,490 lines to **10 files / ~3,500 lines**.

| File | Charter | Absorbs |
|---|---|---|
| `getting-started.md` | Clone → run in 5 minutes. Prereqs, optional Firebase, first build. | current file + Firebase setup from `faq.md` |
| `architecture.md` | Two-layer design, module graph, rationale — stated **once**. | `architecture.md`, `philosophy.md`, `data-flow.md` diagrams, 5 duplicate domain-layer sections |
| `guide.md` | The one canonical "add a feature" walkthrough, using real repo code. | `guide.md`, `quick-reference.md:379-510`, the `AGENTS.md` example |
| `state-management.md` | `UiState`, `StatefulComposable`, the three update extensions, context parameters, `OneTimeEvent`. | `state-management.md` + state sections of `faq.md`/`troubleshooting.md` |
| `navigation.md` | **Full rewrite for Navigation 3**: `NavKey`, `Navigator`, `rememberNavigationState`, multi-back-stack, `ListDetailSceneStrategy`, assisted-inject ViewModels for keyed args. | nothing — current file describes a removed API |
| `components.md` | Design system as a reference table, theming incl. Material Expressive, and the `:lint` `DesignSystem` check that enforces it. | `components.md` (heavily cut), theming from `tips.md` |
| `data.md` | Room / DataStore / Retrofit / Firestore sources, repository pattern, two-way sync, `suspendRunCatching` → `Result`. | `data-flow.md`, `data/README.md` overlap, `dependency-injection.md` essentials |
| `build-and-tooling.md` | Convention plugins, version catalog, Spotless-via-init-script, custom lint, Dependency Guard, APK badging, baseline profiles, `.run` configs, CI. | `plugins.md`, `dependency.md`, `spotless.md`, `github.md`, `fastlane.md`, build parts of `performance.md` |
| `firebase.md` | Console setup, config files, security rules, Credential Manager specifics. | `firebase.md` + Firebase parts of `faq.md`/`troubleshooting.md` |
| `troubleshooting.md` | Collapsible `<details>` entries, one per symptom, cause → fix. **≤ 250 lines.** | all six scattered troubleshooting sections, deduplicated |

**Deleted:** `index.md`, `faq.md`, `tips.md`, `quick-reference.md`, `philosophy.md`, `data-flow.md`,
`plugins.md`, `spotless.md`, `github.md`, `fastlane.md`, `dependency.md`, `performance.md`,
`license.md`. `mkdocs.yml` keeps the API Reference entry and points License at `/LICENSE`.

**Module READMEs** collapse to four sections — *Purpose* (2-3 sentences) → *Key APIs* (table) →
*Gotchas* (only if real) → links. **≤ 80 lines each.** `data/README.md` (484) and `sync/README.md` (331)
shed the most. New READMEs for `core/navigation`, `core/testing`, `lint`, `benchmarks`.

> [!IMPORTANT]
> Module READMEs are Dokka input — `DokkaConventionPlugin` feeds them into the `main` source set, so
> the leading `# Module :core:ui` line is load-bearing syntax, not a heading. Do not restructure it away.

---

## Conventions

### Comment style

Match the **prose-why** style already used in `build-logic/`, `:lint` and `core/navigation`: plain
paragraphs stating rationale and consequence, no markdown headings, no bullet lists, bare URLs inline.
The exemplar, `core/navigation/NavigationState.kt:48-50`:

```kotlin
// key() is what gives each sub stack its own saved state slot. Without it every iteration
// shares the composition's compound key hash, so all the stacks register under one key and
// are restored by position rather than by which destination they belong to.
```

Do **not** extend the older heading-and-bullet KDoc style seen in `core/room/di/DatabaseModule.kt:42-57`.
It is verbose, it duplicates what Dokka already renders, and C5 shows how it produces volume while
omitting the fact that matters.

Hard constraints:

- Every file carries an Apache 2.0 license header enforced by Spotless from `spotless/copyright.*`.
  Never disturb it; markers go in body comments only.
- Links are **bare URLs inline**, no markdown syntax: `https://issuetracker.google.com/issues/NNN`.
- Issue tracking uses `// Tracking: GitHub Issue #578` (`DokkaConventionPlugin.kt:41`).

### The five-entry vocabulary

One vocabulary, two expressions — GitHub alerts in markdown, emoji markers in code. Someone who learns
"⚠️ means trap" from a `.kt` file reads the same meaning off a `> [!WARNING]` box. The 1:1 parity is the
point: no docs-only types, nothing to drift.

| Alert | Emoji | Severity | Use when |
|---|---|---|---|
| `> [!NOTE]` | ℹ️ | Context | Background the reader needs, implying no action |
| `> [!TIP]` | 💡 | Rationale | The code looks wrong or arbitrary until you know why |
| `> [!IMPORTANT]` | ❗ | Contract | A contract the caller must uphold, easy to violate silently |
| `> [!WARNING]` | ⚠️ | Trap | Changing this without knowing will introduce a bug |
| `> [!CAUTION]` | ☠️ | Severe | Leaks credentials, corrupts data, or ships an unsigned build |

**No 🐛 marker.** GitHub has no `bug` type, and a code-only sixth marker would break the parity that
makes the vocabulary learnable. Upstream-bug workarounds use ❗ or ⚠️ plus the existing `Tracking:`
convention and a bare URL, which already signals "upstream defect".

**Code rules.** One marker per comment block, first line only. Never inside a KDoc `@param`/`@return`/
`@throws`, never in the license header. Budget **25-35 markers repo-wide** — unmarked prose-why comments
remain the default and the overwhelming majority. If a file wants three markers, the code needs
restructuring instead.

**Docs rules.** ALL-CAPS qualifiers for greppability. GitHub's own guidance is one or two alerts per
page; honor it (`architecture.md` currently has 7). Reserve ☠️ `CAUTION` for genuinely destructive
outcomes — the debug-signing fallback and the credential paths, roughly three sites.

### Why GitHub alert syntax everywhere

`gh-admonitions` (`mkdocs.yml:108`) converts `> [!TYPE]` blockquotes to Material admonitions at build
time, so one source file renders on GitHub **and** on the docs site. That matters because module READMEs
are read on GitHub and fed to Dokka, and `README.md` is the front door.

> [!WARNING]
> Native `!!! type` renders as literal broken text on GitHub and must never appear in this repo.

Verified constraints of the plugin (v0.1.1, https://github.com/pgijsbers/admonitions):

- **Exactly 5 types** — `NOTE`, `TIP`, `IMPORTANT`, `WARNING`, `CAUTION`. Material's `bug`, `example`,
  `danger`, `question`, `success`, `abstract`, `info` and `quote` are unreachable.
- `IMPORTANT` → warning icon/color, `CAUTION` → danger icon/color, both keeping their own title. These
  are the two GitHub types Material lacks; the plugin maps rather than drops them.
- **No collapsible form, no custom titles, no inline admonitions.** Hence `<details><summary>` HTML for
  `troubleshooting.md`, which renders natively on GitHub and passes through mkdocs untouched.
- Qualifiers are case-insensitive, but standardize on ALL-CAPS anyway.
- Alert bodies may contain lists, fenced code and nested quotes, so no content needs restructuring.
- Because collapsibles are unavailable, `pymdownx.details` (`mkdocs.yml:52`) is now unused. Harmless to
  leave; noted so nobody assumes `???` works.

---

## Out of scope

- **The seven defects** found during the audit. Documented honestly in comments, listed in
  [TASKS.md](TASKS.md) under Follow-up fixes, then fixed one commit each *after* the documentation
  work landed. This kept the refactor itself free of behavioral edits. All seven are now done, each
  with tests that were confirmed to fail against the unfixed code.
- **`docs/api/`** — generated Dokka output, correctly gitignored already.
- **`CODE_OF_CONDUCT.md`** — verbatim upstream Contributor Covenant.
- **`fastlane/README.md`** — tool-generated by fastlane.
