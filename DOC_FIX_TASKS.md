# Documentation repair tasks

Companion goals, findings, and recovery map: [DOC_FIX_PLAN.md](DOC_FIX_PLAN.md).

Status: DF-00–DF-14 complete; DF-15 reader audit complete, final validation and PR preparation pending. Checked items identify completed work in the records below. Reviewed base: `1c2103890d16f81f6c6ae4c2d8626281b841e417`.
Reviewed head: `0fb7882982cfefd55df300e06549731c6fbb33bb` ([PR #647](https://github.com/atick-faisal/Jetpack-Android-Starter/pull/647)).

Complete each task by recording the changed paths, verification result, and any unresolved follow-up
below its checklist. Use task IDs in commits or review notes. Recheck the diff if the PR head changes.

## DF-00 — Record recovery coverage

Depends on: none. Scope: the recovery map in the plan and this execution record.

- [x] Capture the current PR base/head and working-tree status before implementation. Keep unrelated
  edits separate. Use the pinned audit revisions when examining the original damage.
- [x] Review every removed page in the plan's recovery map, section by section. For each substantive
  section, record one disposition: retained at a named destination, restored/reworked, replaced by a
  maintained reference, or removed with a specific reason (obsolete, incorrect, duplicate).
- [x] Repeat the comparison for sections cut from `architecture`, `state-management`, `navigation`,
  `guide`, `components`, `firebase`, `getting-started`, and `troubleshooting`, plus the fifteen
  previously existing module READMEs changed by this PR.
- [x] Include the four newly added READMEs (`core/navigation`, `core/testing`, `benchmarks`, `lint`)
  in the accuracy review. Retain their useful coverage.
- [x] Record smaller unique topics as well as major sections: utility usage, preview setup,
  localization, migration steps, permissions, test setup, and sync progress.
- [x] Give every recovery item a DF task owner. Preserve useful snippets/diagrams only after checking
  their symbols and behavior. Do not use deleted line count as evidence of either value or waste.

Accept: every removed page and substantially shortened section has a reviewable disposition; no
useful topic is covered only by “see source” or by a dead link.

The completed ledger follows.

## DF-00 completion record

Completed 2026-09-14 on `fix/docs-v3`. The audit compares the PR base [`1c2103890d16f81f6c6ae4c2d8626281b841e417`](https://github.com/atick-faisal/Jetpack-Android-Starter/tree/1c2103890d16f81f6c6ae4c2d8626281b841e417) with its reviewed head [`0fb7882982cfefd55df300e06549731c6fbb33bb`](https://github.com/atick-faisal/Jetpack-Android-Starter/tree/0fb7882982cfefd55df300e06549731c6fbb33bb) (PR #647). The working tree contained only these two new repair records before this task; no production source or existing tracked file was changed.

### Method and scope

The ledger uses `git diff --name-status` and pinned `git show <revision>:<path>` content. Each source range is assigned once: its range ends immediately before the next listed range, and the last range ends at the historical file end. “Retain” identifies coverage worth keeping; “rework”, “restore”, “replace”, and “remove” are future outcomes owned by the named DF task. They do **not** claim that the destination has already been repaired. Evidence links identify the current implementation or configuration that a later writer must check before carrying prose or examples forward.

Coverage: 14 removed documentation pages, 8 substantially shortened surviving pages, 15 previously existing module READMEs, and 4 new module READMEs. The audit also records utilities, preview/component extension setup, localization, migration/upgrade context, permission helpers, test support, and sync-progress behavior.

### Section-level recovery ledger

#### `docs/data-flow.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L20`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L1-L20) — Opening; Table of Contents | Find the appropriate data pattern | Remove duplicate navigation; reading paths belong in README.md. | DF-13 | Current README documentation links. |
| [`L21–L79`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L21-L79) — Architecture Overview: layers, unidirectional flow, principles | Understand UI/data responsibilities | Retain concepts in docs/architecture.md; qualify Room as the source for the home feature, not every repository. | DF-05 | [E05](#evidence-e05); [E07](#evidence-e07) |
| [`L80–L147`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L80-L147) — Data Flow Patterns; Network-Only Pattern (all subsections) | Choose and implement a REST-only feature | Rework in docs/data.md: selection table and NetworkDataSource example; remove unconditional freshness guarantees. | DF-06 | [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L148–L211`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L148-L211) — Local-Only Pattern (all subsections) | Persist and observe settings locally | Rework in docs/data.md using SettingsRepository and typed JSON DataStore; do not present auth tokens as stored preferences. | DF-06 | [E05](#evidence-e05); [E12](#evidence-e12) |
| [`L212–L314`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L212-L314) — Offline-First Pattern (all subsections) | Follow local write, push and pull | Rework in docs/data.md; keep the flow diagram with sequential push/pull and actual metadata; DF-02 owns delivery/conflict guarantees. | DF-06 | [E05](#evidence-e05); [E13](#evidence-e13) |
| [`L315–L364`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L315-L364) — Real-Time Data Updates (both subsections) | Extend sync with live updates | Rework as a clearly labeled extension in docs/data.md; current Firestore pull uses get(), not a snapshot listener. | DF-06 | [E11](#evidence-e11) |
| [`L365–L426`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L365-L426) — Caching Strategies: time-based invalidation; manual refresh | Choose a refresh policy | Rework in docs/data.md as possible extensions; neither TTL caching nor pull-to-refresh is the current home UI. | DF-06 | [E05](#evidence-e05); [E07](#evidence-e07) |
| [`L427–L461`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L427-L461) — Error Handling; Error Flow Diagram | Propagate failures through layers | Rework in docs/data.md and link docs/state-management.md; returned Result failures, Flow exceptions and cancellation have different paths. | DF-06 | [E06](#evidence-e06); [E09](#evidence-e09) |
| [`L462–L493`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/data-flow.md#L462-L493) — Summary; Further Reading | Find deeper help | Remove duplicate summary; replace deleted-page links via README.md and repaired guide cross-links. | DF-14 | Current docs/data.md Further reading. |

#### `docs/dependency-injection.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L86`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L1-L86) — Overview; Quick Start (four steps) | Enable Hilt and inject app/screen dependencies | Rework module setup in docs/guide.md and docs/build-and-tooling.md; use App, actual plugins and Screen injection. | DF-08 | [E01](#evidence-e01); [E04](#evidence-e04); [E07](#evidence-e07) |
| [`L87–L202`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L87-L202) — Core Concepts: @Binds vs @Provides | Bind interfaces and construct third-party types | Restore practical repository, Room and Retrofit examples in docs/data.md; explain each role without the blanket efficiency claim. | DF-06 | [E04](#evidence-e04) |
| [`L203–L246`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L203-L246) — Scoping (Singleton, ViewModelScoped, ActivityScoped) | Choose lifetime and install component | Rework in docs/data.md around actual Singleton bindings; label other scopes as extensions, not existing template usage. | DF-06 | [E04](#evidence-e04) |
| [`L247–L302`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L247-L302) — Qualifier Annotations | Distinguish dispatchers and contexts | Restore all three dispatcher qualifiers and ApplicationContext usage in docs/data.md. | DF-06 | [E04](#evidence-e04); [E09](#evidence-e09) |
| [`L303–L423`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L303-L423) — Injection Patterns: constructor, field, assisted Worker | Inject runtime and graph-supplied arguments | Rework in docs/data.md; include worker delegation and collection of sync Flow, with keyed ViewModel link to docs/navigation.md. | DF-06 | [E04](#evidence-e04); [E07](#evidence-e07); [E13](#evidence-e13) |
| [`L424–L500`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L424-L500) — Module Organization; Module Inclusion Pattern | Locate bindings and wire modules | Restore actual DI locations in docs/data.md; remove the unsupported assertion that includes specifies processing order. | DF-06 | [E04](#evidence-e04) |
| [`L501–L607`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L501-L607) — Common Patterns (four examples) | Construct repositories, DataStore, CredentialManager and Retrofit | Restore concise examples in docs/data.md with current symbols and dispatcher wiring. | DF-06 | [E04](#evidence-e04); [E11](#evidence-e11); [E12](#evidence-e12) |
| [`L608–L650`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L608-L650) — Convention Plugin Pattern | Apply the shared Hilt build setup | Retain plugin reference in docs/build-and-tooling.md; restore an application example in docs/guide.md. | DF-04 | [E01](#evidence-e01); [E04](#evidence-e04) |
| [`L651–L711`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L651-L711) — Testing with Hilt: unit and integration tests | Replace dependencies for tests | Rework in docs/guide.md with real constructor-injected fakes; label Hilt instrumentation setup as an extension needing its own configuration. | DF-08 | [E10](#evidence-e10) |
| [`L712–L834`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L712-L834) — Troubleshooting (seven compilation/configuration symptoms) | Diagnose missing bindings, fields, scopes, KSP and cycles | Rework in docs/troubleshooting.md; use actual build diagnostics; do not restore cache-clearing or Lazy as universal fixes. | DF-10 | [E04](#evidence-e04); [E01](#evidence-e01) |
| [`L835–L930`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L835-L930) — Best Practices: Do’s and Don’ts | Choose maintainable injection boundaries | Merge useful constructor/qualifier/lifetime advice into docs/data.md; remove duplicated examples and blanket alternative-framework bans. | DF-06 | [E04](#evidence-e04) |
| [`L931–L988`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L931-L988) — Advanced Topics: custom components, entry points, multibindings | Recognize optional DI extensions | Remove unused generic examples; docs/data.md should point readers needing these extensions to maintained Hilt reference guidance. | DF-06 | No such feature claimed by current docs/data.md; examples explicitly hypothetical at base. |
| [`L989–L1046`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency-injection.md#L989-L1046) — Migration Guide (five steps); Further Reading; Summary | Wire an added module and validate it | Replace duplicate migration walkthrough with docs/guide.md setup/testing and docs/data.md DI reference. | DF-08 | [E01](#evidence-e01); [E04](#evidence-e04); [E10](#evidence-e10) |

#### `docs/dependency.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L81`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency.md#L1-L81) — Summary; Version Catalog: structure and usage | Add catalog versions, libraries, plugins and BOMs | Rework in docs/build-and-tooling.md using current aliases; separate catalog versions from explicitly pinned workflow/init-script tools. | DF-04 | [E01](#evidence-e01) |
| [`L82–L103`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency.md#L82-L103) — Automated Dependency Updates: Renovate | Adopt automated dependency updates | Restore instructions in docs/build-and-tooling.md using .github/renovate.json, not the old root example. | DF-04 | [E01](#evidence-e01) |
| [`L104–L133`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency.md#L104-L133) — Dependabot Setup | Know which updater is configured | Remove: .github/dependabot.yml is absent; do not describe it as supplied. | DF-04 | rg --files --hidden found .github/renovate.json only. |
| [`L134–L190`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/dependency.md#L134-L190) — Best Practices; Adding New Dependencies; Further Reading | Add a dependency safely | Restore version/alias/consumer steps and approval boundary in docs/build-and-tooling.md; keep BOM guidance with actual catalog examples. | DF-04 | [E01](#evidence-e01) |

#### `docs/faq.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L19`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L1-L19) — Opening; Table of Contents | Find answers by task | Replace FAQ navigation with README.md reading paths and topic-guide links. | DF-13 | Current README.md. |
| [`L20–L198`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L20-L198) — Architecture Questions (all three questions and use-case examples) | Decide when to add a domain layer | Retain extension criteria in docs/architecture.md; remove duplicated hypothetical use-case implementations and incomplete mkdir-only module setup. | DF-05 | [E01](#evidence-e01); [E05](#evidence-e05) |
| [`L199–L363`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L199-L363) — Component Questions (custom appearance, new component, selection) | Customize and select components | Rework in docs/components.md; JetpackButton has no colors parameter, so do not restore that override example. | DF-09 | [E08](#evidence-e08) |
| [`L364–L483`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L364-L483) — Add a new data source: Retrofit and Room | Extend persistence/network access | Replace with docs/guide.md registration steps and docs/data.md REST option; discard malformed UsersApi parameter syntax. | DF-08 | [E04](#evidence-e04); [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L484–L547`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L484-L547) — Choose between Room, DataStore and Firestore | Choose storage appropriate to a feature | Restore a comparison in docs/data.md, distinguishing JSON preferences, relational local data and remote sync. | DF-06 | [E05](#evidence-e05); [E11](#evidence-e11); [E12](#evidence-e12) |
| [`L548–L649`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L548-L649) — Handle offline scenarios (five steps) | Understand offline writes and scheduling | Rework in docs/data.md; remove periodic-sync assertion and fictional posts implementation. | DF-06 | [E05](#evidence-e05); [E13](#evidence-e13) |
| [`L650–L745`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L650-L745) — Choose updateState/updateStateWith/updateWith | Select the state helper | Retain decision table in docs/state-management.md, correcting data replacement and Result contracts. | DF-07 | [E06](#evidence-e06) |
| [`L746–L843`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L746-L843) — Handle multiple loading states | Coordinate independent or combined operations | Restore qualified guidance in docs/state-management.md; do not copy getOrThrow under helpers as if they catch thrown exceptions. | DF-07 | [E06](#evidence-e06) |
| [`L844–L905`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L844-L905) — Reset state | Reset form data and event/loading state deliberately | Restore a labeled example in docs/state-management.md; distinguish resetting data via updateState from replacing the whole UiState. | DF-07 | [E06](#evidence-e06) |
| [`L906–L1108`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L906-L1108) — Prepare for release; signing; CI/CD | Prepare a signed release and deployment inputs | Rework in docs/build-and-tooling.md with docs/getting-started.md signing link; replace invented secret names and custom APK filename. | DF-04 | [E01](#evidence-e01); [E02](#evidence-e02) |
| [`L1109–L1193`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L1109-L1193) — Optimize Compose performance (four examples) | Measure and address UI work | Rework in docs/components.md with profiling link to docs/build-and-tooling.md; remove unconditional lambda and derivedStateOf prescriptions. | DF-09 | [E08](#evidence-e08) |
| [`L1194–L1243`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L1194-L1243) — Reduce app size (four steps) | Inspect release size and current shrinking settings | Rework in docs/build-and-tooling.md; shrinking already enabled; validate analysis commands instead of copying analyzeReleaseBundle. | DF-04 | [E01](#evidence-e01) |
| [`L1244–L1343`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L1244-L1343) — Firebase setup; Google Sign-In failures (all subsections) | Configure and diagnose auth | Replace with docs/firebase.md and docs/troubleshooting.md; remove fictional Android-14-only legacy fallback implementation. | DF-03 | [E11](#evidence-e11) |
| [`L1344–L1386`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/faq.md#L1344-L1386) — Additional Resources; Still Have Questions? | Find guides and report a reproducible issue | Replace link inventories with README.md and docs/troubleshooting.md Getting Additional Help. | DF-14 | Current guide help section. |

#### `docs/fastlane.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L44`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/fastlane.md#L1-L44) — Summary; Prerequisites; Initial Setup | Prepare Ruby/Bundler and Play credentials | Restore in docs/build-and-tooling.md; verify current provider requirements when implementing DF-04; keep key paths and ignored credentials explicit. | DF-04 | [E02](#evidence-e02) |
| [`L45–L79`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/fastlane.md#L45-L79) — Configure Fastlane Files: Appfile and Fastfile | Set package, service-account path and lane | Rework in docs/build-and-tooling.md using actual Appfile and AAB deploy lane; do not restore old lane config as current. | DF-04 | [E02](#evidence-e02) |
| [`L80–L109`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/fastlane.md#L80-L109) — Metadata Management | Initialize and edit store metadata | Restore in docs/build-and-tooling.md; verify supply prerequisites upstream and explain actual metadata paths. | DF-04 | [E02](#evidence-e02) |
| [`L110–L128`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/fastlane.md#L110-L128) — Deploying Updates | Prepare changelog/version and choose release workflow | Rework in docs/build-and-tooling.md: deploy generates default.txt from five commits; distinguish manual edits from overwritten output. | DF-04 | [E02](#evidence-e02) |
| [`L129–L154`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/fastlane.md#L129-L154) — Troubleshooting Common Issues; Further Reading | Diagnose credentials, upload and metadata failures | Restore targeted checks in docs/build-and-tooling.md Release and link from docs/troubleshooting.md. | DF-04 | [E02](#evidence-e02) |

#### `docs/github.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L27`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/github.md#L1-L27) — Summary; Build Workflow | Understand CI triggers and gates | Retain CI/CD table in docs/build-and-tooling.md; restore repository_dispatch trigger and actual build/test/guard tasks. | DF-04 | [E02](#evidence-e02) |
| [`L28–L39`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/github.md#L28-L39) — Documentation Workflow | Set up docs validation/deployment | Rework local prerequisites in docs/build-and-tooling.md; validation/deployment corrections owned by DF-14. | DF-04 | [E03](#evidence-e03) |
| [`L40–L78`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/github.md#L40-L78) — Release Workflow; Prerequisites | Encode secrets and place them correctly | Restore four base64 inputs plus plain GOOGLE_WEB_CLIENT_ID with exact output paths in docs/build-and-tooling.md. | DF-04 | [E02](#evidence-e02) |
| [`L79–L100`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/github.md#L79-L100) — Creating a Release | Understand tag-triggered APK and AAB jobs | Rework in docs/build-and-tooling.md with version/signing preparation and separate GitHub/Play outcomes. | DF-04 | [E02](#evidence-e02) |
| [`L101–L119`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/github.md#L101-L119) — Debugging Failed Releases; Further Reading | Find the failed job and configuration | Restore release diagnosis in docs/build-and-tooling.md; distinguish build timeout from other job behavior. | DF-04 | [E02](#evidence-e02) |

#### `docs/index.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L48`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/index.md#L1-L48) — Branding; Apps Built With This Template; Why This Template? | Understand and recognize the project | Retain presentation in README.md; qualify release-readiness claims in DF-13. Generate homepage from README. | DF-13 | [E03](#evidence-e03) |
| [`L49–L80`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/index.md#L49-L80) — Technical Stack: Core Libraries; Build & Tools | Identify actual technology and versions | Replace stale version and Firebase Performance list with README.md current stack. | DF-13 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L81–L263`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/index.md#L81-L263) — Documentation; Documentation Map through Module Documentation | Find onboarding, reference, tooling, deployment and module docs | Restore useful reading paths in README.md, with docs/build-and-tooling.md local site/API assembly instructions; replace dead links. | DF-13 | [E03](#evidence-e03) |
| [`L264–L355`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/index.md#L264-L355) — Module Dependency Graph; dependency rules | Locate actual Gradle dependencies | Rebuild in docs/architecture.md from current declarations, including core:navigation, core:testing, benchmarks and lint. | DF-05 | [E01](#evidence-e01) |
| [`L356–L368`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/index.md#L356-L368) — Cross-References & Integration | Understand how guides fit together | Rework README.md reading map and docs/architecture.md cross-links. | DF-13 | Current ten guides. |
| [`L369–L392`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/index.md#L369-L392) — Documentation Statistics; closing tips and footer | Find homepage/API and license | Remove stale counts and size-based claims; retain API/license access in README.md and generated homepage. | DF-13 | [E03](#evidence-e03) |

#### `docs/license.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L195`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/license.md#L1-L195) — Apache License; Terms §§1–9; Appendix | Reach authoritative license text and attribution | Replace duplicated license transcription with root LICENSE access; DF-14 must preserve or explicitly map the old public URL and anchors. No license wording change. | DF-14 | Root LICENSE; current mkdocs.yml License link. |

#### `docs/performance.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L27`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L1-L27) — Compose Compiler Metrics; Using the Reports | Generate and interpret compiler reports | Restore flags, root build/<module> output paths and report inspection in docs/build-and-tooling.md. | DF-04 | [E01](#evidence-e01) |
| [`L28–L64`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L28-L64) — Optimizing Composables | Understand stability contracts | Rework in docs/components.md; annotations do not make mutable data immutable; link compiler stability configuration. | DF-09 | [E08](#evidence-e08) |
| [`L65–L149`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L65-L149) — LazyList Optimization (keys, contentType, computations) | Improve list behavior and measure work | Restore concise guidance in docs/components.md based on HomeScreen usage; label mixed-list additions as illustrative. | DF-09 | [E07](#evidence-e07); [E08](#evidence-e08) |
| [`L150–L233`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L150-L233) — Image Loading Best Practices (three subsections) | Use/configure image loading | Rework in docs/components.md and core/network/README.md; verify actual loader, tint and request customization. | DF-09 | [E08](#evidence-e08); [E11](#evidence-e11) |
| [`L234–L311`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L234-L311) — Memory Leak Prevention (four subsections) | Manage lifetimes and coroutine cancellation | Rework lifetime guidance in docs/state-management.md and diagnosis in docs/troubleshooting.md; Worker wiring belongs in docs/data.md. | DF-07 | [E06](#evidence-e06); [E09](#evidence-e09); [E13](#evidence-e13) |
| [`L312–L341`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L312-L341) — R8 and ProGuard Optimization | Diagnose release-only failures | Rework in docs/build-and-tooling.md; remove blanket model keep example and path-based safety guarantees. | DF-04 | [E01](#evidence-e01) |
| [`L342–L387`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L342-L387) — Startup Optimization (lazy initialization; DI) | Understand startup work | Replace unsupported performance justification with actual App wiring and measurement in docs/build-and-tooling.md. | DF-04 | [E07](#evidence-e07); [E10](#evidence-e10) |
| [`L388–L432`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L388-L432) — Gradle Build Optimization (scan; modules) | Inspect build performance | Retain settings summary and restore targeted module command in docs/build-and-tooling.md; describe actual scan publication condition. | DF-04 | [E01](#evidence-e01) |
| [`L433–L454`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L433-L454) — Baseline Profiles; Planned Implementation | Generate and measure profiles | Replace obsolete planned status with current docs/build-and-tooling.md Baseline profiles and benchmarks/README.md. | DF-04 | [E10](#evidence-e10) |
| [`L455–L496`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L455-L496) — Firebase Performance Monitoring | Know whether monitoring is included | Remove: no firebase-perf plugin or monitoring implementation in template; distinguish from Crashlytics in docs/firebase.md. | DF-03 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L497–L526`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/performance.md#L497-L526) — Profiling and Monitoring; Further Reading | Measure before/after optimizations | Restore profiling entry points in docs/build-and-tooling.md and link from UI diagnosis; refresh IDE instructions against current tools. | DF-04 | [E10](#evidence-e10) |

#### `docs/philosophy.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L16`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/philosophy.md#L1-L16) — Opening; Pragmatic Simplicity | Understand design tradeoffs | Rework in docs/architecture.md without assertions that correctness was sacrificed or that most apps need this architecture. | DF-05 | [E05](#evidence-e05) |
| [`L17–L130`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/philosophy.md#L17-L130) — Centralized State Management; Direct ViewModels; Error Handling; State Updates | Understand state helpers and failure propagation | Replace repeated/inaccurate examples with docs/state-management.md contracts and docs/data.md error boundary. | DF-07 | [E06](#evidence-e06); [E09](#evidence-e09) |
| [`L131–L139`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/philosophy.md#L131-L139) — Feature Organization | Locate feature files | Replace with actual screen/ViewModel layout in docs/guide.md. | DF-08 | [E07](#evidence-e07) |
| [`L140–L196`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/philosophy.md#L140-L196) — Conscious Trade-offs; Benefits; When to Break These Rules | Know when to extend defaults | Restore concise extension criteria in docs/architecture.md; unique loading/error UI goes to docs/state-management.md. | DF-05 | [E05](#evidence-e05); [E06](#evidence-e06) |
| [`L197–L213`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/philosophy.md#L197-L213) — Summary; Further Reading | Find the canonical explanation | Remove repeated summary; replace dead navigation through docs/architecture.md. | DF-14 | Current architecture Further Reading. |

#### `docs/plugins.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L91`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/plugins.md#L1-L91) — Summary; Available Plugins (five plugin sections) | Choose the appropriate convention plugin | Retain current seven-plugin table in docs/build-and-tooling.md; remove obsolete compiler-flag and Firebase Performance claims. | DF-04 | [E01](#evidence-e01) |
| [`L92–L107`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/plugins.md#L92-L107) — Using the Plugins | Apply plugin aliases in a module | Restore short actual module setup in docs/build-and-tooling.md and docs/guide.md. | DF-04 | [E01](#evidence-e01) |
| [`L108–L143`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/plugins.md#L108-L143) — Creating Custom Plugins | Locate class, registration and alias | Retain three-step outline in docs/build-and-tooling.md with approval boundary; no new plugin added. | DF-04 | [E01](#evidence-e01) |
| [`L144–L204`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/plugins.md#L144-L204) — Plugin Development Tips; Best Practices; Further Reading | Extend shared config consistently | Rework catalog/extension/dependency examples in docs/build-and-tooling.md, reflecting current AGP and isolated-root path use. | DF-04 | [E01](#evidence-e01) |

#### `docs/quick-reference.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L41`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L1-L41) — Opening; Summary; Table of Contents | Find common patterns quickly | Replace duplicate index with README.md reading paths and docs/build-and-tooling.md command reference. | DF-13 | [E03](#evidence-e03) |
| [`L42–L139`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L42-L139) — State Management (three subsections) | Initialize state and select helpers | Replace with docs/state-management.md corrected table and actual Screen pattern; helpers do not return the Result they consume. | DF-07 | [E06](#evidence-e06) |
| [`L140–L197`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L140-L197) — Navigation (routes; extensions) | Register and navigate destinations | Replace Navigation 2 snippets with docs/navigation.md NavKey/entry/Navigator examples. | DF-08 | [E07](#evidence-e07) |
| [`L198–L246`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L198-L246) — Dependency Injection (three subsections) | Inject ViewModels, repositories and dispatchers | Retain in docs/data.md and docs/guide.md with actual bindings and qualifiers. | DF-06 | [E04](#evidence-e04) |
| [`L247–L297`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L247-L297) — Error Handling (three layers) | Propagate errors to UI | Rework in docs/data.md with docs/state-management.md link; qualify automatic error display. | DF-06 | [E06](#evidence-e06); [E09](#evidence-e09) |
| [`L298–L378`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L298-L378) — Coroutines & Threading; Common Extensions | Use timeout, flow, context and update utilities | Restore timeout overload/units and utility reference in core/android/README.md; place lifecycle/update examples in docs/state-management.md. Remove mislabeled context-switch snippet posing as a context extension. | DF-11 | [E09](#evidence-e09) |
| [`L379–L510`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L379-L510) — Complete Example: Feature Implementation (five steps) | Build a feature end to end | Replace duplicate fictional Profile/Route tutorial with docs/guide.md; preserve model→repository→UI→navigation sequence. | DF-08 | [E07](#evidence-e07) |
| [`L511–L568`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/quick-reference.md#L511-L568) — Further Reading; Quick Commands; Need Help? | Build, test, format and generate docs | Restore usable commands in docs/build-and-tooling.md; replace navigation with README.md and docs/troubleshooting.md. | DF-04 | [E01](#evidence-e01); [E03](#evidence-e03) |

#### `docs/spotless.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L47`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/spotless.md#L1-L47) — Summary; Features; Using Spotless (IDE and CI) | Run formatting and understand covered files | Retain table in docs/build-and-tooling.md; correct all commands to include init script and no-configuration-cache. | DF-04 | [E01](#evidence-e01); [E02](#evidence-e02) |
| [`L48–L75`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/spotless.md#L48-L75) — Customizing Copyright Headers | Personalize all four header templates | Restore steps in docs/build-and-tooling.md using spotless/copyright.*; changing actual templates is not this repair. | DF-04 | [E01](#evidence-e01) |
| [`L76–L110`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/spotless.md#L76-L110) — Configuration Details (Kotlin; other file types) | Locate ktlint and file targets | Retain target table in docs/build-and-tooling.md; verify observed subproject/included-build scope. | DF-04 | [E01](#evidence-e01) |
| [`L111–L134`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/spotless.md#L111-L134) — Best Practices; Further Reading | Use formatter during development | Replace stale invocation and duplicate workflow advice with docs/build-and-tooling.md and CONTRIBUTING.md. | DF-04 | [E01](#evidence-e01) |

#### `docs/tips.md` — removed page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L75`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L1-L75) — Opening; Pre-built Components; Theming System | Select components and theme them | Replace duplicate examples with docs/components.md catalog and corrected customization examples. | DF-09 | [E08](#evidence-e08) |
| [`L76–L100`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L76-L100) — Composable Previews | Preview devices and themes | Restore in docs/components.md; verify combined annotation behavior before repeating the claimed Cartesian product. | DF-09 | [E08](#evidence-e08) |
| [`L101–L180`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L101-L180) — State Management Utilities; Automatic Error Handling | Use helpers and render errors/loading | Replace with docs/state-management.md contracts; remove Route split and overlay/report automation overclaims. | DF-07 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L181–L221`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L181-L221) — Data Layer Tips: String Validation Extensions | Validate auth input | Restore examples in docs/components.md with links to core/android/README.md; verify actual nullable validators and form error types. | DF-09 | [E09](#evidence-e09) |
| [`L222–L249`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L222-L249) — IDE Productivity: Secrets Management | Set real build configuration keys | Replace API_KEY/API_SECRET examples with BACKEND_URL in docs/build-and-tooling.md and GOOGLE_WEB_CLIENT_ID in docs/firebase.md. | DF-04 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L250–L277`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L250-L277) — Documentation Generation | Generate API docs and preview the site | Restore complete local assembly in docs/build-and-tooling.md, including Python dependencies, generated index/API and strict build. | DF-04 | [E03](#evidence-e03) |
| [`L278–L309`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L278-L309) — Multilingual Support | Add and persist an app language | Restore complete resource→Language→picker→locale path in docs/components.md; language is applied through AppCompat, not a DataStore language field. | DF-09 | [E12](#evidence-e12) |
| [`L310–L355`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L310-L355) — Performance Tips (images; loading) | Use loading indicators and image settings correctly | Rework in docs/components.md; distinguish current StatefulComposable indicator, image loader defaults and unsupported blocking/crossfade claims. | DF-09 | [E06](#evidence-e06); [E08](#evidence-e08) |
| [`L356–L397`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L356-L397) — Best Practices (components; documentation) | Find conventions and documentation surfaces | Merge component guidance into docs/components.md and documentation workflow into docs/build-and-tooling.md; policy itself belongs to DF-01. | DF-09 | [E03](#evidence-e03); [E08](#evidence-e08) |
| [`L398–L474`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L398-L474) — Git Workflow Tips (commit style; pre-commit; ignore) | Prepare a clean, reviewable change | Rework CONTRIBUTING.md/root workflow guidance; do not claim automatic semantic versioning or that ignored tracked Firebase config is protected. | DF-13 | [E01](#evidence-e01); [E02](#evidence-e02) |
| [`L475–L521`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L475-L521) — Android Studio Productivity (run configs; EditorConfig) | Use project IDE helpers | Retain run-config inventory in docs/build-and-tooling.md; refresh names and actual formatting settings. | DF-04 | [E01](#evidence-e01) |
| [`L522–L547`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L522-L547) — Debugging Tips: LeakCanary | Investigate leaks in debug builds | Restore practical trace workflow in docs/troubleshooting.md. | DF-10 | [E01](#evidence-e01) |
| [`L548–L567`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L548-L567) — Compose Preview Optimization | Combine and inspect previews | Merge with restored docs/components.md preview guidance; do not duplicate the unverified eight-preview claim. | DF-09 | [E08](#evidence-e08) |
| [`L568–L608`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/tips.md#L568-L608) — Build Performance; Module Documentation; Concept Guides | Inspect slow builds and find references | Retain build settings in docs/build-and-tooling.md; replace unconditional clean/scan advice and deleted links. | DF-04 | [E01](#evidence-e01); [E03](#evidence-e03) |

#### `docs/architecture.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L126`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L1-L126) — Principles; Core Layers (UI and data) | Understand module responsibilities | Retain two-layer explanation; replace fictional HomeAction/Data examples in docs/architecture.md with current code or labeled conceptual excerpts. | DF-05 | [E05](#evidence-e05); [E07](#evidence-e07) |
| [`L127–L175`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L127-L175) — State Management | Understand shared screen-state shape | Retain short UiState introduction and docs/state-management.md link; remove outdated compiler-flag advice. | DF-05 | [E06](#evidence-e06); [E01](#evidence-e01) |
| [`L176–L202`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L176-L202) — Dependency Injection; Data Flow | Trace calls and construction | Retain overview in docs/architecture.md with restored docs/data.md construction examples; add actual module dependency map. | DF-05 | [E01](#evidence-e01); [E04](#evidence-e04) |
| [`L203–L221`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L203-L221) — Adding a Domain Layer | Recognize when to extend layering | Retain criteria in docs/architecture.md Domain Layer; label illustrative use-case types explicitly. | DF-05 | [E05](#evidence-e05) |
| [`L222–L243`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L222-L243) — Testing Strategy; Best Practices | Find test support and implementation conventions | Replace obsolete planned-testing assertion with docs/guide.md worked test; retain focused conventions without counts. | DF-05 | [E10](#evidence-e10) |
| [`L244–L308`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L244-L308) — Integration Patterns: Navigation + State Management | Preserve navigation arguments and screen state | Replace SavedStateHandle/toRoute example with docs/navigation.md assisted injection and restoration. | DF-08 | [E07](#evidence-e07) |
| [`L309–L375`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L309-L375) — Firebase + Data Layer Integration | Understand authenticated data flow | Rework in docs/architecture.md using AuthRepository/preferences and HomeRepository; remove fabricated FirebaseAuthWrapper listener example. | DF-05 | [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L376–L436`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L376-L436) — Dependency Injection Integration | Wire data sources through repository to UI | Restore a concise actual chain in docs/data.md and docs/guide.md, linked from docs/architecture.md. | DF-06 | [E04](#evidence-e04); [E07](#evidence-e07) |
| [`L437–L511`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L437-L511) — Sync + Repositories Integration | Connect worker, repository, constraints and progress | Rework in docs/data.md; remove periodic scheduling assertion, collect sync Flow, and distinguish progress notification from WorkInfo state. | DF-06 | [E05](#evidence-e05); [E13](#evidence-e13) |
| [`L512–L601`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/architecture.md#L512-L601) — Complete Integration Flow Example; Summary; Further Reading | Follow one complete screen/data flow | Rework surviving How It Fits Together diagram in docs/architecture.md: request-triggered push then pull, not periodic fetch. Replace stale cross-links. | DF-05 | [E05](#evidence-e05); [E07](#evidence-e07); [E13](#evidence-e13) |

#### `docs/state-management.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L141`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L1-L141) — Opening; Core Concepts; UiState definition/purpose/rationale | Understand data, loading and event state | Retain UiState explanation in docs/state-management.md; remove blanket prohibitions on multiple flows and qualify exactly-once language. | DF-07 | [E06](#evidence-e06) |
| [`L142–L317`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L142-L317) — Update Functions (three helpers and examples) | Choose helper and understand its result | Rework table/examples in docs/state-management.md: updateState replaces data; async helpers inspect Result and do not catch thrown exceptions. | DF-07 | [E06](#evidence-e06) |
| [`L318–L391`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L318-L391) — Kotlin Context Parameters (all subsections) | Understand the implicit ViewModel argument | Retain current named context example; remove obsolete receiver syntax, fake compiler path and before/after repetition. Verify compiler support under DF-07. | DF-07 | [E01](#evidence-e01); [E06](#evidence-e06) |
| [`L392–L581`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L392-L581) — Complete Examples: posts list; validated form | Implement a list and form | Replace hypothetical full examples with docs/guide.md Home/Item flow and auth validation in docs/components.md. Correct Item failure behavior before recommending it. | DF-08 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L582–L675`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L582-L675) — Advanced Patterns: multiple operations; partial loading | Manage multiple requests and independent loading | Restore bounded guidance in docs/state-management.md; Result.flatMap/async examples must not masquerade as existing helper contracts. | DF-07 | [E06](#evidence-e06) |
| [`L676–L714`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L676-L714) — Advanced Patterns: optimistic updates | Understand immediate updates and failed persistence | Rework as an optional extension in docs/state-management.md with rollback/concurrent-edit caveats; distinguish Room-first writes from remote success. | DF-07 | [E05](#evidence-e05); [E06](#evidence-e06) |
| [`L715–L813`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L715-L813) — Anti-Patterns (four cases) | Choose defaults without banning valid state shapes | Rework in docs/state-management.md; retain repository boundary and normal helper usage, remove blanket bans on nullable fields/manual loading/separate flows. | DF-07 | [E06](#evidence-e06) |
| [`L814–L916`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L814-L916) — Migration Guide: LiveData and sealed-state migrations | Adapt an existing screen to UiState | Remove duplicate generic before/after tutorials; docs/state-management.md should explain UiState integration without declaring other state models invalid. | DF-07 | [E06](#evidence-e06) |
| [`L917–L949`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/state-management.md#L917-L949) — Summary; Further Reading | Find deeper state examples and API docs | Replace duplicate summary and stale API paths with docs/state-management.md testing/navigation links. | DF-14 | [E03](#evidence-e03); [E10](#evidence-e10) |

#### `docs/navigation.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L98`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L1-L98) — Opening; Overview (architecture, components, flow, graph) | Understand navigation structure | Replace old NavHost graph and Route inventory with current docs/navigation.md Concepts and app auth branches. | DF-08 | [E07](#evidence-e07) |
| [`L99–L160`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L99-L160) — Type-Safe Navigation (definitions; organization) | Declare and organize destinations | Retain NavKey definitions in docs/navigation.md; remove obsolete graph marker types and unsupported argument-type promises. | DF-08 | [E07](#evidence-e07) |
| [`L161–L283`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L161-L283) — Simple Navigation (extensions, graph builders, host) | Register and reach a screen | Replace with docs/navigation.md Registering entries and Wiring NavDisplay; retain per-feature extension organization. | DF-08 | [E07](#evidence-e07) |
| [`L284–L427`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L284-L427) — Navigation with Arguments (required, optional, complex) | Pass destination data into a ViewModel | Replace toRoute/SavedStateHandle code with ItemNavKey and assisted factory in docs/navigation.md; explain nullable/default argument distinction. | DF-08 | [E07](#evidence-e07) |
| [`L428–L515`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L428-L515) — Nested Navigation Graphs (four subsections) | Separate authenticated and signed-out flows | Replace obsolete nesting recipes with independent SignedInNavigation/SignedOutNavigation state in docs/navigation.md. | DF-08 | [E07](#evidence-e07) |
| [`L516–L658`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L516-L658) — Adaptive Navigation (five subsections) | Add a tab and adapt app chrome | Restore top-level destination registration steps in docs/navigation.md; verify actual scaffold modes and current chrome visibility. | DF-08 | [E07](#evidence-e07); [E08](#evidence-e08) |
| [`L659–L797`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L659-L797) — Back Stack Management (six subsections) | Navigate back, switch tabs and prevent duplicate entries | Retain Navigator behavior and restoration in docs/navigation.md; replace NavOptions/popUpTo recipes and distinguish equal keys from key types. | DF-08 | [E07](#evidence-e07) |
| [`L798–L900`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L798-L900) — Navigation Testing (actions; back stack) | Test navigation and restoration | Replace TestNavHostController examples with NavigatorTest/NavigationStateTest-based walkthrough in docs/navigation.md. | DF-08 | [E10](#evidence-e10) |
| [`L901–L987`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L901-L987) — Common Patterns (conditional, after async, arguments) | Navigate after a successful operation | Rework in docs/navigation.md with docs/state-management.md link; Item save result is a DF-02 concern, not proof of success. | DF-08 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L988–L1138`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L988-L1138) — Best Practices: Do’s and Don’ts | Keep navigation responsibilities clear | Retain typed keys/callbacks/current state APIs in docs/navigation.md; remove Route separation and obsolete NavController restrictions/examples. | DF-08 | [E07](#evidence-e07) |
| [`L1139–L1243`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L1139-L1243) — Troubleshooting (five cases) | Diagnose args, stack clearing, registration, restoration and chrome | Rework current equivalents in docs/troubleshooting.md with docs/navigation.md links; remove nested-graph recipe itself. | DF-10 | [E07](#evidence-e07) |
| [`L1244–L1284`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/navigation.md#L1244-L1284) — Summary; Further Reading | Find navigation guidance | Replace repeated old APIs and links with current docs/navigation.md cross-links; preserve old anchor disposition under DF-14. | DF-14 | [E03](#evidence-e03); [E07](#evidence-e07) |

#### `docs/guide.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L34`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L1-L34) — Opening; Summary; Overview of Steps | Follow a complete feature implementation | Retain home as the running feature in docs/guide.md; add module creation, Gradle registration, app dependency and resource-prefix steps. | DF-08 | [E01](#evidence-e01); [E07](#evidence-e07) |
| [`L35–L64`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L35-L64) — Step 1: Data Models | Define storage/remote/repository models | Rework current Jetpack excerpt in docs/guide.md with complete sync metadata or explicitly labeled omissions and mapper coverage. | DF-08 | [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L65–L122`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L65-L122) — Step 2: Data Sources (network; local) | Add DAO and data-source access | Restore DAO/database/DI registration in docs/guide.md; link docs/data.md for REST-only alternative; remove fictional FeatureApi as existing code. | DF-08 | [E04](#evidence-e04); [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L123–L198`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L123-L198) — Step 3: Repository (models; implementation) | Map data and expose Result/Flow | Retain current HomeRepository excerpt in docs/guide.md; label omitted code and link sync constraints rather than promising guaranteed persistence. | DF-08 | [E05](#evidence-e05); [E13](#evidence-e13) |
| [`L199–L292`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L199-L292) — Step 4: UI (screen data; ViewModel; components) | Build observable UI and input handling | Retain Home/Item examples with corrected error handling after DF-02; restore active-collector setup and explicit Screen callbacks. | DF-08 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L293–L334`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L293-L334) — Step 5: Navigation | Wire the feature into the app | Replace Navigation 2 example with current keys/entries; include list/detail metadata or label omission, and app-level top-level registration. | DF-08 | [E07](#evidence-e07) |
| [`L335–L368`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L335-L368) — Step 6: Dependency Injection | Bind repository and data source | Restore full module/install context and provider registration in docs/guide.md. | DF-08 | [E04](#evidence-e04) |
| [`L369–L400`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L369-L400) — Best Practices Reminder | Check layout, errors and dispatcher boundaries | Retain focused guidance in docs/guide.md; remove Route split and link precise helper contracts. | DF-08 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L401–L430`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/guide.md#L401-L430) — Testing; Further Reading | Verify a newly added feature | Replace obsolete planned tests with a worked MainDispatcherRule/fake/Flow success-and-failure test and module commands. | DF-08 | [E10](#evidence-e10) |

#### `docs/components.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L81`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1-L81) — Overview; philosophy; selection guide | Choose existing components or extend them | Rework docs/components.md opening and selection guidance; remove unconditional accessibility/performance claims and fabricated component references. | DF-09 | [E08](#evidence-e08) |
| [`L82–L192`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L82-L192) — Buttons (filled, outlined, text) | Use button slots and defaults | Retain catalog; restore compact usage/slot guidance in docs/components.md with actual overloads; no unsupported colors override. | DF-09 | [E08](#evidence-e08) |
| [`L193–L340`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L193-L340) — Text Fields (standard, password, validation/login examples) | Build validated inputs | Retain catalog and auth usage in docs/components.md; verify TextFieldData/errorMessage/keyboard API and actual visibility state. | DF-09 | [E08](#evidence-e08); [E09](#evidence-e09) |
| [`L341–L446`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L341-L446) — Top App Bars (four variants and scaffold example) | Choose title, navigation, action and avatar layout | Retain docs/components.md app-bar catalog; restore useful slot/description examples using callbacks and current argument names. | DF-09 | [E08](#evidence-e08) |
| [`L447–L522`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L447-L522) — Loading Indicators (wheel; overlay) | Choose inline or overlaid feedback | Rework docs/components.md guidance; StatefulComposable uses ContainedLoadingIndicator; do not claim an overlay inherently blocks interaction. | DF-09 | [E06](#evidence-e06); [E08](#evidence-e08) |
| [`L523–L629`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L523-L629) — Filter Chips; Tags | Represent selection and following | Retain catalog in docs/components.md; remove misleading non-toggleable/persistence distinction and show caller-owned state. | DF-09 | [E08](#evidence-e08) |
| [`L630–L723`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L630-L723) — Icon Toggle Buttons; Toggle Options | Choose binary or exclusive selection | Retain docs/components.md catalog and settings example; verify defaults and distinguish suggested option counts from enforced limits. | DF-09 | [E08](#evidence-e08); [E12](#evidence-e12) |
| [`L724–L768`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L724-L768) — Floating Action Buttons | Expose a primary action | Retain JetpackExtendedFab reference in docs/components.md; validate resource label/description and current overload. | DF-09 | [E08](#evidence-e08) |
| [`L769–L872`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L769-L872) — Navigation Components (suite, bar, rail) | Choose adaptive navigation chrome | Rework docs/components.md with current wrapper behavior and docs/navigation.md integration; verify expanded-mode claim. | DF-09 | [E08](#evidence-e08); [E07](#evidence-e07) |
| [`L873–L931`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L873-L931) — Tabs | Switch local content | Retain catalog and restore a labeled minimal selection example in docs/components.md using existing Tab/TabRow API. | DF-09 | [E08](#evidence-e08) |
| [`L932–L972`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L932-L972) — Dividers | Separate sign-in choices | Retain real SignInScreen usage in docs/components.md; discard nonexistent GoogleSignInButton/FacebookSignInButton examples. | DF-09 | [E08](#evidence-e08); [E07](#evidence-e07) |
| [`L973–L1015`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L973-L1015) — Swipe to Dismiss | Delete a keyed list item | Retain current docs/components.md usage; describe callback transition and lack of undo without safety assurances. | DF-09 | [E08](#evidence-e08); [E07](#evidence-e07) |
| [`L1016–L1058`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1016-L1058) — Image Loading | Handle image loading, errors, tint and preview | Rework in docs/components.md: actual implementation is painter+Image; verify loader connection and remove unsupported caching/crossfade guarantees. | DF-09 | [E08](#evidence-e08); [E11](#evidence-e11) |
| [`L1059–L1141`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1059-L1141) — Background Components (solid; gradient) | Apply themed backgrounds | Retain catalog in docs/components.md; preserve actual locals and useful customization, omit decorative numeric internals unless needed. | DF-09 | [E08](#evidence-e08) |
| [`L1142–L1210`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1142-L1210) — Theming Components (colors, overrides, typography, dynamic color) | Customize branding | Restore practical steps in docs/components.md using Theme.kt color schemes, Color.kt tokens, JetpackTypography and settings preference. | DF-09 | [E08](#evidence-e08); [E12](#evidence-e12) |
| [`L1211–L1257`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1211-L1257) — Accessibility (descriptions, touch targets, contrast) | Make customized components accessible | Restore actionable guidance in docs/components.md, including decorative labels, semantics and loading/error announcements; verify custom contrast rather than promise it. | DF-09 | [E08](#evidence-e08) |
| [`L1258–L1383`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1258-L1383) — Custom Component Creation (five steps) | Add a component with useful public API and previews | Restore a labeled extension walkthrough in docs/components.md; include resource prefix, theme, KDoc and detector/test integration. | DF-09 | [E01](#evidence-e01); [E08](#evidence-e08); [E10](#evidence-e10) |
| [`L1384–L1424`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1384-L1424) — Component Organization (structure, naming, splitting) | Organize component code | Restore concise conventions in docs/components.md; remove arbitrary file-length rule and update actual file names. | DF-09 | [E08](#evidence-e08) |
| [`L1425–L1492`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1425-L1492) — Performance Considerations (composition, lists, images) | Investigate UI performance | Rework docs/components.md with measured examples and build/profiling link; remove blanket lambda advice. | DF-09 | [E08](#evidence-e08); [E10](#evidence-e10) |
| [`L1493–L1524`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1493-L1524) — Migration from XML Views | Find Compose equivalents when adopting template | Remove duplicate generic migration code; component-selection table in docs/components.md provides equivalents, with maintained Android migration reference if needed. | DF-09 | Current component catalog; template screens use Compose. |
| [`L1525–L1556`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/components.md#L1525-L1556) — Summary; Further Reading | Navigate component reference | Remove repeated guarantees; keep links to repaired guides/API and resolve old anchors. | DF-14 | [E03](#evidence-e03) |

#### `docs/firebase.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L52`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/firebase.md#L1-L52) — Summary; Prerequisites; Firebase Console Setup | Create backend and register Android app | Retain setup sequence in docs/firebase.md; clarify Crashlytics vs event analytics and application-ID/fingerprint matching. | DF-03 | [E11](#evidence-e11); [E01](#evidence-e01) |
| [`L53–L71`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/firebase.md#L53-L71) — Local Project Setup | Configure the local build and try auth | Restore explicit GOOGLE_WEB_CLIENT_ID in local.properties; retain JSON replacement steps with accurate tracked-file handling. | DF-03 | [E11](#evidence-e11); [E02](#evidence-e02) |
| [`L72–L94`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/firebase.md#L72-L94) — Troubleshooting | Diagnose Google auth, Firestore and build failures | Rework in docs/troubleshooting.md with links to exact configuration steps. | DF-10 | [E11](#evidence-e11) |
| [`L95–L110`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/firebase.md#L95-L110) — Firebase Configuration Files | Separate template, local and CI configuration | Restore in docs/firebase.md; skip-worktree is an index hint, not untracking/security; CI formats belong in docs/build-and-tooling.md. | DF-03 | [E02](#evidence-e02) |
| [`L111–L172`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/firebase.md#L111-L172) — Firestore Security Rules | Deploy rules matching app requests | Rework current docs/firebase.md example after DF-02 compatibility decision; distinguish Markdown edits from deployment and actual hard-delete request. | DF-03 | [E11](#evidence-e11) |
| [`L173–L202`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/firebase.md#L173-L202) — Additional Setup; Further Reading | Verify setup, undo index hint and separate environments | Restore verification and --no-skip-worktree steps in docs/firebase.md; label multi-environment configuration as adopter setup, not preconfigured. | DF-03 | [E01](#evidence-e01); [E11](#evidence-e11) |

#### `docs/getting-started.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L50`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/getting-started.md#L1-L50) — Summary; Quick Start; Prerequisites | Build and launch a fresh checkout | Rework docs/getting-started.md: cd into clone, JDK/SDK and compatible IDE, install/run step, expected template-config behavior. | DF-03 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L51–L68`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/getting-started.md#L51-L68) — Setting Up Firebase Features | Enable working auth and sync | Retain docs/firebase.md setup link; replace generic analytics wording with configured Crashlytics scope. | DF-03 | [E11](#evidence-e11) |
| [`L69–L97`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/getting-started.md#L69-L97) — Release Build Setup | Configure signing | Retain keystore properties/path/build steps in docs/getting-started.md; clarify certificate-change upgrade consequences. | DF-03 | [E01](#evidence-e01); [E02](#evidence-e02) |
| [`L98–L110`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/getting-started.md#L98-L110) — Next Steps | Choose next adopter workflow | Retain current Further reading links and extend README.md reading paths. | DF-13 | Current getting-started.md Further reading. |
| [`L111–L138`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/getting-started.md#L111-L138) — Common Issues | Diagnose first-run, Firebase and release failures | Replace generic clean/rebuild advice with docs/troubleshooting.md diagnoses and exact setup links. | DF-10 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L139–L173`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/getting-started.md#L139-L173) — IDE Setup; Further Reading | Use previews and project tooling | Rework current IDE Setup in docs/getting-started.md with compatible Studio guidance; link docs/components.md previews and docs/build-and-tooling.md run configs. | DF-03 | [E01](#evidence-e01); [E08](#evidence-e08) |

#### `docs/troubleshooting.md` — shortened page

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L94`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1-L94) — Build Errors: Gradle sync (JDK, repositories, catalog) | Diagnose build setup and missing aliases | Rework in docs/troubleshooting.md; restore catalog diagnostics, verify Gradle JDK and repository/proxy issue before cache refresh. | DF-10 | [E01](#evidence-e01) |
| [`L95–L157`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L95-L157) — KSP/Kapt Errors (Hilt; Room) | Diagnose generated-code failures | Restore differentiated Hilt binding and Room schema/query diagnostics in docs/troubleshooting.md. | DF-10 | [E04](#evidence-e04); [E05](#evidence-e05) |
| [`L158–L213`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L158-L213) — Dependency Resolution (duplicate classes; configuration cache) | Diagnose class conflicts and cache reports | Rework docs/troubleshooting.md using dependency reports and actual cache failure details; remove blanket BOM/nothing-to-do diagnoses. | DF-10 | [E01](#evidence-e01) |
| [`L214–L282`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L214-L282) — Runtime Errors: startup Firebase and Hilt failures | Diagnose app initialization | Retain two current entries in docs/troubleshooting.md with actual App/MainActivity/config locations. | DF-10 | [E07](#evidence-e07); [E11](#evidence-e11) |
| [`L283–L507`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L283-L507) — Navigation Errors (destination, serialization, backstack, nesting, arguments) | Diagnose registration, args and restoration | Rework as Navigation 3 symptoms in docs/troubleshooting.md; preserve serialization/backstack needs, discard obsolete nested-graph fixes. | DF-10 | [E07](#evidence-e07) |
| [`L508–L655`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L508-L655) — State Management Issues (updates, events, loading, helper compilation) | Diagnose silent state updates and stuck operations | Rework docs/troubleshooting.md using Result/Flow/lifecycle contracts; restore multiple-loading diagnosis; correct collectAsState and event-replay claims. | DF-10 | [E06](#evidence-e06) |
| [`L656–L796`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L656-L796) — Lifecycle Issues (recomposition, ViewModel lifetime, snackbar, rotation) | Distinguish composition, destination and process lifetimes | Restore in docs/troubleshooting.md with docs/state-management.md link; no navigation-before-error recipe or universal cancellation guarantee. | DF-10 | [E06](#evidence-e06); [E07](#evidence-e07); [E09](#evidence-e09) |
| [`L797–L854`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L797-L854) — Firebase auth (Google failure; Credential Manager) | Diagnose credentials and provider configuration | Rework docs/troubleshooting.md with exact docs/firebase.md setup links and verified SDK/provider behavior. | DF-10 | [E11](#evidence-e11) |
| [`L855–L886`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L855-L886) — Firestore Permission Denied | Diagnose ownership/path/rule rejection | Retain current docs/firebase.md rule link in docs/troubleshooting.md; remove broadly permissive rules as a proposed fix. | DF-10 | [E11](#evidence-e11) |
| [`L887–L924`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L887-L924) — Firebase Analytics Not Tracking | Understand absent event instrumentation | Replace with scope explanation in docs/firebase.md: no AnalyticsLogger/event logging API exists here. Do not restore fabricated calls. | DF-03 | [E11](#evidence-e11) |
| [`L925–L1004`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L925-L1004) — Crashlytics Not Reporting; duplicate Firebase initialization | Verify reporting and backend setup | Rework current reporting entry in docs/troubleshooting.md with setup/collection/build checks; merge duplicate initialization, no timing-only diagnosis. | DF-10 | [E11](#evidence-e11) |
| [`L1005–L1111`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1005-L1111) — Compose Recomposition Issues (not recomposing; excessive) | Diagnose state observation and UI work | Merge repeated symptoms into docs/troubleshooting.md; do not recommend removing visible UI data or indiscriminate derivedStateOf. | DF-10 | [E06](#evidence-e06); [E08](#evidence-e08) |
| [`L1112–L1254`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1112-L1254) — Compose Preview Issues (rendering; theme; fake data) | Diagnose previews correctly | Rework docs/troubleshooting.md: theme, dependencies and preview-safe content; ordinary Preview and visibility are not established failure causes. | DF-10 | [E08](#evidence-e08); [E07](#evidence-e07) |
| [`L1255–L1364`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1255-L1364) — Compose Performance Issues (list lag; jank) | Measure and reduce slow rendering | Merge repeated list/performance entries in docs/troubleshooting.md; link docs/components.md and build profiling; avoid universal animation fixes. | DF-10 | [E08](#evidence-e08); [E10](#evidence-e10) |
| [`L1365–L1459`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1365-L1459) — Compose State Issues (remember; LaunchedEffect) | Diagnose state reset and effect restarts | Restore docs/troubleshooting.md guidance on composition identity, key changes, saveable state and lifecycle; remove once-per-app claims. | DF-10 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L1460–L1563`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1460-L1563) — Code Quality Issues (headers; ktlint; CI) | Resolve formatting failures | Retain consolidated docs/troubleshooting.md entry with exact Spotless invocation and template locations. | DF-10 | [E01](#evidence-e01) |
| [`L1564–L1678`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1564-L1678) — Development Environment (preview; build/KSP speed; emulator install) | Diagnose tool and device setup | Restore distinct build/processor/install symptoms in docs/troubleshooting.md; merge duplicate previews; verify device-selection commands and avoid default uninstall/data loss. | DF-10 | [E01](#evidence-e01); [E08](#evidence-e08) |
| [`L1679–L1746`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1679-L1746) — Build Configuration (keystore; R8) | Diagnose signing and release-only problems | Retain entries in docs/troubleshooting.md with correct signing path and focused release diagnosis; remove blanket Firebase keep rules. | DF-10 | [E01](#evidence-e01); [E02](#evidence-e02) |
| [`L1747–L1805`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1747-L1805) — Data Layer (repository errors; Room migration) | Distinguish error boundaries and schema failures | Rework docs/troubleshooting.md with Flow/Result failures and version vs integrity distinction; destructive fallback is not a universal fix. | DF-10 | [E05](#evidence-e05); [E06](#evidence-e06); [E12](#evidence-e12) |
| [`L1806–L1847`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1806-L1847) — WorkManager Sync Issues | Diagnose scheduling and worker failures | Retain and correct current Sync entries in docs/troubleshooting.md; use actual request trigger, constraints, notification and retry path. | DF-10 | [E13](#evidence-e13) |
| [`L1848–L2006`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L1848-L2006) — Memory Issues (leaks; OOM; lists; recomposition) | Investigate memory and repeated UI work | Retain leak/OOM entries; merge duplicated list/recomposition advice; verify loader use and memory allocation before prescribing wrapper substitution. | DF-10 | [E08](#evidence-e08); [E11](#evidence-e11) |
| [`L2007–L2029`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L2007-L2029) — Testing Issues | Run tests and identify setup failures | Replace obsolete no-tests claim with docs/build-and-tooling.md commands and docs/guide.md worked test. | DF-04 | [E10](#evidence-e10) |
| [`L2030–L2071`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/docs/troubleshooting.md#L2030-L2071) — Getting Additional Help | Gather diagnostics and report a reproducible issue | Retain help links/logging in docs/troubleshooting.md; remove universal clean/build/invalidate-caches advice. | DF-10 | [E01](#evidence-e01); [E03](#evidence-e03) |

#### `app/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L28`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/app/README.md#L1-L28) — Purpose; Dependencies Graph; Key Features | Understand application assembly | Retain purpose in app/README.md; replace dependency graph with docs/architecture.md actual map; correct analytics scope. | DF-11 | [E01](#evidence-e01); [E07](#evidence-e07); [E11](#evidence-e11) |
| [`L29–L132`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/app/README.md#L29-L132) — MainActivity: lifecycle, theme, navigation, system UI, DI | Understand splash, permissions, theme and app host | Retain actual responsibilities and edge-to-edge example in app/README.md; restore useful lifecycle context and link docs/components.md locale/permission instructions. | DF-11 | [E07](#evidence-e07); [E12](#evidence-e12) |
| [`L133–L151`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/app/README.md#L133-L151) — Version Management; Related Documentation | Set app version and find integration details | Retain version formula in app/README.md; put release preparation in docs/build-and-tooling.md and fix graph/API links. | DF-11 | [E01](#evidence-e01); [E03](#evidence-e03) |

#### `core/android/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L19`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/android/README.md#L1-L19) — Overview; Key Concepts: coroutine utilities | Use cancellation-aware error and timeout helpers | Restore suspendCoroutineWithTimeout overloads/units in core/android/README.md alongside retained suspendRunCatching. | DF-11 | [E09](#evidence-e09) |
| [`L20–L25`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/android/README.md#L20-L25) — State Management Primitives | Know which primitives exist | Retain OneTimeEvent; remove Resource/networkBoundResource: neither exists in current production sources. Link docs/state-management.md. | DF-11 | [E09](#evidence-e09) |
| [`L26–L56`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/android/README.md#L26-L56) — Extension Functions; DI; When to Use | Discover validation, formatting, context and error utilities | Restore useful API/usage guidance in core/android/README.md with dispatcher qualifiers; distinguish actual functions from generic categories. | DF-11 | [E09](#evidence-e09) |
| [`L57–L106`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/android/README.md#L57-L106) — Common Patterns (offline repository; event; dispatcher) | Apply utilities in a consumer | Replace nonexistent offline helper example with docs/data.md and actual utility examples in core/android/README.md. | DF-11 | [E05](#evidence-e05); [E09](#evidence-e09) |
| [`L107–L153`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/android/README.md#L107-L153) — Dependencies Graph; API Documentation; Related Documentation; Usage | Add module dependency and reach API reference | Restore consumer snippet in core/android/README.md; link docs/architecture.md, remove false all-api claim and repair Dokka paths. | DF-11 | [E01](#evidence-e01); [E03](#evidence-e03) |

#### `core/network/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L50`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/network/README.md#L1-L50) — Overview; Key Concepts; When to Use | Understand REST/connectivity/image infrastructure | Retain current API inventory in core/network/README.md; qualify throwing data-source contract and remove blanket REST/Firestore mixing ban. | DF-11 | [E11](#evidence-e11) |
| [`L51–L120`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/network/README.md#L51-L120) — Common Patterns: data source; repository; monitoring; API | Add a REST endpoint and observe connectivity | Restore compact current NetworkDataSource/JetpackRestApi usage in core/network/README.md; replace nonexistent isNetworkAvailable with getCurrentState. Distinguish declared LOSING from emissions. | DF-11 | [E11](#evidence-e11) |
| [`L121–L160`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/network/README.md#L121-L160) — Dependencies Graph; Secret Management | Configure endpoint and dependencies | Replace invented API_KEY/API_ENDPOINT with BACKEND_URL in core/network/README.md; link docs/build-and-tooling.md and docs/architecture.md. | DF-11 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L161–L206`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/network/README.md#L161-L206) — API Documentation; Related Documentation; Error Handling; Best Practices; Usage | Consume REST APIs and handle errors | Restore consumer context in core/network/README.md; preserve throwing-source→Result boundary, remove Resource helper and repair API links. | DF-11 | [E03](#evidence-e03); [E09](#evidence-e09); [E11](#evidence-e11) |

#### `core/preferences/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L51`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/preferences/README.md#L1-L51) — Overview; Key Concepts; When to Use | Choose typed preferences storage | Rework core/preferences/README.md: JSON-backed DataStore, actual fields and Flow/updateData; remove Proto-style/no-runtime-exceptions and token-storage claims. | DF-11 | [E12](#evidence-e12) |
| [`L52–L172`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/preferences/README.md#L52-L172) — Common Patterns (data class; source; repository; ViewModel; auth state) | Add/observe a preference and clear user state | Restore actual UserDataPreferences/updateData/reset examples in docs/data.md, summarized in core/preferences/README.md; remove fictional authToken contract. | DF-06 | [E12](#evidence-e12); [E05](#evidence-e05) |
| [`L173–L232`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/preferences/README.md#L173-L232) — Dependencies Graph; DataStore Setup (provider; serializer) | Construct and evolve DataStore | Restore DatastoreModule and UserDataSerializer explanation in docs/data.md; describe CorruptionException and absent corruption handler. | DF-06 | [E04](#evidence-e04); [E12](#evidence-e12) |
| [`L233–L272`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/preferences/README.md#L233-L272) — API Documentation; Related Documentation; DataStore vs SharedPreferences; Best Practices | Find API and choose persistence | Rework core/preferences/README.md links and usage advice; avoid obsolete alternative-storage recommendation and default-on-corruption assertion. | DF-11 | [E03](#evidence-e03); [E12](#evidence-e12) |
| [`L273–L292`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/preferences/README.md#L273-L292) — ProGuard Rules; Usage | Consume module and find serialization rules | Replace copied keep rules with verified rule-file reference in core/preferences/README.md and usage snippet; necessity claims remain DF-12 review. | DF-11 | [E01](#evidence-e01) |

#### `core/room/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L51`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/room/README.md#L1-L51) — Overview; Key Concepts; When to Use | Understand local persistence responsibilities | Retain purpose in core/room/README.md; correct SyncAction inventory and broad metadata guarantees. | DF-11 | [E05](#evidence-e05) |
| [`L52–L168`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/room/README.md#L52-L168) — Common Patterns (entity, data source, DAO, repository) | Add local storage end to end | Restore current DAO/database registration and mapping example in docs/data.md/docs/guide.md; README links to repaired walkthrough. | DF-06 | [E04](#evidence-e04); [E05](#evidence-e05) |
| [`L169–L226`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/room/README.md#L169-L226) — Dependencies Graph; Sync Metadata (enum, timestamps, soft delete) | Interpret pending rows and deletion | Rework core/room/README.md with UPSERT, timestamp query roles and retained local tombstones; no automatic post-push cleanup. | DF-11 | [E05](#evidence-e05); [E13](#evidence-e13) |
| [`L227–L250`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/room/README.md#L227-L250) — Database Migrations | Change schema without hiding data loss | Restore version/migration registration and tests in docs/data.md; expose destructive fallback and DF-02 upgrade decision. | DF-06 | [E05](#evidence-e05); [E10](#evidence-e10) |
| [`L251–L286`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/room/README.md#L251-L286) — API Documentation; Related Documentation; Best Practices; Usage | Find and consume Room APIs | Repair core/room/README.md API links; retain consumer guidance and qualify query/user/deleted filters by actual DAO method. | DF-11 | [E01](#evidence-e01); [E03](#evidence-e03); [E05](#evidence-e05) |

#### `core/ui/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L58`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/ui/README.md#L1-L58) — Overview; Key Concepts; When to Use | Discover state, UI and theme APIs | Retain catalog in core/ui/README.md; remove nonexistent LiveData-observation extension inventory and correct getActivity location. | DF-11 | [E08](#evidence-e08); [E09](#evidence-e09) |
| [`L59–L121`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/ui/README.md#L59-L121) — Screen with State Management | Connect ViewModel state to UI | Replace Route wrapper example with docs/guide.md Screen pattern and docs/state-management.md contracts. | DF-11 | [E06](#evidence-e06); [E07](#evidence-e07) |
| [`L122–L145`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/ui/README.md#L122-L145) — Type-Safe Text Handling | Resolve dynamic/localized text at UI boundary | Restore UiText example in docs/components.md, linked from core/ui/README.md; show actual resource args/asString overloads. | DF-09 | [E09](#evidence-e09) |
| [`L146–L165`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/ui/README.md#L146-L165) — Permission Handling | Request permissions through existing helpers | Restore actual ComponentActivity helper usage in docs/components.md; signatures are onSuccess/onFailure, not onGranted/onDenied; explain already-granted path. | DF-09 | [E09](#evidence-e09); [E12](#evidence-e12) |
| [`L166–L203`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/ui/README.md#L166-L203) — Dependencies Graph; API Documentation; Related Documentation | Find dependencies and API entry points | Rework core/ui/README.md links; include core:navigation exposure and lint publication in docs/architecture.md. | DF-11 | [E01](#evidence-e01); [E03](#evidence-e03) |
| [`L204–L239`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/core/ui/README.md#L204-L239) — Philosophy (separation, errors, testability); Usage | Use the module consistently | Replace stale Route split with actual Screen/subcomponent pattern in core/ui/README.md; qualify error delivery and api exposure. | DF-11 | [E06](#evidence-e06); [E07](#evidence-e07); [E01](#evidence-e01) |

#### `data/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L53`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L1-L53) — Overview; Key Concepts; When to Use | Understand repository responsibilities | Rework data/README.md overview around actual Auth/Home/Profile/Settings repositories; Room-first is home-specific. | DF-11 | [E05](#evidence-e05); [E11](#evidence-e11); [E12](#evidence-e12) |
| [`L54–L123`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L54-L123) — Common Patterns: repository interface and implementation | Implement repository contracts | Replace hypothetical users code with docs/data.md current Result/Flow and local-write pattern. | DF-06 | [E05](#evidence-e05) |
| [`L124–L151`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L124-L151) — Using networkBoundResource Helper | Implement offline-first coordination | Remove nonexistent Resource/networkBoundResource example; docs/data.md describes actual sync. | DF-06 | [E05](#evidence-e05) |
| [`L152–L185`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L152-L185) — Data Transformation (Mappers) | Convert between layer models | Restore explicit mapping guidance in docs/data.md and docs/guide.md, checking all sync metadata. | DF-06 | [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L186–L236`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L186-L236) — Dependencies Graph; Hilt Module Setup; API/Related Documentation | Understand and bind repositories | Rework data/README.md with actual graph link, RepositoryModule and maintained guide/API links. | DF-11 | [E01](#evidence-e01); [E03](#evidence-e03); [E04](#evidence-e04) |
| [`L237–L286`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L237-L286) — Repository Patterns (network-only, local-only, manual/automatic sync) | Choose repository behavior | Restore options in docs/data.md using real examples or labeled extensions; remove automatic helper reference. | DF-06 | [E05](#evidence-e05); [E11](#evidence-e11); [E12](#evidence-e12) |
| [`L287–L369`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L287-L369) — Error Handling: repository, ViewModel, UI | Propagate failures through state | Rework docs/data.md and docs/state-management.md; remove caught-exception and built-in Retry action claims. | DF-06 | [E06](#evidence-e06); [E09](#evidence-e09) |
| [`L370–L441`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L370-L441) — Network-Specific Error Handling; HTTP codes; Error Flow Diagram | Diagnose network failure at the right boundary | Restore bounded exception guidance in docs/data.md; remove automatic sign-out/retry recommendations and nonexistent network methods. | DF-06 | [E09](#evidence-e09); [E11](#evidence-e11) |
| [`L442–L485`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/data/README.md#L442-L485) — Best Practices; Philosophy; Usage | Keep layering and consume repositories | Retain current data/README.md entry points; link docs/architecture.md extension criteria and correct Syncable implementation/local-write assurances. | DF-11 | [E01](#evidence-e01); [E05](#evidence-e05); [E13](#evidence-e13) |

#### `sync/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L39`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L1-L39) — Purpose; Features; Dependencies Graph; Usage | Understand scheduling responsibilities | Rework sync/README.md: SyncManagerImpl schedules; HomeRepositoryImpl implements Syncable in data; no periodic scheduling. | DF-11 | [E01](#evidence-e01); [E05](#evidence-e05); [E13](#evidence-e13) |
| [`L40–L80`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L40-L80) — Setting Up Sync | Wire a syncable repository and worker | Restore actual setup in docs/data.md, linking from sync/README.md; collect Flow and explain delegating worker. | DF-06 | [E04](#evidence-e04); [E13](#evidence-e13) |
| [`L81–L101`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L81-L101) — Work Constraints; Progress Tracking | Observe execution and progress | Restore docs/data.md guidance: CONNECTED only; isSyncing is RUNNING status; current progress goes to foreground notification, not setProgress. | DF-06 | [E13](#evidence-e13) |
| [`L102–L190`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L102-L190) — Troubleshooting: not running; repeated failure | Diagnose queue and retry behavior | Rework docs/troubleshooting.md Sync entries; no App.initialize requirement or inference that a log proves running work. | DF-10 | [E13](#evidence-e13) |
| [`L191–L224`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L191-L224) — Sync Notification Not Showing | Diagnose foreground notification visibility | Restore docs/troubleshooting.md with actual app permission and merged service setup; verify API-specific requirements. | DF-10 | [E12](#evidence-e12); [E13](#evidence-e13) |
| [`L225–L242`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L225-L242) — WorkManager Constraints Not Respected | Customize scheduling intentionally | Rework docs/data.md with current constraints; label stricter constraints as extensions, avoid equating unmetered with Wi-Fi. | DF-06 | [E13](#evidence-e13) |
| [`L243–L286`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L243-L286) — Sync Conflict Resolution | Understand overwritten edits and deletion propagation | Replace client-clock merge recipe with DF-02 outcome in docs/data.md; remote delete() and incremental pulls require explicit deletion-delivery assessment. | DF-06 | [E05](#evidence-e05); [E11](#evidence-e11) |
| [`L287–L332`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/sync/README.md#L287-L332) — Debugging Sync Issues; Related Documentation; Implementation Reference | Inspect worker execution | Rework docs/troubleshooting.md diagnostics and sync/README.md links; verify ADB/package names, do not assume device sqlite3. | DF-10 | [E01](#evidence-e01); [E13](#evidence-e13) |

#### `feature/auth/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L37`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/auth/README.md#L1-L37) — Purpose; Features; Dependencies Graph; Usage | Understand auth feature boundaries | Retain feature/auth/README.md overview; make AuthRepository boundary explicit and link actual dependency map. | DF-11 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L38–L67`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/auth/README.md#L38-L67) — Key Components; Related Documentation | Integrate sign-in/sign-up screens | Replace old Route signatures with Screen and entry registration in feature/auth/README.md; retain state/navigation links. | DF-11 | [E07](#evidence-e07) |

#### `feature/home/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L28`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/home/README.md#L1-L28) — Purpose; Features; Dependencies Graph | Understand list/edit/delete behavior | Rework feature/home/README.md: retain offline/local edits and sync; remove unsupported undo and pull-to-refresh. | DF-11 | [E05](#evidence-e05); [E07](#evidence-e07) |
| [`L29–L56`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/home/README.md#L29-L56) — Key Components; Related Documentation | Use list/item destinations | Retain current Item assisted-factory snippet in feature/home/README.md; qualify failure behavior with DF-02 outcome and link complete guide. | DF-11 | [E06](#evidence-e06); [E07](#evidence-e07) |

#### `feature/profile/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L27`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/profile/README.md#L1-L27) — Purpose; Features; Dependencies Graph | Understand profile capabilities | Retain corrected read-only profile/sign-out overview in feature/profile/README.md; leave removed avatar management and user-stats promises removed. | DF-11 | [E05](#evidence-e05); [E07](#evidence-e07) |
| [`L28–L44`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/profile/README.md#L28-L44) — Key Components; Related Documentation | Integrate profile screen | Retain Screen signature and docs/navigation.md link; clarify ProfileRepository boundary in feature/profile/README.md. | DF-11 | [E05](#evidence-e05); [E07](#evidence-e07) |

#### `feature/settings/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L38`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/settings/README.md#L1-L38) — Purpose; Features; Dependencies Graph; Usage | Understand settings dependencies | Retain theme/language/sign-out/licenses coverage in feature/settings/README.md with actual SettingsRepository boundary. | DF-11 | [E01](#evidence-e01); [E12](#evidence-e12) |
| [`L39–L79`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/feature/settings/README.md#L39-L79) — Settings Dialog; Key Features; Related Documentation | Customize theme, locale, links and sign-out | Retain SettingsDialog signature in feature/settings/README.md; replace fictional ThemeSettings/ProfileSettings APIs and link docs/components.md full locale extension path. | DF-11 | [E12](#evidence-e12) |

#### `firebase/analytics/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L33`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/analytics/README.md#L1-L33) — Purpose; Features; Dependencies Graph; Usage | Identify crash-reporting role | Retain Crashlytics/event-analytics distinction and module use in firebase/analytics/README.md; link actual graph. | DF-11 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L34–L58`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/analytics/README.md#L34-L58) — Error Reporting; Setup; Related Documentation | Report an exception and verify collection | Retain reportException example in firebase/analytics/README.md; docs/firebase.md owns setup/mapping-upload verification. Distinguish optional UI report action from uncaught crashes. | DF-11 | [E06](#evidence-e06); [E11](#evidence-e11) |

#### `firebase/auth/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L41`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/auth/README.md#L1-L41) — Purpose; Features; Dependencies Graph; Usage | Understand auth wrapper and dependencies | Retain scope in firebase/auth/README.md; update graph/consumer context without promising an independent token-management API. | DF-11 | [E01](#evidence-e01); [E11](#evidence-e11) |
| [`L42–L65`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/auth/README.md#L42-L65) — Authentication Operations; Credential Management | Choose auth methods and saved credentials | Rework firebase/auth/README.md with actual interface and request builders; verify filter vs account creation semantics under DF-03. | DF-11 | [E11](#evidence-e11) |
| [`L66–L74`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/auth/README.md#L66-L74) — Setup; Related Documentation | Configure Firebase auth | Retain docs/firebase.md link in firebase/auth/README.md; setup must include web OAuth client ID. | DF-11 | [E11](#evidence-e11); [E02](#evidence-e02) |

#### `firebase/firestore/README.md` — previous README

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L31`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/firestore/README.md#L1-L31) — Purpose; Features; Dependencies Graph; Usage | Understand remote sync interface | Retain FirebaseDataSource reference in firebase/firestore/README.md; remove unsupported batch/listener coverage and broad security assurances. | DF-11 | [E11](#evidence-e11) |
| [`L32–L55`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/firestore/README.md#L32-L55) — Data Operations | Read/write remote records | Replace fictional FirestoreDataSource/getData Flow with current pull/set/delete and timestamp behavior in firebase/firestore/README.md. | DF-11 | [E11](#evidence-e11) |
| [`L56–L85`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/1c2103890d16f81f6c6ae4c2d8626281b841e417/firebase/firestore/README.md#L56-L85) — Security Rules; Setup; Related Documentation | Deploy rules matching model and ownership | Replace permissive duplicate rule with docs/firebase.md reference; clarify rules are manually deployed and compatibility is pending DF-02. | DF-11 | [E11](#evidence-e11) |

#### `core/navigation/README.md` — new README accuracy review

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L25`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/core/navigation/README.md#L1-L25) — Purpose; Key APIs | Use multi-stack state and Navigator | Retain current API table/snippet in core/navigation/README.md; describe Navigator as the intended mutation API, not enforced encapsulation of public stacks. | DF-11 | [E07](#evidence-e07) |
| [`L26–L35`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/core/navigation/README.md#L26-L35) — Gotchas; Related Documentation | Avoid identity collisions and integrate entries | Rework core/navigation/README.md key-type claim: instances with differing arguments are not equal merely because they share a type. Verify upstream contentKey collision behavior under DF-08. | DF-11 | [E07](#evidence-e07) |

#### `core/testing/README.md` — new README accuracy review

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L25`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/core/testing/README.md#L1-L25) — Purpose; Key APIs; example | Add test helpers and write assertions | Retain MainDispatcherRule and fakes in core/testing/README.md; replace empty test with worked success/failure example and testImplementation setup. Correct FakeAuthDataSource description: it stores a nullable user, not a MutableStateFlow, and sign-in methods throw unsupported. | DF-11 | [E10](#evidence-e10) |
| [`L26–L37`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/core/testing/README.md#L26-L37) — Gotchas; Related Documentation | Know fake limitations and find working tests | Remove mock impossibility claim and stale consumer list; repair nonexistent state-management Testing link and distinguish included test libraries from optional shared fakes. | DF-11 | [E01](#evidence-e01); [E10](#evidence-e10); [E03](#evidence-e03) |

#### `benchmarks/README.md` — new README accuracy review

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L25`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/benchmarks/README.md#L1-L25) — Purpose; Key APIs; generator excerpt | Generate profile and compare startup modes | Retain actual generator/benchmark entry points in benchmarks/README.md; describe measured comparison, not guaranteed improvement or CI execution. | DF-11 | [E10](#evidence-e10); [E02](#evidence-e02) |
| [`L26–L35`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/benchmarks/README.md#L26-L35) — Gotchas; Related Documentation | Regenerate and run benchmark tasks | Retain commands subject to task/API validation in DF-04; replace invented first-hour adopter rationale with actual startup-only interaction sequence. | DF-11 | [E10](#evidence-e10) |

#### `lint/README.md` — new README accuracy review

| Source range and section | Reader need | Disposition and destination | Owner | Evidence |
|---|---|---|---|---|
| [`L1–L21`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/lint/README.md#L1-L21) — Purpose; Key APIs; registry excerpt | Understand published custom lint checks | Retain registry, severities, package resolution and core:ui publication in lint/README.md; link component extension/testing workflow. | DF-11 | [E08](#evidence-e08); [E10](#evidence-e10); [E01](#evidence-e01) |
| [`L22–L30`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/0fb7882982cfefd55df300e06549731c6fbb33bb/lint/README.md#L22-L30) — Gotchas; Related Documentation | Name tests and extend rules | Replace historical naming-rule comparison with actual test-prefix behavior; retain docs/components.md link and add :lint:test usage in lint/README.md. | DF-11 | [E10](#evidence-e10) |

### Evidence catalogue

#### Evidence E01

[Build wiring](settings.gradle.kts), [version catalog](gradle/libs.versions.toml), [application build](app/build.gradle.kts), [Compose convention](build-logic/convention/src/main/kotlin/dev/atick/AndroidCompose.kt), and [Renovate configuration](.github/renovate.json).

#### Evidence E02

[CI workflow](.github/workflows/ci.yml), [release workflow](.github/workflows/cd.yml), [Fastlane configuration](fastlane/Appfile), and [Fastlane lanes](fastlane/Fastfile).

#### Evidence E03

[Docs workflow](.github/workflows/docs.yml), [MkDocs configuration](mkdocs.yml), [root README](README.md), and current documentation links.

#### Evidence E04

[Room DI](core/room/src/main/kotlin/dev/atick/core/room/di/DatabaseModule.kt), [network DI](core/network/src/main/kotlin/dev/atick/core/network/di/retrofit/RetrofitModule.kt), [dispatcher DI](core/android/src/main/kotlin/dev/atick/core/di/DispatcherModule.kt), and [repository DI](data/src/main/kotlin/dev/atick/data/di/RepositoryModule.kt).

#### Evidence E05

[HomeRepositoryImpl](data/src/main/kotlin/dev/atick/data/repository/home/HomeRepositoryImpl.kt), [JetpackDao](core/room/src/main/kotlin/dev/atick/core/room/data/JetpackDao.kt), and [JetpackDatabase](core/room/src/main/kotlin/dev/atick/core/room/data/JetpackDatabase.kt).

#### Evidence E06

[StatefulComposable and UiState helpers](core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt), [OneTimeEvent](core/android/src/main/kotlin/dev/atick/core/utils/OneTimeEvent.kt), and Home/Item ViewModels.

#### Evidence E07

[NavigationState](core/navigation/src/main/kotlin/dev/atick/core/navigation/NavigationState.kt), [Navigator](core/navigation/src/main/kotlin/dev/atick/core/navigation/Navigator.kt), [home navigation](feature/home/src/main/kotlin/dev/atick/feature/home/navigation/HomeNavigation.kt), and [JetpackApp](app/src/main/kotlin/dev/atick/compose/ui/JetpackApp.kt).

#### Evidence E08

[core UI components](core/ui/src/main/kotlin/dev/atick/core/ui/components), [theme](core/ui/src/main/kotlin/dev/atick/core/ui/theme), and [DesignSystemDetector](lint/src/main/kotlin/dev/atick/lint/designsystem/DesignSystemDetector.kt).

#### Evidence E09

[core Android utilities](core/android/src/main/kotlin/dev/atick/core/utils), [UiText](core/ui/src/main/kotlin/dev/atick/core/ui/utils/UiText.kt), and [activity extensions](core/ui/src/main/kotlin/dev/atick/core/ui/extensions/ActivityExtensions.kt).

#### Evidence E10

[testing helpers](core/testing/src/main/kotlin/dev/atick/core/testing), [navigation tests](core/navigation/src/test), [UI tests](core/ui/src/test), and [baseline profile generator](benchmarks/src/main/kotlin/dev/atick/benchmarks/BaselineProfileGenerator.kt).

#### Evidence E11

[Firestore data source](firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/data/FirebaseDataSourceImpl.kt), [Firebase model](firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/model/FirebaseJetpack.kt), [auth data source](firebase/auth/src/main/kotlin/dev/atick/firebase/auth/data/AuthDataSourceImpl.kt), and [network utility](core/network/src/main/kotlin/dev/atick/core/network/utils/NetworkUtilsImpl.kt).

#### Evidence E12

[DataStore module](core/preferences/src/main/kotlin/dev/atick/core/preferences/di/DatastoreModule.kt), [user serializer](core/preferences/src/main/kotlin/dev/atick/core/preferences/utils/UserDataSerializer.kt), [settings ViewModel](feature/settings/src/main/kotlin/dev/atick/feature/settings/ui/SettingsViewModel.kt), and [locale utilities](core/ui/src/main/kotlin/dev/atick/core/ui/utils/LocaleUtils.kt).

#### Evidence E13

[Sync scheduling](sync/src/main/kotlin/dev/atick/sync/manager/Sync.kt), [SyncManagerImpl](sync/src/main/kotlin/dev/atick/sync/manager/SyncManagerImpl.kt), [SyncWorker](sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt), and [sync utilities](data/src/main/kotlin/dev/atick/data/utils/SyncUtils.kt).

### DF-00 verification

- [x] All 41 scoped source files are present in the ledger, every range starts at line 1 or follows the preceding range, and each row has a future task owner.
- [x] The pinned revisions and the PR’s open `main` → `fix/docs-v3` relationship were checked before recording the audit.
- [x] `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache` passed (180 tasks up-to-date).
- [x] `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` passed (180 tasks up-to-date).
- [x] `./gradlew check` passed (965 tasks: 18 executed, 947 up-to-date).

No documentation page was rewritten during DF-00. DF-02 must settle the listed runtime and backend uncertainties before later tasks state guarantees about synchronization, failures, collectors, permissions, or migration safety.


## DF-01 — Replace the policy that produced the prose

Depends on: DF-00 scope captured. Scope: `CONTRIBUTING.md`; active guidance in root planning files.

- [x] Replace “comments explain why, not what” with the plan's guidance on behavior, constraints,
  contracts, and supported rationale. Allow lists and KDoc links where they improve readability.
- [x] Remove mandatory code-emoji/Markdown-alert parity and numeric alert/line budgets. Preserve
  GitHub-compatible alert syntax and concrete signing/data-loss notices where useful.
- [x] Add a short example of a helpful comment and a useful tutorial excerpt. Explain how to label
  omissions and hypothetical additions without pretending they are existing code.
- [x] Mark the old `PLAN.md` and `TASKS.md` as historical and point to these repair files. Preserve
  evidence in the old logs; remove their authority over new editorial work.
- [x] Apply the new standard to subsequent tasks: no invented motives, blame, self-congratulation,
  blanket bans on reasonable alternatives, or repeated historical explanations of fixed bugs.

Accept: contributor guidance evaluates usefulness and accuracy; it no longer rewards deleting
content to meet a quota or adding a justification to every unusual line.

### DF-01 completion record

Completed 2026-10-16 from clean working-tree revision `df5af917d`.

- Replaced `CONTRIBUTING.md`'s rationale-only comment policy, emoji/alert mapping, and alert limits
  with guidance on useful behavior, contracts, constraints, supported rationale, and task-oriented
  tutorials. Added a query-filter comment example and a source-checked `getJetpacks()` method
  excerpt with explicit omissions. Preserved GitHub alert syntax and a concrete signing warning.
- Added historical notices to `PLAN.md` and `TASKS.md`, pointing to the active repair records and
  contributor guidance. Their original findings, targets, and verification logs remain intact.
- Verified changed policy links and anchors, checked the tutorial excerpt against its source,
  and reviewed the Markdown and diff. `git diff --check` passed.
- `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` passed
  (180 tasks up-to-date). The first sandboxed attempt could not open Gradle's cache lock; the
  approved rerun passed. Spotless does not target Markdown, so editorial checks were separate.
- No runtime or public API changes. No commit was created; Spotless apply and `./gradlew check`
  remain required before committing. Runtime contract review remains DF-02; source comment
  rewrites and broader root-guidance reconciliation remain with their existing later owners.

## DF-02 — Settle contracts and separate runtime work

Depends on: DF-00. Scope: targeted inspection/tests and a local behavior-follow-up record.

- [x] Classify the PR's executable changes separately: swipe effect, initial connectivity emission,
  raw price state, unused serializer removal, no-op variant-block removal, and server-clock sync
  with Room schema/model/query changes. Classify the backoff correction as documentation-only.
- [x] For `HomeRepositoryImpl.sync()`, examine edits after the pending-row snapshot, edits to a row
  while its push is in flight, `markAsSynced(id)` clearing a newer edit, and a pull overwriting that
  edit. Examine whether a request dropped by `KEEP` is ever guaranteed another run. Do not call
  these paths safe merely because the first write was stored locally.
- [x] Review partial pull/retry behavior: rows are individually upserted while the cursor is their
  maximum server timestamp. Determine what happens if collection fails after storing only some
  returned documents. Check query ordering, equal timestamps, and any SDK cache assumptions before
  claiming complete incremental delivery.
- [x] Check the lifecycle of collectors launched from `onStart` in Home, Item, and Settings
  ViewModels. `launchIn(viewModelScope)` outlives the shared upstream; document or test repeated
  subscribe/unsubscribe cycles beyond the five-second timeout.
- [x] Review `StatefulComposable` event consumption during composition and effect cancellation/key
  behavior. Verify at-most-once consumption separately from successful snackbar delivery.
- [x] Review update-helper behavior for returned failures, thrown exceptions, cancellation,
  overlapping calls, and synchronous edits during a suspended update. The loading flag is checked
  before launch and set inside it; avoid describing it as a general concurrency lock.
- [x] Trace Item save/load failures: save discards the repository `Result`; load `catch` constructs
  an unused `UiState`. Record that the guide copies these paths, so code agreement alone does not
  make the example a sound error-handling tutorial.
- [x] Assess schema 1 → 2 for existing installs and pending offline changes. Assess Firestore
  documents without `serverUpdatedAt`, clients writing the old field set, and the updated rule
  requirements. The old task log's assumption that no adopter has old data is not evidence.
- [x] Record each issue as confirmed from source, reproduced by a targeted test, or still unverified.
  Give it a bounded outcome: separately scoped fix, accurate documented limitation with follow-up,
  or disproved concern with evidence. Do not silently expand the documentation repair into runtime
  or backend changes. New external issues/comments need user authorization.

Accept: DF-06–08 and DF-12 have clear contracts to document. No uncertainty has been converted into
a claim of safety. The PR's existing tests are distinguished from missing concurrency/upgrade tests.

### DF-02 completion record

Completed 2026-10-16 from clean working-tree revision `13031839ddb0b36eefbe068a5e44ccbce7749a28`.
The executable-change classification compares the pinned audit base `1c2103890` with audited head
`0fb788298`; the two subsequent commits changed documentation policy and recovery records only.
No production code, build configuration, backend rules, or public interface was changed by this task.

**Evidence levels:** “reproduced” means a passing characterization test demonstrates current
behavior, including bugs. “Source-confirmed” is a trace through the implementation and, where
needed, the SDK contract; it does not claim device/emulator execution. “Unverified” identifies the
remaining integration question. Existing tests and newly added tests are listed separately below.

### Executable changes already in the audited PR

| Change | Classification and evidence | Compatibility / disposition |
|---|---|---|
| Swipe dismissal | Runtime fix: moves `onDelete` from composition into `LaunchedEffect(currentValue)` and reads the latest callback through `rememberUpdatedState`. Existing `SwipeToDismissTest` covers a completed swipe, recomposition after dismissal, and no swipe. | One invocation for the tested dismissal while that composition survives; not a durable or global exactly-once guarantee. Preserve the fix; DF-09/12 explain the effect. |
| Initial connectivity | Runtime fix: seeds `currentNetworkState()` before registering callbacks and adds `distinctUntilChanged()`. Existing `NetworkUtilsImplTest` covers initial states and duplicate suppression. | Checks `NET_CAPABILITY_INTERNET`, not validation or backend reachability. Registration and the seed are not an atomic snapshot. Keep claims limited to the observed capability/callback state; DF-10/12. |
| Price editing | Runtime fix: raw `String` state and text-field value, parsing at submit with invalid/empty input becoming `0.0`. Seven pre-existing Item tests cover text preservation, parsing and loading. | No non-negative-price validation is implemented. Preserve text behavior; DF-07/08 must distinguish parsing from validation and successful persistence. |
| Unused serializer | Removal of public `DarkThemeConfigSerializer`; no in-repo references or registration. The active `UserDataSerializer` remains JSON and throws `CorruptionException` on serialization failure; DI installs no corruption handler (E12). | No current in-repo serialization path changes, but adopters directly using the removed symbol need to replace that reference. Removal does not add corruption recovery; DF-06/11/13. |
| Variant block | Build configuration removal: the old block copied the first output's version name to each output. Current single-version configuration has no custom filename behavior. | Does not implement APK renaming. Do not generalize to an adopter's customized per-output versions; DF-04/13. |
| Server-clock sync | Runtime/model/schema change: timestamp query replaces the device-clock query; Room version 1 → 2 adds the stored server timestamp; mappings and model change with it (E05/E11). `FirebaseJetpack` also loses Kotlin serialization's `@Serializable`; no in-repo generated-serializer consumer was found. | Backend/document compatibility and destructive upgrade are assessed below. Adopters using the generated Kotlin serializer also need an explicit replacement; DF-06/13. |
| Backoff correction | Documentation-only: the worker diff changes comments, not scheduling. No `setBackoffCriteria` is configured. [WorkManager defaults](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work) are exponential with a 30-second base. | `runAttemptCount < 3` permits three retries after the initial attempt, not three total attempts. Delays are minimum scheduling intervals, not exact execution times; DF-06/12. |

Other Kotlin/build diff hunks were inspected: theme/auth comments, helper/ViewModel comments,
Compose-provider and Spotless explanations are editorial. Test additions and their test-only
classpath declarations support the executable fixes; they are not application runtime dependencies.
The Firestore rules example changes deployment guidance only: editing Markdown deploys nothing.

### Behavior ledger

| Concern | Evidence/reproduction | Decision and follow-up | Documentation affected |
|---|---|---|---|
| Writes after the pending snapshot; dropped requests | **Reproduced:** `a row added after the push snapshot waits for another sync` leaves that row pending after a successful pass. Source: E05/E13 take one snapshot, enqueue one-time work with `KEEP`, and configure no periodic drain. [KEEP](https://developer.android.com/reference/androidx/work/ExistingWorkPolicy) does nothing when matching unfinished work exists. | Document that a later trigger is required and not guaranteed by the dropped request. Follow-up **Sync consistency** must cover scheduling a subsequent pass. The fake records requests; it does not test WorkManager scheduling. | DF-06, DF-08, DF-11, DF-12 |
| Acknowledging a newer edit | **Reproduced:** suspending an upload, editing the same row, then finishing upload clears the newer edit's pending flag/action. `markAsSynced(id)` has no version predicate and stamps a later local time. | Document possible loss of pending status, not guaranteed eventual upload. Follow-up **Sync consistency** must acknowledge only the version actually uploaded. | DF-06, DF-12 |
| Pull overwrites newer local content | **Reproduced:** the old upload echo overwrites the edit made while upload was suspended. Pull uses unconditional per-row upserts; the same risk applies to edits made during pull. | Remove protection claims based solely on push-before-pull. Follow-up **Sync consistency** must protect pending edits during both phases and test delete/edit races. | DF-06–08, DF-11, DF-12 |
| Partial pull, distinct timestamps | **Reproduced:** fail the second local upsert; the first stays committed and advances MAX(timestamp). With ascending distinct timestamps, retry resumes the remaining row. SDK inequality queries imply ordering by that field ([query ordering](https://firebase.google.com/docs/firestore/query-data/order-limit-data#orderby_and_existence)); ties use document path ([index ordering](https://firebase.google.com/docs/firestore/query-data/index-overview#default_ordering_and_the_name_field)). | The broad concern that an ordinary ordered prefix necessarily skips all later distinct timestamps is disproved under a complete server result. This is not atomic batch application. Keep the narrower tie/cache limitations below. | DF-06, DF-12 |
| Partial pull, equal timestamps | **Reproduced:** after storing one of two equal-timestamp documents and failing the next upsert, retry's strict `>` excludes the unapplied document. A stable SDK tie order does not help a timestamp-only cursor. | Document incomplete delivery after interruption within a tie group. Follow-up **Pull checkpoints** must make checkpoint advancement cover all records at its boundary, with failure/cancellation tests. | DF-06, DF-12 |
| SDK cache completeness | **Source-confirmed:** `.get()` uses the SDK default source, which can return cached data when the server is unreachable ([Android Query contract](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/Query#get())). Mapping to a list discards snapshot metadata. A network constraint does not guarantee server access. **Unverified:** exact cache-gap/eviction sequences with the project's resolved SDK. | No guarantee that every successful pull is a complete server snapshot. Follow-up **Pull checkpoints** must include cached partial results and an emulator/device reproduction before claiming complete delivery. | DF-06, DF-10, DF-12 |
| Delete propagation | **Source-confirmed:** local soft-delete becomes remote `.delete()`, then local acknowledgement. No remote tombstone or deletion feed is pulled; another device's existing row is not removed by an absent document. | Document that hard deletes are not delivered to other replicas through this incremental query. Follow-up **Sync consistency** must define and test cross-device deletion delivery separately from local hiding. | DF-06, DF-11, DF-12 |
| Local/remote metadata | **Source-confirmed:** DAO pending query uses `lastUpdated > lastSynced OR needsSync`; mappings carry `lastSynced` to/from Firestore. Pulled entities default to `needsSync=false`, action `NONE`. | Neither timestamp is merely display-only, and `lastSynced` is not local-only. Do not promise a pushed row echoes exactly once or always has unchanged content. DF-06 owns the field table and algorithm; no metadata changes here. | DF-06, DF-11, DF-12 |
| Collector lifetimes | **Reproduced:** Home and Item retain a repository collector after a gap exceeding 5 seconds, then add another on resubscription. Settings does the same after its zero timeout. All return to zero when the ViewModel scope is cancelled. `onStart` launches a separate `viewModelScope` job; stopping the shared upstream does not stop it. | Document accumulating collectors while that ViewModel remains alive. Follow-up **Observation lifetime** must keep one repository subscription per ViewModel/shared upstream and retain correct locale refresh. Profile has the same source pattern and belongs in that follow-up's audit. | DF-07/08, DF-11, DF-12 |
| Returned failures and synchronous updates | Existing helper tests plus source E06: `updateState` replaces data while preserving loading/error; async helpers inspect returned `Result`, clear the old error on start, and clear loading on returned success/failure. | Correct the table saying synchronous updates do not replace data. These are Result-handling helpers, not exception-catching wrappers. DF-07 documents the contract; no runtime fix needed for these branches. | DF-07, DF-12 |
| Exceptions/cancellation | **Reproduced for both helpers:** thrown exception escapes to a supervised scope's handler with no error event, leaving loading true; thrown `CancellationException` likewise leaves loading true and is not reported to the exception handler. No `try/finally` exists. E13 also catches `Exception` in `SyncWorker`, including cancellation. | Document the actual cleanup boundary. Follow-up **Async state lifecycle** must define exception and cancellation cleanup without swallowing cancellation; include worker cancellation review. Passing Result tests do not cover these paths. | DF-06/07, DF-12 |
| Overlap before loading | **Reproduced for both helpers:** a queued dispatcher permits two calls before either coroutine sets loading; both operations start. Existing eager-dispatcher tests only prove a second call is dropped once loading is already true. | No general concurrency-lock claim. Follow-up **Async state lifecycle** must test queued launch, overlap, and loading while another operation is still active. | DF-07, DF-12 |
| Edits during suspended state update | **Reproduced:** `updateStateWith` returns a copy of its receiver snapshot and overwrites a synchronous edit made during suspension. `updateWith` preserves that edit because it replaces no data. | Document snapshot replacement. Follow-up **Async state lifecycle** must decide and test form-edit behavior during save; do not silently choose a merge policy here. | DF-07/08, DF-12 |
| Snackbar event consumption/delivery | **Reproduced with Compose/Robolectric:** recomposition reading an already-consumed error removes the effect and cancels its suspended callback. Replacing it with a new error under the same stable callback key consumes the new event without starting a new callback. Source CAS in `OneTimeEvent` allows at-most-once consumption, not delivery acknowledgement. Discarded-composition loss is source-reviewed, not reproduced. | Remove exactly-once snackbar delivery and defensive keying claims. Follow-up **Event delivery** must consume in an appropriate effect and test event identity, cancellation, repeated errors, and composition disposal. | DF-07, DF-11, DF-12 |
| Item save/load failure | **Reproduced:** failed repository save still creates `navigateBack=true`, exposes no error, and clears loading. Failed load leaves default form data and no error after a confirmed repository subscription. E06: save ignores Result; load catch constructs an unused state. Settings load catch has the same source defect. | Follow-up **Item failure handling** must map save Result before navigation and store load errors; include Settings load error handling. DF-07/08 must not recommend the copied tutorial snippets as correct failure handling. | DF-07/08, DF-11, DF-12 |
| Existing Room installs | **Source-confirmed:** version 2, no registered 1 → 2 migration, `fallbackToDestructiveMigration(true)`, schemas not exported (E05/E04). [Room fallback](https://developer.android.com/reference/androidx/room/RoomDatabase.Builder) recreates tables when an upgrade path is missing. No file-backed upgrade test was added. | Existing local rows, including unpushed edits/deletes, can be lost. Remote re-fetch cannot recover unsent data. Follow-up **Upgrade compatibility** requires a data-preserving migration and a file-backed test with pending changes before claiming upgrade safety. Fresh installs are a different case. | DF-03/06, DF-11–13 |
| Legacy Firestore documents and writers | **Source/SDK-confirmed:** the range query excludes documents missing `serverUpdatedAt`, even with cursor 0. Legacy `.set()` writers omit the new field: updated rules reject them; permissive old rules can let them replace a stamped document with an unstamped one. The new model's default null does not backfill server data. | No automatic backward compatibility. Follow-up **Upgrade compatibility** must define legacy backfill and a staged writer/rules rollout, then test mixed client versions. No assumed absence of adopter data and no reset recommendation. | DF-03/06, DF-11–13 |
| Rule timestamp requirement | Source: new writers map a null `@get:ServerTimestamp` property; the documented eight-field rule requires `serverUpdatedAt == request.time`. SDK docs support null substitution ([ServerTimestamp](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/ServerTimestamp)); rules define [request.time](https://firebase.google.com/docs/reference/rules/rules.firestore.Request#time) as the server timestamp for such writes. **Unverified:** deployed rules and end-to-end authorization; no credentials were read and no backend was contacted. | The intended new write shape agrees with the documented rule; this is not proof of deployment or legacy acceptance. Keep manual deployment explicit. Follow-up **Upgrade compatibility** includes emulator allow/deny tests; DF-03 repairs setup prose. | DF-03/06, DF-11–13 |

### Bounded runtime follow-ups

These are **local follow-up specifications**, not completed fixes or newly opened external issues.
Each concern above has an immediate documentation disposition; the repair can proceed with explicit
limitations. Runtime designs and compatibility policy require their own scoped work and existing
approval boundaries.

- **Sync consistency:** pending-write acknowledgement, another pass after concurrent writes,
  pending-edit protection during pull, and cross-device deletes. Acceptance requires controlled
  concurrent write/edit/delete tests and WorkManager scheduling coverage.
- **Pull checkpoints:** interruption within equal timestamps and incomplete cache results.
  Acceptance requires no skipped records on retry, plus SDK integration coverage of cache gaps.
- **Observation lifetime:** Home/Item/Settings and matching Profile observation jobs. Acceptance
  requires one intended repository collector across unsubscribe/restart cycles and correct locale
  refresh, with all collectors ending when the ViewModel is cleared.
- **Async state lifecycle:** exception/cancellation cleanup, launch-time overlap, and edits during
  suspension; review worker cancellation too. Preserve returned Result behavior and explicitly
  choose editing/concurrency semantics before changing shared helpers.
- **Event delivery:** effect lifecycle and event identity, with successful callback completion
  distinguished from event consumption. Reuse the two reproductions below as regression inputs.
- **Item failure handling:** navigate only on successful persistence, expose load/save failures,
  preserve entered form data, and cover Settings' unused load-error state.
- **Upgrade compatibility:** preserve pending Room data and define legacy Firestore backfill and
  mixed-client rollout. Acceptance includes migration and security-rule allow/deny tests; no
  migration, remote data rewrite, or deployment is authorized by this review.

### Tests and verification

New characterization coverage (17 tests):

- [Repository tests](data/src/test/kotlin/dev/atick/data/repository/home/HomeRepositoryImplTest.kt):
  five new controlled snapshot/upload/pull/retry tests. The fake sorts timestamp results to model
  the SDK's implied ordering; it is not an SDK cache, rules, or WorkManager emulator.
- [Item/Home tests](feature/home/src/test/kotlin/dev/atick/feature/home/ui/item/ItemViewModelTest.kt):
  four new tests for save/load failures and Home/Item subscription accumulation, with active UI
  collectors and scope cleanup.
- [Settings tests](feature/settings/src/test/kotlin/dev/atick/feature/settings/ui/SettingsViewModelTest.kt):
  one new zero-timeout subscription test using the existing Robolectric setup.
- [Queued helper tests](core/ui/src/test/kotlin/dev/atick/core/ui/utils/UiStateContractTest.kt):
  five new tests covering both helpers' overlap, exceptions, cancellation and suspended edits.
- [Compose event tests](core/ui/src/test/kotlin/dev/atick/core/ui/utils/StatefulComposableEventTest.kt):
  two new tests separating consumption from callback cancellation/replacement.

The existing repository cursor/order/echo tests, seven Item input tests, six Settings tests, helper
Result tests, swipe tests, network tests, serializer tests, and DAO tests predate this review.
Their presence is not evidence of concurrency, migration, cache completeness, or deployed-rule
coverage. The four affected debug module suites passed: **64 tests, zero failures/errors/skips**.

Validation used the working-tree changes on the baseline revision above, not a new commit:
`./gradlew :data:testDebugUnitTest :feature:home:testDebugUnitTest
:feature:settings:testDebugUnitTest :core:ui:testDebugUnitTest` passed, followed by a successful
format-and-test run with `spotlessApply --init-script gradle/init.gradle.kts
--no-configuration-cache` and the same four suites. A Compose test-rule deprecation warning follows
the existing test API; no dependency or convention-plugin change was needed.

Final verification: `./gradlew spotlessCheck --init-script gradle/init.gradle.kts
--no-configuration-cache :core:network:testDebugUnitTest :core:preferences:testDebugUnitTest
:core:room:testDebugUnitTest` passed (the three unchanged module test tasks were up-to-date).
`git diff --check` passed; the new local evidence links resolve. No documentation site build,
release APK, device test, live Firebase request, or external issue/comment is claimed for this task.
Published-guide edits and source-comment rewrites remain with their queued owners.

## DF-03 — Make first-run and Firebase setup complete

Depends on: DF-00; DF-02 for compatibility statements. Scope: `docs/getting-started.md`, `docs/firebase.md`.

- [x] Walk clone → change into the checkout → select JDK/SDK → build → install/run. State expected
  behavior with the template Firebase config and with a configured Firebase project.
- [x] Verify the Android Studio/AGP compatibility requirement from official documentation; replace
  “Hedgehog or newer” if incompatible with the pinned AGP. Recheck IDE preview instructions too.
- [x] Document the actual local configuration path: `GOOGLE_WEB_CLIENT_ID=<web OAuth client ID>` in
  ignored `local.properties`, defaults in `secrets.defaults.properties`, and how to obtain the
  correct client type. Distinguish this from downloading `app/google-services.json`.
- [x] Explain package/application ID matching, enabled auth providers, certificate fingerprints for
  the build being tested, and the steps needed after replacing template identifiers in an adopter app.
- [x] Verify Credential Manager's authorized-account filter and Firebase account creation semantics
  from the pinned SDK or official docs. Remove “entire sign-in/register contract in one flag.”
- [x] Describe handling of the tracked template config accurately. `--skip-worktree` is a local index
  flag, not a security control or true untracking; include how to inspect/undo it if retaining advice.
- [x] Keep Firestore rules, model fields, path, and soft-delete behavior consistent. Explain that
  editing a Markdown rules example does not deploy it; include the compatibility outcome from DF-02.
- [x] Verify Crashlytics setup/report collection and mapping-upload claims against build config and
  official guidance. Remove “almost always timing” diagnoses without evidence.
- [x] Keep signing guidance precise: a debug-signed release can be installed, but switching signing
  certificates affects upgrades. Avoid the unqualified “cannot be upgraded” statement.

Accept: someone following only the linked setup path knows every local setting required to build
and try auth/sync. No real credentials, console changes, or deployment are needed for doc validation.

### DF-03 completion record

Completed 2026-10-16 against working-tree base `bf6ea884cd4bf76e6625bd0bbc988db33dd1a53c`.
The tree was clean before implementation. Changed `docs/getting-started.md`, `docs/firebase.md`,
and this execution record only; no runtime, build configuration, dependency, or rules-policy changes.

- Restored clone/cd, JDK/SDK selection, build output, install and launcher steps. Replaced obsolete
  Studio and preview guidance. The official [Studio/AGP table](https://developer.android.com/build/releases/about-agp)
  supports Quail 2 (2026.1.2) with AGP 9.3; the catalog pins AGP 9.3.1, JDK 21, compileSdk 37,
  and minSdk 24. Checked the launcher name against app resources and the APK output on disk.
- Documented the separate JSON and web OAuth client inputs, local.properties/defaults/BuildConfig
  wiring, providers, signing fingerprints, changed application IDs, and index-hint inspection/undo.
  Inspected build scripts, Config and request builders without reading protected configuration.
  [Firebase Google auth](https://firebase.google.com/docs/auth/android/google-signin) and
  [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation)
  support the web-client type, account-filter and first-Firebase-user creation explanation.
- Checked the Firestore path and eight-field model against the implementation. Preserved the
  executable rules example byte-for-byte except its explanatory comment (verified by comparison
  with HEAD). Made publication explicit and carried forward the completed compatibility review:
  destructive local upgrade, missing-field exclusion, legacy-writer rejection and hard-delete
  propagation limits. Added adopter auth/write/edit/delete checks without claiming they were run.
- Corrected the telemetry scope after inspecting FirebaseConventionPlugin: the wrapper reports
  Crashlytics exceptions, while the app also includes Analytics and Performance SDKs. No custom
  event/trace instrumentation or applied Performance plugin was found. Linked official SDK setup.
  Checked report restart/debug-log steps and mapping-upload defaults against official
  [test instructions](https://firebase.google.com/docs/crashlytics/android/test-implementation) and
  [mapping guidance](https://firebase.google.com/docs/crashlytics/android/get-deobfuscated-reports).
- Clarified app-relative keystore paths, installable debug-signed release APKs and certificate
  continuity for updates. No signing configuration was changed or release upload attempted.

Verification:

- `./gradlew assembleDebug dokkaGeneratePublicationHtml` — BUILD SUCCESSFUL, 861 actionable tasks
  (6 executed, 855 up-to-date). This verifies the current checkout, not a newly cloned machine or
  a live Firebase setup.
- Assembled a temporary site using the existing workflow: generated Dokka HTML copied into
  `docs/api/`, root README copied into `docs/index.md` with `docs/` link prefixes removed, and the
  existing MkDocs config. Repository-generated site inputs were not modified.
- `mkdocs build --strict` — passed with no documentation warnings. Material printed its upstream
  MkDocs 2.0 notice; MkDocs reported the existing homepage-not-in-nav informational message.
  Validation used MkDocs 1.6.1, Material 9.7.7 and the workflow's admonitions plugin 0.1.1 in a
  temporary virtual environment.
- Inspected generated HTML for both edited pages: five code blocks each, four properly numbered
  quick-start steps, rendered note/caution/warning boxes, and all local links/fragments resolving.
  Fixed list continuation rendering and retained the existing `#crashlytics` anchor so the
  troubleshooting link continues to work. GitHub-compatible fences, lists and alerts remain in
  source; no live GitHub rendering or IDE Preview execution is claimed.
- `git diff --check` — passed. No new unit tests were needed for documentation-only edits.

Not run: device install/launch, Firebase console changes, live authentication/sync, deployed-rule
allow/deny tests, intentional crash reporting, release mapping upload, or upgrade migration tests.
Those require adopter configuration or separately scoped runtime work. The compatibility concerns
remain open under DF-02's bounded follow-ups; later data/reference tasks still own their broader
explanations. No commit, push, deployment, or external issue/comment was made.

## DF-04 — Restore build, release, and local docs instructions

Depends on: DF-00–01. Scope: `docs/build-and-tooling.md`; root README navigation.

- [x] Restore a compact command reference for build/install, unit and device tests, formatting,
  Dokka, Dependency Guard, APK badging, and baseline profiles; give prerequisites and output paths.
- [x] Restore release secret formats from `cd.yml`: `GOOGLE_SERVICES`, `KEYSTORE`,
  `KEYSTORE_PROPERTIES`, and `PLAY_STORE_JSON` are base64-decoded; `GOOGLE_WEB_CLIENT_ID` is written
  as plain text. Map each to its actual destination, including `app/key.jks` and matching `storeFile`.
- [x] Restore Play service-account setup references, `fastlane/Appfile` package/key path, Bundler
  workflow, actual metadata/changelog behavior, version-code preparation, and tag-triggered release
  steps. Verify current provider requirements with official sources. Use placeholders for secrets.
- [x] Describe GitHub APK publication and Play AAB upload as separate jobs. Inspect the legacy `beta`
  lane before presenting it as usable; do not treat generated `fastlane/README.md` as a setup guide.
- [x] Restore Compose metrics/report flags and actual output layout from `AndroidCompose.kt`:
  `enableComposeCompilerMetrics`, `enableComposeCompilerReports`, and root
  `build/<module>/compose-metrics` / `compose-reports`. Show how to inspect results.
- [x] Retain baseline profile generation and measurement instructions; verify managed-device task
  names and required API from the configured plugin/SDK. Remove unsupported “silently ignored” claims.
- [x] Explain existing convention-plugin use, catalog aliases, actual Renovate setup, and approval
  boundaries for build changes. Correct “all versions live in the catalog”: the init script and
  workflow contain explicit tool versions. Do not move versions as part of a prose edit.
- [x] Explain the Spotless init script's observed scope and invocation. Remove “every Gradle
  invocation” and unverified claims that the script formats the included build. Use
  `--init-script gradle/init.gradle.kts --no-configuration-cache` consistently.
- [x] Recover header-template customization instructions from `docs/spotless.md`, without changing
  headers or Spotless configuration as part of this task.
- [x] Document local site setup using the dependencies/extensions in `docs.yml` and `mkdocs.yml`:
  isolated Python environment, Dokka generation, API copy, README-to-index transformation, strict
  build, then preview. Explain which files are generated and ignored. Cross-check with DF-14.

Accept: build/format/preview commands are usable from a clean checkout; release preparation includes
formats and paths rather than only names. Publishing commands are documented, not executed.

### DF-04 completion record

Completed 2026-10-16 against working-tree base `28404fcc38c3f5f396934d50a91ad2d6ccd6b756`.
The initial tree was clean. Changed `docs/build-and-tooling.md`, README navigation, and this record
only; no runtime, dependency, signing, workflow, formatting configuration, or secret changes.

- Restored command prerequisites/results, catalog/BOM and convention-plugin use, Renovate adoption,
  header customization, local backend configuration, compiler report inspection, profiling and
  release-size diagnosis. Checked aliases, run configurations and paths against current sources.
- Restored the release setup, five secret formats/destinations, app-relative signing path, version
  formula, Appfile, Bundler, service-account access, metadata and separate APK/AAB publication jobs.
  [Google API setup](https://developers.google.com/android-publisher/getting_started) confirms that
  developer-account/Cloud-project linking is no longer required. [Fastlane setup](https://docs.fastlane.tools/getting-started/android/setup/)
  supplies the Bundler and initial metadata steps; [Supply](https://docs.fastlane.tools/actions/supply/)
  supplies initial-upload, release-note precedence and metadata behavior. No account setup was run.
- Downloaded Fastlane 2.237.0 to temporary storage and verified its SHA-256 against `Gemfile.lock`.
  Inspected its packaged sources: no `crashlytics.rb` action; Supply defaults to production/completed,
  and upload skip flags default to false. Documented the unusable legacy lane, real-upload behavior,
  overwritten default notes, ignored changelogs and shallow-checkout history limitation.
- Verified generation/measurement task names by Gradle task discovery and dry runs. Checked the
  cached baseline-profile plugin 1.5.0-alpha07 sources: application profile output defaults to
  `src/<variant>/generated/baselineProfiles`, with `saveInSrc=true` and no merge into main by default.
  Documented the configured API 33 environment without treating it as a consumer minimum.
- Restored the local Python/Dokka/MkDocs assembly and portable README link transformation. Added
  README links to release preparation and local preview. Generated inputs/output remain ignored.

Verification:

- `./gradlew :feature:home:compileReleaseKotlin dokkaGeneratePublicationHtml
  -PenableComposeCompilerMetrics=true -PenableComposeCompilerReports=true` — BUILD SUCCESSFUL,
  280 actionable tasks (6 executed, 274 up-to-date). Real Dokka HTML was used for site assembly.
- `./gradlew :feature:home:compileReleaseKotlin --rerun
  -PenableComposeCompilerMetrics=true -PenableComposeCompilerReports=true` — reran the focused
  compiler task to verify report generation; inspected `build/feature/home/compose-metrics/release/`
  JSON and the classes/composables text/CSV reports under `compose-reports/`.
- `./gradlew :benchmarks:tasks --all` and profile-generation dry run — passed. The filtered
  `:benchmarks:pixel6Api33BenchmarkReleaseAndroidTest` and `:feature:home:testDebugUnitTest` dry run
  also passed. No emulator was started and no profile sources were regenerated.
- `./gradlew tasks --all spotlessCheck --init-script gradle/init.gradle.kts
  --no-configuration-cache` — BUILD SUCCESSFUL. Tasks are discoverable with the init script;
  the root formatting check covers main-build subprojects, not the root project's files or the
  included build's formatting tasks.
- Explicit included-build task discovery succeeds, but
  `./gradlew :build-logic:convention:spotlessCheck --init-script gradle/init.gradle.kts
  --no-configuration-cache` fails at `spotlessGroovy` because
  `build-logic/spotless/copyright.gradle` does not exist. The guide now states this limitation.
  A build configuration fix needs separate scope/agreement; DF-12 still owns the misleading
  init-script source comment. Root formatting success is not presented as included-build coverage.
- Executed the guide's exact Python assembly snippet with real API HTML. Checked that all external
  README URLs remain unchanged and both new documentation fragments survive the transformation.
  `mkdocs build --strict` passes. Environment: Python 3.14, MkDocs 1.6.1, Material 9.7.7,
  admonitions plugin 0.1.1 and Pygments 2.20.0, using the existing isolated `.venv`.
- `mkdocs serve --dev-addr 127.0.0.1:8000` started successfully; HTTP retrieval served the updated
  guide. Inspected generated HTML: ten tables, fourteen code blocks, numbered procedures, rendered
  alerts, and all local guide links/fragments resolving. Browser inventory was empty, so no visual
  browser/GitHub rendering check is claimed. Material's upstream notice and the existing
  homepage-not-in-nav informational message remain; navigation/CI repairs belong to DF-14.
- All Bash blocks pass `bash -n`; the documented base64 pipeline round-trips placeholder bytes
  without wrapping. `git diff --check` passes. No new unit tests are needed for these prose edits.

Not run: fresh-machine dependency installation, device tests, full Android test/build checks,
profile capture/measurement, live credential validation, signing reports, account setup or any
release/deployment. No protected configuration was read and no commit, tag, push or external update
was made. The included-build formatter failure is a recorded existing limitation, not a new failure
introduced by this documentation change.

## DF-05 — Repair the architecture explanation and module map

Depends on: DF-00–02. Scope: `docs/architecture.md` and architecture links.

- [x] Replace generic fictional `HomeAction`, `Data`, `Repository`, and `Params` examples with actual
  types, or label a small conceptual example honestly. Remove repeated principles and summaries.
- [x] Add a current module dependency diagram/table from Gradle declarations. Distinguish build
  dependencies from runtime calls: `sync → data`, Firebase modules depend on `core:android`,
  `core:ui` exposes `core:navigation`, and `core:testing` has test-helper dependencies.
- [x] Explain the `SyncManager` interface in `data` and its Hilt binding/implementation in `sync`
  without drawing a Gradle dependency cycle. Include app assembly, lint, and benchmarks appropriately.
- [x] Correct “periodic” sync in prose and the sequence diagram; show actual trigger, pending local
  write, push, pull, Room emission, and lifecycle-aware UI collection.
- [x] Describe the two-layer design and useful extension criteria once. Avoid blanket prohibitions
  on combining REST/Firestore or adding a domain layer when an adopter's requirements call for it.
- [x] Link to a real test walkthrough and avoid hardcoded test counts or “full harness” claims that
  imply coverage of behavior the tests do not exercise.

Accept: each architecture arrow and example is supported by source; module-map links lead to a real
map; an adopter can understand where a change belongs.

### DF-05 completion record

Completed 2026-10-16 against clean starting revision `3ad166b84` on `fix/docs-v3`.
Changed `docs/architecture.md` and this task record. No executable code, public interface,
dependency, build configuration, or backend rule changed.

- Replaced fictional UI/repository/use-case examples with labeled HomeViewModel excerpts using
  `HomeScreenData`, `Jetpack`, `UiState`, and the actual delete method. Removed repeated principles,
  summaries, test counts, and unsupported coverage claims.
- Rebuilt the module graph/table from `settings.gradle.kts` and all module project-dependency
  declarations. Checked `implementation`, `api`, test-helper dependencies, `lintPublish`, baseline
  profile consumption, and benchmark targeting separately. The diagram covers application edges;
  the table includes all declared modules and tooling relationships.
- Traced `SyncManager` through its data-owned interface, `SyncManagerImpl`, `SyncModule` binding,
  app dependency, and the worker's call into HomeRepository. There is no `data → sync` build edge.
- Replaced periodic fetch with the actual request, local pending write, one-time constrained work,
  snapshot/push/acknowledgement/pull, Room observation, and lifecycle-aware screen collection.
  Documented dropped-request and concurrent-edit limitations from the existing behavior ledger.
- Checked Home, Profile, Settings and Auth repositories to distinguish Room, DataStore, and explicit
  auth-profile writes. Kept domain-layer extension criteria and REST/Firestore choices concise.
- Preserved the `testing` anchor consumed by `core/testing/README.md`. Added a walkthrough and exact
  command for the existing SettingsRepositoryImplTest, plus a ViewModel test link. Existing generic
  architecture links still resolve to the repaired page; no caller edits were needed.

Verification against the documentation working tree above starting revision `3ad166b84`:

- `./gradlew :data:testDebugUnitTest --tests
  'dev.atick.data.repository.settings.SettingsRepositoryImplTest' check spotlessCheck
  --init-script gradle/init.gradle.kts --no-configuration-cache` — BUILD SUCCESSFUL,
  1,141 actionable tasks (19 executed, 1,122 up-to-date). The selected debug repository suite's XML
  reports seven tests with no failures, errors, or skips. Other project checks reused outputs where
  Gradle considered them current; this is not a claim of a fresh-device run. Root Spotless success
  does not cover the separately recorded included-build formatter limitation.
- `./gradlew dokkaGeneratePublicationHtml` — BUILD SUCCESSFUL, 280 actionable tasks
  (6 executed, 274 up-to-date). Used the real HTML output and the exact documented Python
  homepage/API assembly procedure before rebuilding MkDocs.
- `.venv/bin/python -m mkdocs build --strict` — passed. The upstream Material notice and existing
  homepage-not-in-nav informational message remain; navigation changes are separately queued.
- Inspected generated architecture HTML with Python's HTML parser: two tables, two Mermaid blocks,
  preserved `testing` anchor, new `module-dependencies` anchor, and all 56 local links/fragments
  resolve. Mermaid source was checked against the operations and Gradle declarations above.
- `git diff --check` — passed; final diff is limited to the two authored Markdown files.

Visual browser inspection was attempted but blocked: browser inventory had no connected browser,
then native Chrome access reported pending macOS Accessibility/Screen Recording permissions.
Generated HTML checks do not establish visual Mermaid layout or GitHub rendering. No device tests,
release, deployment, commit, push, or external update was performed. Spotless apply remains required
before a later commit; no formatter rewrite was needed for this Markdown-only change.

## DF-06 — Repair data, DI, persistence, and sync guidance

Depends on: DF-02, DF-05. Scope: `docs/data.md`, related module READMEs/KDoc, Firebase cross-links.

- [x] Restore real DI examples: repository `@Binds`, Room/Retrofit/DataStore `@Provides`, qualifiers,
  `@ApplicationContext`, scope/install location, and fake injection in tests. Avoid calling `@Binds`
  merely a cast or inventing scopes used by this project.
- [x] Describe JSON-backed typed DataStore and the actual corruption exception path. Explain
  defaults, adding a preference, serializer compatibility, and reset/sign-out behavior without
  claiming automatic corruption recovery.
- [x] Restore Room entity/DAO/database registration and version/migration guidance. Explicitly
  describe destructive fallback and loss of unsynced data; distinguish version upgrades from
  same-version schema-integrity failures. Use the compatibility decision from DF-02.
- [x] Correct the timestamp table using DAO queries and both mapping directions: milliseconds versus
  nanoseconds, local pending-change checks, server pull cursor, and transmitted `lastSynced`.
- [x] State what the sync code does in order. Correct “every read requests sync” (`getJetpack(id)`
  does not), “drain everything,” guaranteed follow-up runs, and conflict-free push/pull claims.
- [x] Explain soft-delete/tombstone retention accurately. Rows remain after successful push; no
  automatic tombstone cleanup is implemented. Keep server-cursor inclusion of deleted rows clear.
- [x] Replace “writes always succeed” with the actual boundary: local persistence and enqueueing
  can fail; a successful local operation does not mean remote sync succeeded.
- [x] State cancellation behavior and error boundaries accurately. `suspendRunCatching` rethrows
  cancellation; data sources may throw; Flow failures need handling; update helpers consume Results.
- [x] Document DF-02 sync limitations and upgrade requirements near the algorithm, without a
  blanket safety assertion. Explain the server timestamp mechanism once, with short local reminders.
- [x] Restore how to observe sync progress and locate scheduling/retry constraints. Keep the worker
  and repository roles distinct; verify retries versus total attempts and the existing backoff.

Accept: documented persistence/error/sync guarantees do not exceed implementation; an adopter can
extend storage and understand the consequences for existing data.

### DF-06 completion record

Completed 2026-10-16 against clean starting revision `5b16904eabb578a0afbe10034c74b7e8866089c4`
on `fix/docs-v3`. No executable behavior, API, dependency, schema, configuration, or rules changed.

Changed paths:

- Main guide: `docs/data.md`.
- Related references: `core/preferences/README.md`, `core/room/README.md`, `data/README.md`,
  `sync/README.md`, `firebase/firestore/README.md`.
- Preferences KDoc: `core/preferences/src/main/kotlin/dev/atick/core/preferences/model/UserDataPreferences.kt`
  and `core/preferences/src/main/kotlin/dev/atick/core/preferences/utils/UserDataSerializer.kt`.
- Room KDoc: `core/room/src/main/kotlin/dev/atick/core/room/data/JetpackDao.kt`,
  `core/room/src/main/kotlin/dev/atick/core/room/di/DatabaseModule.kt`,
  `core/room/src/main/kotlin/dev/atick/core/room/model/JetpackEntity.kt`.
- Data KDoc/comments: `data/src/main/kotlin/dev/atick/data/model/home/Jetpack.kt`,
  `data/src/main/kotlin/dev/atick/data/repository/home/HomeRepositoryImpl.kt`,
  `data/src/main/kotlin/dev/atick/data/utils/SyncUtils.kt`.
- Scheduling/worker KDoc/comments: `sync/src/main/kotlin/dev/atick/sync/manager/Sync.kt`,
  `sync/src/main/kotlin/dev/atick/sync/manager/SyncManagerImpl.kt`,
  `sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt`.
- This execution record. Generated site/API output remains ignored.

Coverage and decisions:

- Restored storage selection and actual REST access, Result/Flow/cancellation boundaries, DI
  bindings/providers/qualifiers/lifetimes, constructor-supplied fakes, and Android field/worker
  injection context. Included current Room, Retrofit, DataStore, preference-write, and remote-mapping
  excerpts; labeled omissions and hypothetical extensions. Linked keyed ViewModel guidance and
  maintained Hilt documentation rather than restoring unused advanced DI examples.
- Restored preference evolution/reset and Room entity/DAO/provider/migration steps. Documented
  JSON corruption propagation without a handler, strict serializer compatibility, destructive
  version-upgrade loss (including unsent changes), and the separate same-version integrity case.
- Replaced the sync explanation and sequence diagram with the pending snapshot/push/ack/pull
  sequence. Corrected device-millisecond versus server-nanosecond metadata, both mapping directions,
  retained local deleted rows, cursor inclusion, and absent cross-device hard-delete delivery.
- Documented local persistence/request failure boundaries, concurrent edits, dropped requests,
  equal-timestamp interruptions, SDK cache completeness limits, legacy documents/writers, progress
  phases, RUNNING-only status, and connected/expedited scheduling. Corrected retry count to three
  retries after the initial attempt; default backoff remains nominally 30/60/120 seconds.
- The DF-00 recovery rows owned here are covered in the corresponding guide sections. Explicit
  mapping and storage extension steps replace fictional helpers; listener/cache/manual-refresh
  ideas remain labeled extensions. Broad README reconciliation and unrelated source comments
  remain with their later owners.

Verification:

- `./gradlew :data:testDebugUnitTest :core:preferences:testDebugUnitTest
  :core:room:testDebugUnitTest :core:android:testDebugUnitTest check spotlessCheck
  --init-script gradle/init.gradle.kts --no-configuration-cache` — BUILD SUCCESSFUL;
  1,141 actionable tasks, 183 executed and 958 up-to-date. Debug XML reports: data 19,
  preferences 10, Room 18, Android utilities 38 tests; all 85 have zero failures/errors/skips.
  These include the existing race, interrupted-pull, serializer, DAO, and Result/cancellation tests;
  no new tests were added for prose. This is not fresh-device or SDK-cache integration coverage.
- `./gradlew dokkaGeneratePublicationHtml spotlessCheck --init-script gradle/init.gradle.kts
  --no-configuration-cache` — BUILD SUCCESSFUL; 456 actionable tasks, 61 executed, 5 from cache,
  390 up-to-date. No unresolved-link/warning/error diagnostics in the captured log. Root Spotless
  does not resolve the previously recorded included-build formatter limitation.
- Assembled the real Dokka HTML and README homepage with the procedure in the build guide, then
  `.venv/bin/python -m mkdocs build --strict` — passed. Existing Material notice and
  homepage-not-in-nav informational message remain; site configuration work is separately queued.
- Parsed generated data HTML: three tables, one Mermaid block, all five original section anchors
  preserved, and all 144 local link/fragment references resolve.
- Inspected the local site in Chrome: metadata table wraps within the content column, and the
  push/pull Mermaid sequence renders with participants, loops, acknowledgements, and progress.
- Compared executable token streams against the starting revision, preserving string literals:
  all eleven changed Kotlin files differ only in comments/KDoc. `git diff --check` passed.

Unresolved runtime work remains in the [bounded follow-ups](#bounded-runtime-follow-ups): sync
consistency, pull checkpoints/cache verification, cancellation lifecycle, and upgrade compatibility.
No emulator/backend authorization test, migration, runtime fix, deployment, commit, push, or external
update was performed. Spotless apply remains required before a later commit.

## DF-07 — Make state and event documentation trustworthy

Depends on: DF-02. Scope: `docs/state-management.md`, `StatefulComposable.kt`, linked ViewModel KDoc.

- [x] Correct the update-function table: `updateState` replaces `data` synchronously via its receiver
  lambda; `updateStateWith` replaces it after a successful Result; `updateWith` preserves it.
- [x] Describe returned failure handling separately from thrown exceptions/cancellation. Remove
  claims that helpers automatically catch everything or reset loading on every possible exit.
- [x] Explain the loading check's actual call assumptions, dropped actions, and suspended-operation
  snapshot behavior. Give readers guidance for independent operations and edits during loading,
  using an explicitly labeled extension if the template does not implement it.
- [x] Document subscription/restart behavior from DF-02 and show an appropriate test pattern that
  actively collects `stateInDelayed`; do not teach that a `.value` assertion always exercises it.
- [x] Replace exactly-once delivery guarantees with the actual `OneTimeEvent` consumption contract;
  align error/snackbar and navigation explanations with the DF-02 outcome.
- [x] Fix the save example after DF-02 disposition. Clearly show how the repository Result affects
  the success event, or identify the current limitation; do not advertise the ignored Result as safe.
- [x] Keep raw price input guidance, but explain parsing/formatting separately. Verify edge-case
  examples such as `1.` against the actual parser instead of claiming every issue is parse failure.
- [x] Remove fabricated Posts/User.EMPTY demonstrations and blanket “nullable fields are bad” advice.
  Explain renderable initial state and legitimate optional data using current screen models.
- [x] Reduce duplicate helper tutorials in KDoc; retain callable contracts, relevant examples,
  parameters, and resolvable API links. Put longer lifecycle explanations in the guide.

Accept: a reader can select an update function, handle a failed operation, and test a collected
ViewModel state without relying on unsupported lifecycle/error guarantees.

### DF-07 completion record

Completed 2026-10-16 against clean starting revision `7aaee2d2b` on `fix/docs-v3`.
No executable behavior, API, dependency, schema, or configuration changed.

Changed paths:

- `docs/state-management.md`.
- State/helper KDoc and comments in `core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt`.
- Event and sharing KDoc in `core/android/src/main/kotlin/dev/atick/core/utils/OneTimeEvent.kt`
  and `core/android/src/main/kotlin/dev/atick/core/extensions/FlowExtensions.kt`.
- Home/Item ViewModel KDoc and directly related comments in
  `feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeViewModel.kt` and
  `feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemViewModel.kt`.
- This execution record. Generated API/site output and example harnesses are not tracked.

Coverage and decisions:

- Corrected helper selection, synchronous replacement, returned Result handling, uncaught
  exception/cancellation cleanup limits, launch-time overlap, dropped calls, and suspended snapshots.
  Restored independent-operation, data/whole-state reset, optional-field, and optimistic-update
  guidance. Extensions and their concurrency/rollback responsibilities are labeled explicitly.
- Explained WhileSubscribed upstream lifetimes separately from ViewModel-owned repository jobs,
  including collector accumulation on restart. Added a complete active-collection Item input test
  with imports, MainDispatcherRule, Turbine, and a constructor-supplied fake.
- Replaced delivery guarantees with at-most-once consumption, null-content and competing-consumer
  behavior, callback return handling, reproduced snackbar cancellation/replacement limits, and
  the separately source-reviewed discarded-composition risk. Navigation consumption is described
  separately from its completion and from Item's current failed-save behavior.
- Preserved current Item save/load limitations and linked the bounded runtime follow-ups. The guide
  presents a proposed Result.map save correction, not an already-applied fix. Raw text, parsing,
  zero fallback, and missing price validation are distinguished; trailing-decimal parsing is verified.
- Replaced duplicate fictional helper tutorials with callable contracts and real API examples.
  Removed unresolved cross-module event KDoc links and Route-wrapper instructions in edited comments.
  The DF-00 recovery topics owned here are covered by the guide's helper, lifecycle, reset,
  extension, event, and testing sections. Full feature walkthrough/module-reference repair remains
  with its later owners.

Verification:

- `./gradlew :core:ui:testDebugUnitTest :core:android:testDebugUnitTest
  :feature:home:testDebugUnitTest check spotlessCheck dokkaGeneratePublicationHtml
  --init-script gradle/init.gradle.kts --no-configuration-cache` — BUILD SUCCESSFUL;
  1,417 actionable tasks, 316 executed, 3 from cache, 1,098 up-to-date. Debug XML reports:
  UI 27, Android utilities 38, Home 11 tests; all 76 have zero failures/errors/skips.
  Spotless emitted its formatter-resolution notice for StatefulComposable.kt but both check tasks
  passed. Dokka reported unresolved references in unchanged files, including UI components,
  CoroutineUtils, Settings and Firebase references; none identify an edited source file. These
  outstanding reference repairs remain with the later module/KDoc cleanup tasks.
- Extracted the guide's complete test and proposed save method into `/tmp/df07-examples`, wired
  only Kotlin's `debugUnitTest` source set using `/tmp/df07-examples.init.gradle`, and ran
  `./gradlew :feature:home:testDebugUnitTest --tests '*ItemInputExampleTest'
  --tests '*ProposedSaveExampleTest' --init-script /tmp/df07-examples.init.gradle
  --no-configuration-cache` — BUILD SUCCESSFUL. Both tests passed: raw trailing-decimal text,
  returned save success/failure and navigation/error/loading outcomes, plus `1.` and `12e` parsing.
  Initial harness attempts failed while configuring the included build/source-set names; the final
  isolated run compiled and exercised both examples. No test or example was added to production.
- Assembled real Dokka output and the README homepage using the build guide's procedure;
  `.venv/bin/python -m mkdocs build --strict` passed after the final guide edit. Existing Material
  notice and homepage-not-in-nav informational message remain.
- Regenerated API output after the final consumption-order wording correction with
  `./gradlew dokkaGeneratePublicationHtml spotlessCheck --init-script gradle/init.gradle.kts
  --no-configuration-cache` — BUILD SUCCESSFUL; 456 actionable tasks, 13 executed, 443 up-to-date.
  Reassembled the site and repeated the strict MkDocs build successfully.
- Inspected the guide's comparison table and the generated updateStateWith API contract in Chrome.
  Shortened table labels after visual review; inspected the final table, worked test, and event API
  page. All 125 local guide links/fragment references resolve against the generated site.
- Compared executable Kotlin token streams against HEAD while preserving string literals: all
  five changed Kotlin files differ only in comments/KDoc. `git diff --check` passed.

Runtime follow-ups remain unchanged: Async state lifecycle, Observation lifetime, Event delivery,
and Item failure handling. No runtime fix, commit, push, deployment, or external update was performed.
Spotless apply remains required before a later commit.

## DF-08 — Turn the feature tour into a usable walkthrough

Depends on: DF-05–07. Scope: `docs/guide.md`, `docs/navigation.md`.

- [x] State which excerpts explain the existing home feature and which steps create an adopter's
  new feature. Include exact placement and integration steps omitted by a source-code tour.
- [x] Cover module registration/plugin/dependency wiring when a new module is needed; otherwise
  explain adding a screen to an existing module. Respect the existing approval boundary for new modules.
- [x] Cover entity/DAO/database registration and DI, repository binding, screen data, ViewModel,
  destination entries, app-level registration, and tests. For synced features, identify the worker
  integration point rather than implying `Syncable` alone registers the feature.
- [x] Update the `JetpackEntity` excerpt to include `serverUpdatedAtNanos` or explicitly mark the
  omitted metadata. Check all other snippets against their source and label abbreviations.
- [x] Preserve Navigation 3, assisted injection, multi-stack restoration, list/detail panes, and back
  handling. Explain adding a top-level destination and account-boundary stack behavior.
- [x] Clarify navigation identity: equal content keys matter, not simply reuse of a key type. Verify
  duplicate-entry advice against the pinned Navigation 3 implementation before proposing a solution.
- [x] Use a small real feature test with `MainDispatcherRule`, a fake, active Flow collection, and
  meaningful success/failure assertions. Existing test links alone are not a worked example.
- [x] Walk the complete sequence in a disposable checkout or a clearly labeled example, checking
  imports, visibility, resource prefixes, and Gradle registration. Do not add a demonstration module
  to the production tree merely to validate documentation.

Accept: a reader can follow the guide through app integration and validation; omitted code is
identified, and examples do not disguise unresolved DF-02 behavior as a recommended pattern.

### DF-08 completion record

Completed 2026-10-17 against clean starting revision `af070eb7b` on `fix/docs-v3`.
Tracked changes are limited to `docs/guide.md`, `docs/navigation.md`, and this execution record.
No production runtime, public API, dependency, module, schema, or configuration changed.

Coverage and decisions:

- Kept Home/Item as the real running example, as selected during planning. Distinguished existing
  excerpts, omitted members/imports, and adopter integration steps. Added module registration,
  plugin/dependency setup, source placement, namespace/resource prefixes, and the existing-module path.
- Restored entity/DAO/database/accessor/provider registration, data-source construction/binding,
  repository binding with full Hilt module context, and schema/migration responsibilities. Included
  `serverUpdatedAtNanos`; linked complete mappings and local/REST/synced source choices.
- Added the explicit `SyncWorker` repository/Flow-collection integration point. `Syncable` alone
  does not register work. Preserved the documented scheduling, concurrency, deletion, and cursor
  limitations; no sync protocol or security-rule design was changed.
- Covered screen data, observation, Screen callbacks/visibility, raw price text, assisted creation,
  navigation events, app entry-provider registration, top-level enum/resources, and create FAB wiring.
  Removed the unqualified failed-save snippet. Linked the proposed Result.map correction and kept
  Item load/save defects, observation lifetime, and event-delivery limitations visible.
- Corrected navigation identity against exact published source artifacts: Navigation 3 1.1.4
  `defaultContentKey` returns `key.toString()`; its saveable-state decorator and lifecycle 2.11.0
  decorator use content keys. Compose saveable-state 1.11.4 rejects simultaneous duplicate keys.
  Equal key values and shared key types are distinguished; no cross-tab identity redesign was made.
- Preserved multi-stack restoration, list/detail metadata, explicit back handling and top-level
  history. Explained signed-in/signed-out graph removal without claiming complete account-data reset.
- Added a complete MainDispatcherRule/fake/Turbine test with an explicitly suspended delete,
  success/failure outcomes, loading, observed data, original error identity, and scope cleanup.
  Restored DAO/repository test expectations and manual device/Firebase acceptance steps.
- These sections cover the DF-00 recovery items owned here: module/DI setup, persistence wiring,
  model→repository→UI integration, direct Screens, Navigation 3/assisted arguments, app registration,
  and worked unit testing. Module README/KDoc reconciliation remains with DF-11/12.

Verification:

- `./gradlew :feature:home:testDebugUnitTest :core:navigation:testDebugUnitTest
  --no-configuration-cache` — BUILD SUCCESSFUL before edits. The initial sandboxed invocation
  could not create the Gradle wrapper lock; the approved cache-access retry succeeded.
- Extracted the guide's complete test unchanged into `/tmp/df08-examples`, wired only the Home
  `debugUnitTest` source set through `/tmp/df08-examples.init.gradle`, and ran
  `./gradlew :feature:home:testDebugUnitTest --tests '*HomeFeatureExampleTest'
  --init-script /tmp/df08-examples.init.gradle --no-configuration-cache` — BUILD SUCCESSFUL;
  both example tests passed. No example test was added to the production tree.
- `./gradlew :core:room:testDebugUnitTest :data:testDebugUnitTest :feature:home:testDebugUnitTest
  :core:navigation:testDebugUnitTest :app:assembleDebug spotlessCheck dokkaGeneratePublicationHtml
  --init-script gradle/init.gradle.kts --no-configuration-cache` — BUILD SUCCESSFUL;
  927 actionable tasks, 19 executed, 4 from cache, 904 up-to-date. Debug XML results: Room 18,
  Data 19, Home 11, Navigation 14 tests; all 62 have zero failures/errors/skips. Dokka tasks were
  up-to-date: this change edits no KDoc or executable source.
- Walked the integration sequence in a disposable source copy at `/tmp/df08-walkthrough`, created
  from tracked files while excluding credentials, service files, keystores and local properties.
  `/tmp/df08-disposable.py` records the adaptation; `/tmp/df08-add-validation-tests.py` adds its
  DAO/repository checks. Added a local-only Catalog model/table/DAO/data source/repository, Hilt
  providers/bindings, a renamed Home/Item-shaped UI module, prefixed resources, top-level tab,
  entries and create FAB. Its create/edit ViewModel uses the labeled proposed Result.map save
  correction. A fresh database version was used only in this disposable copy; no upgrade was tested.
- Disposable initial command: `./gradlew :feature:catalog:testDebugUnitTest
  :core:room:testDebugUnitTest :data:testDebugUnitTest :app:assembleDebug
  --init-script df08-validation.init.gradle --no-configuration-cache` — BUILD SUCCESSFUL;
  483 actionable tasks, 429 executed, 54 from cache. Google Services processing is disabled only
  in this copy; this validates Gradle/KSP/Hilt/resource/app integration without a Firebase runtime.
- Temporarily swallowed the delete Result in the disposable Catalog ViewModel. The worked
  failure test failed, while its success test passed (`/tmp/df08-mutation.log`). Restored the
  ViewModel, aligned fake parameter names, and added two proposed-save success/failure tests.
  Final disposable command adds `:feature:catalog:lintDebug` to the initial command — BUILD
  SUCCESSFUL; 690 actionable tasks, 237 executed, 7 from cache, 446 up-to-date. XML results:
  Catalog 4, Room 19, Data 21 tests, all zero failures/errors/skips. Lint has no errors and one
  VectorRaster warning from the copied resources. DAO coverage checks upsert/read/update/delete;
  repository coverage checks mapping, writes and returned failures. Save checks cover raw input,
  parsing fallback, error/loading and navigation only on returned success.
- Downloaded exact Navigation/lifecycle source archives from Google Maven, rather than relying
  on the older cached sources. `:core:navigation:dependencyInsight --dependency runtime-saveable
  --configuration debugRuntimeClasspath` confirmed Compose saveable-state 1.11.4; checked that
  version's source too. The three source links are in the navigation guide. Duplicate-renderer
  failure behavior is source-verified, not a newly executed Compose reproduction.
- Assembled real Dokka output and the README homepage using the existing build-guide procedure.
  `.venv/bin/python -m mkdocs build --strict` passed after the final edits. Existing Material notice
  and homepage-not-in-nav informational message remain. Checked all 518 local links/fragments
  across the two rendered pages with `/tmp/df08-check-links.py`: no missing files or fragments.
- Inspected module setup, the worked test and navigation identity in Chrome. Replaced the overview's
  zero-based ordered list with bullets to avoid Markdown renumbering against the section headings,
  and shortened the worked test's inline placement text. `git diff --check` passed.

Device/emulator interaction, configured Firebase, migration upgrades, cross-tab decorated-renderer
collision tests, and worker/remote integration were not executed. The guide identifies the relevant
manual/extension checks; it does not claim those guarantees. Existing bounded runtime follow-ups
remain unchanged. Generated site/API output, disposable code and test harnesses are untracked.
No commit, push, deployment, or external update was performed; Spotless apply remains required
before a later commit.

## DF-09 — Restore practical UI customization guidance

Depends on: DF-00–01. Scope: `docs/components.md`, UI/localization references.

- [x] Rewrite the opening to explain what the components provide. Put lint rules alongside guidance
  for choosing/adding a component, instead of opening with a threat that the build will fail.
- [x] Keep the useful catalog and link to source/API details. Verify overloads, behavior, default
  colors, adaptive navigation modes, image-loader/tint claims, and supported lint substitutions.
- [x] Remove fragile component/file counts and unsupported “used/unused” declarations, or verify
  usages including calls inside other wrappers. Avoid repeated “real” as a quality claim.
- [x] Restore short examples for theme colors, typography, dynamic color, previewing, and creating a
  component. Explain public API choices and extending `DesignSystemDetector`/its tests when relevant.
- [x] Restore actionable accessibility guidance: labels/decorative icons, touch interaction,
  semantics, loading/error announcements, and checking customized theme contrast. Do not restore
  claims that theme colors automatically guarantee accessibility.
- [x] Restore the localization path: resources and prefixes, `Language`, language option UI,
  `getPreferredLanguage`, locale config, AppCompat persistence, and RTL checks.
- [x] Give `UiText`, permission helpers, validation utilities, and shared preview annotations useful
  examples or maintained links. Separate lint conventions from actual preview failure causes.

Accept: a reader can customize or extend the UI accessibly and test previews/locales using the
project's components, without reading the entire component implementation first.

### DF-09 completion record

Completed 2026-10-17 on the working tree based on
`315e673e1602655320bd5069dbcb88a08c1ce651`. The starting tree was clean. Changed authored paths:
`docs/components.md`, `core/ui/README.md`, and this execution record. No production API, runtime,
resource, manifest, dependency, lint-rule or source-comment changes; no commit or publication.

The guide now starts with choosing components, retains the catalog, and restores customization,
accessibility, previews, localization, utilities, extension and performance guidance. The UI README
links to the restored utility/preview sections and corrects its contradictory Screen/Route claim.

#### Recovery coverage

All DF-09-owned rows in the DF-00 ledger have destinations below. Historical examples were used
as reader needs, not restored as current APIs.

| Assigned source sections | Repaired destination / disposition |
|---|---|
| Components opening, selection, buttons, inputs, app bars, navigation, dividers and FAB | Catalog and Real usage; preserve current overloads and screen excerpts, label omissions. Remove counts and unverified used/unused lists. Navigation delegates to adaptive defaults; no asserted expanded-window drawer mapping. |
| Chips, tags, icon toggles, exclusive options and tabs | Catalog, caller-owned selection explanation, and a labeled hoisted-tab excerpt in Add a shared component. Remove unsupported option-count enforcement and tag-persistence distinctions. |
| Loaders, swipe dismissal, image loading and backgrounds | Catalog, Images and loading feedback, and Theming; distinguish visual overlay from an interaction barrier, explain effect callback scope/no undo, painter/error/preview behavior, loader configuration and theme tint. |
| Theme colors, typography, overrides, dynamic color; tips prebuilt components/theming; FAQ customization | Theming; paired light/dark roles, actual wrapper color choices, local text/gradient/tint examples and the settings preference path. No invented button colors parameter. |
| Accessibility, custom-component creation and organization; FAQ component selection/creation | Accessibility and Add a shared component; labeled complete retry example, caller-owned state, resource prefix, package/placement, KDoc, preview and meaningful interaction checks. Enforcement explains when detector mappings/tests need extension. |
| Tips shared previews and preview optimization | Previews and complete component preview; independent multipreview configurations, explicit device/theme combinations, locale/font checks and render-failure diagnosis. Correct the eight-combination claim using Android's maintained preview reference. |
| Tips multilingual support | Localization; resources → Language → option labels/order → preferred-language mapping → AppCompat locale setter/persistence → generated locale config → restart/system-picker/RTL checks. No claimed DataStore language persistence. |
| Tips validators; core/ui README text/permission usage | Text, validation and permissions; actual nullable validators, String-backed TextFieldData, UiText resource arguments/resolution, complete lifecycle-registered permission launcher and the high-level helper's early-return/denial limitations. |
| Components performance; FAQ Compose optimization; deleted performance stability/lists/images; tips image/loading performance | Performance checks plus Images and loading feedback; stable keys, optional mixed-list content types, stability contracts, loader behavior and links to existing compiler reports/profiling. Remove universal lambda/derived-state prescriptions and unsupported crossfade guarantees. |
| Tips component conventions | Add a shared component and Enforcement; existing wrappers first, feature-local vs shared placement, public API choices and lint tests. Documentation build workflow remains with the already repaired build guide. |
| Deleted performance image guidance also assigned to core/network README | Keep loader/image usage in the guide beside wrapper behavior and source location; core/network README reconciliation remains DF-11. No second image tutorial added. |

#### Verification

| Command / review | Result and limits |
|---|---|
| `./gradlew :core:ui:compileDebugKotlin` | Passed with the two complete guide examples extracted into a temporary debug Kotlin source: notification permission Activity and RetryButton/preview. Removed that source before quality checks. Partial argument/state/tab excerpts were checked against signatures and surrounding source; they are labeled as excerpts. |
| `./gradlew dokkaGeneratePublicationHtml` | Passed; inspected generated Core UI module entry point and guide link. Generated artifacts remain ignored. |
| Local assembly from docs/build-and-tooling.md, then `.venv/bin/python -m mkdocs build --strict` | Passed. Existing informational notice that index.md is absent from nav remains; no strict-build warnings/errors. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache check` | Passed, including repository quality gates (1,141 actionable tasks). Temporary examples were absent. No formatter apply or commit performed. |
| `git diff --check` | Passed; only the three authored Markdown paths remain changed. |
| Chrome inspection of locally served generated pages | Inspected components catalog/table structure, table of contents, extension code rendering and linked Core UI Dokka module. Browser rendering of documentation was exercised; no app installed or docs deployed. |
| Preview combinations | Checked annotation definitions and Android's official multipreview documentation linked in the guide. Live Android Studio rendering was not exercised. |
| Accessibility/theme/locale scenarios | Source and guidance inspection only. No TalkBack/Scanner, contrast measurements, large-font device run, pre-33/33+ locale persistence or RTL interaction run claimed. These are adopter checks, not results established by this docs-only change. |

Gradle initially failed inside the sandbox because it could not access its wrapper lock; rerunning
with approved cache access passed. Local preview serving likewise required approved localhost
socket access. Logs are in `/private/tmp/df09-{examples,dokka,mkdocs,check}.log` (local evidence,
not repository artifacts). No protected configuration or credential files were opened.

#### Visible limitations and queued follow-ups

- `DynamicAsyncImage` converts a null description to the string `"null"` for its loading wheel.
  The guide exposes this limitation; fixing accessibility behavior needs a separate runtime scope.
- The permission helper's already-granted early return and internal launcher registration remain
  unchanged; the guide teaches a lifecycle-registered launcher for user actions.
- Current auth validation messages are hardcoded English. The guide identifies the localization
  gap without changing state types or introducing a new text/error architecture.
- DF-11 retains the broader module README reconciliation. DF-12 owns stale source KDoc, including
  the preview Cartesian-product claim, navigation drawer assertion, and image-wrapper description.
  DF-14 retains site-wide URL/source-link compatibility; DF-10 is the next queued repair task.

## DF-10 — Rebuild troubleshooting around reliable diagnosis

Depends on: DF-03–09. Scope: all entries in `docs/troubleshooting.md`.

- [x] Review all 29 existing entries and unique deleted symptoms from DF-00. Each should state the
  symptom, plausible causes, how to distinguish them, a fix, and how to confirm recovery.
- [x] Correct claims that `collectAsState()` prevents updates, ordinary `@Preview` or non-private
  previews inherently fail, or every repeated event is caused by omitting `OneTimeEvent`.
- [x] Correct sync retry diagnosis: swallowed errors do not explain retry exhaustion. Distinguish
  pending constraints, dropped requests, authentication/rules failures, and exhausted retries.
- [x] Replace client-clock conflict-resolution advice with the DF-02/06 outcome. Verify debugging
  commands; do not assume an Android device ships a usable `sqlite3` binary.
- [x] Check configuration-cache/plugin diagnoses against current build config and upstream sources.
  Remove “nothing to do” as a universal answer to configuration-cache warnings.
- [x] Correct Room integrity/migration advice and notification/foreground-service guidance against
  the actual manifest and permission flow. Link to the specific setup/configuration steps.
- [x] Replace “clean build --refresh-dependencies resolves most mysterious states” with targeted
  checks. Include missing new-tool symptoms such as dependency/badging baseline drift and lint errors.
- [x] Move “How do I run tests?” to the command/testing guide; replace remaining “Cause: N/A” entries
  with appropriate instructional content. Remove editorial commentary about nonexistent symptoms.
- [x] Preserve readable/searchable summaries and functional links, alerts, and code inside details
  blocks in both GitHub and MkDocs rendering.

Accept: each entry helps diagnose rather than guessing a single cause; restored topics are easy
to find, with no arbitrary line ceiling.

### DF-10 completion record

Completed 2026-10-17 from clean working-tree revision `6e84c49c645bc6c24c32151b2b1a8edd21897419`.
Changed only `docs/troubleshooting.md` and this execution record. No runtime, public API, dependency,
manifest, backend, database, build configuration or source-comment changes; no commit or deployment.

#### Coverage and recovery disposition

Reviewed all 29 prior entries. The resulting page has 34 collapsible entries plus direct Testing
instructions; entry count is an observation, not an acceptance target. All ten existing section
anchors are preserved. The following groups account for the prior entries and the DF-00 recovery
rows assigned to DF-10, including topics from deleted DI/tips pages and shortened navigation,
Firebase, getting-started and sync references.

| Prior entries / recovery topics | Final disposition |
|---|---|
| JDK; dependency resolution; historical Gradle sync/catalog setup | Corrected JVM/toolchain diagnosis; differentiated aliases, coordinates, offline mode and network errors; targeted retry instead of blanket refresh. |
| Missing binding; deleted DI troubleshooting (bindings, fields, scopes, KSP, cycles) | Expanded qualifier/component/module visibility and duplicate/cycle checks; restored processor-first Hilt/Room diagnosis in a separate KSP entry. Existing DI guide owns field/construction examples. |
| Duplicate classes; configuration cache | Added executable dependencyInsight example; report-led cache diagnosis with official Gradle reference, no unsupported single-plugin attribution. |
| Historical slow build/processor failures | Restored local profiling, phase comparison and memory checks; no automatic scan upload or cache invalidation prescription. |
| Firebase initialization; Hilt startup | Retained and corrected variant, manifest, initialization and annotation checks; linked canonical setup. |
| Destination/arguments; historical navigation serialization, back stack, restoration, chrome | Corrected NavKey/entry/assisted-factory checks and restored a separate identity/restoration entry; linked current wiring instead of obsolete nested graphs. |
| Stale UI; repeated events; helper compilation | Corrected observation/equality/lifecycle and Result/context checks; documented event consumption versus successful delivery. |
| Historical loading, lifecycle, state reset and effect restarts | Restored loading/ignored/save-failure entry; distributed composition identity, process restoration and collector-lifetime diagnosis across navigation, UI and event entries. |
| Preview; jank/list/recomposition | Removed private/ordinary Preview failure myths and Route requirement; linked preview-safe content and measured performance rather than universal annotations/lambdas fixes. |
| Historical emulator/install failures | Restored device inventory, explicit device selection and installation-error diagnosis without default uninstall/data loss. |
| Google sign-in; Credential Manager | Corrected certificate/client/provider/account-filter diagnosis; linked configured sign-in/registration behavior. |
| Firestore permission denied; historical missing documents | Added deployed-rule/path/UID/field checks and old timestamp-field compatibility; no permissive-rules shortcut. |
| Crashlytics; historical duplicate initialization and reporting | Corrected test/restart/upload/mapping diagnosis; initialization stays in its own entry, instrumentation scope stays in Firebase guide. |
| Sync never runs; repeated retries; notification; customization/conflicts | Separated queue/constraints/KEEP, actual failures, delivery gaps and notification visibility/foreground setup. Corrected four-attempt limit; replaced clock-conflict and device-sqlite assumptions with established limitations and official diagnostics. Customization remains explicitly instructional. |
| Spotless; historical headers/ktlint/CI; new guardrails | Retained exact init-script flags, added baseline/badging/custom-lint diagnosis and reviewed-regeneration links. |
| Signing fallback; R8 | Retained targeted release diagnosis and signing verification; keep-rule edits remain subject to review. |
| Repository crash; Room integrity | Corrected Result versus Flow/throw handling; distinguished same-version integrity, migration paths, destructive loss and missing upgrade tests. |
| LeakCanary; image OOM; deleted tips leak workflow | Restored retained-object trace/reproduction/recheck workflow; corrected Context lifetime and image-loader assumptions. Performance duplicates merged into existing UI entry. |
| How to run tests; additional help | Replaced Cause: N/A with links to existing command/testing guides and shared-Flow testing caveat; replaced universal clean/build advice with reproducible diagnostic collection. |

#### Verification

| Check | Result and limits |
|---|---|
| Source/configuration review | Checked current worker/request/notification creation, permission request, JVM criteria, image painter and repaired canonical guides. Existing generated debug and release merged manifests contain SystemForegroundService without a service type; these artifacts were inspected, not freshly rebuilt. |
| Upstream contracts | Checked official Android long-running WorkManager, notification-permission and WorkManager diagnostics pages, plus Gradle configuration-cache debugging. Linked those contracts at the relevant instructions. |
| `./gradlew dokkaGeneratePublicationHtml` | Passed; 276 actionable tasks, configuration cache reused. |
| Documented local assembly and `.venv/bin/python -m mkdocs build --strict` | Passed after final summary markup change. Existing index-omitted-from-nav informational notice remains; no strict warnings/errors. Generated output is ignored. |
| Rendered local links/anchors | Checked all 88 local hrefs in generated troubleshooting HTML against existing output and target fragment IDs; 34 details/summary pairs. |
| GitHub Markdown compatibility | Temporary cmarkgfm renderer installed under `/private/tmp/df10-render-deps`, with no project dependency change. Verified 34 balanced details/summary pairs, nested emphasis, links, fenced code and caution blockquote. Changed summary backticks to explicit HTML code tags after GFM exposed literal-backtick rendering. GitHub-specific alert styling/sanitization was not simulated and no page was published. |
| Chrome rendering | Inspected local MkDocs page and expanded sync diagnostics/code and release caution. Also inspected standalone GFM output summaries. No app UI/device execution claimed. |
| `./gradlew --version` | Passed; documented JVM diagnostic exercised. |
| `./gradlew :app:dependencyInsight --configuration debugRuntimeClasspath --dependency kotlin-stdlib` | Passed; documented configuration/dependency diagnostic exercised. |
| `adb devices -l` | Command succeeded after sandbox cache/socket access was granted; no devices attached. Broadcast, jobscheduler, install, notification and Inspector scenarios are instructions checked against official docs, not device results. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` | Passed; 180 tasks up-to-date. No formatter apply or commit performed. |
| `git diff --check` | Passed. Only the two intended authored Markdown files changed. |

Local logs: `/private/tmp/df10-{dokka,mkdocs,version,dependency,spotless}.log`. Initial Gradle wrapper
lock, localhost serving, ADB socket and temporary renderer download attempts were sandbox-blocked;
rerunning with granted access succeeded. No protected credential/configuration files were opened.
No new runtime tests were added for this documentation-only change.

#### Visible limitations and follow-ups

- **Foreground sync configuration:** source-confirmed missing runtime service type in ForegroundInfo;
  inspected merged manifests also lack a SystemForegroundService type and dataSync-specific permission.
  A separately reviewed fix must align manifest service type/permission and runtime type, then verify
  affected API levels, notification denied/granted states and worker results on a device/emulator.
  This task documents the gap; it does not establish a device reproduction or change configuration.
- Existing sync consistency, pull checkpoints, event delivery, observation lifetime, Item error handling
  and worker cancellation concerns retain their DF-02 dispositions. Successful work does not imply
  complete delivery or persistence.
- Device/Firebase/rules/R8/Room-upgrade/Android Studio preview and memory scenarios were not executed.
  Site-wide source-link/URL compatibility remains DF-14; module README reconciliation is next, DF-11.

## DF-11 — Reconcile all module READMEs and API entry points

Depends on: DF-03–10. Scope: nineteen module READMEs, Dokka output.

- [x] Preserve the leading `# Module :...` syntax used by Dokka. Keep a purpose statement, important
  entry points, enough usage context, and links; choose structure by module needs, not a fixed cap.
- [x] Review `app`, `core/android`, `core/navigation`, `core/network`, `core/preferences`, `core/room`,
  `core/testing`, and `core/ui`; reconcile all findings with their repaired guide sections.
- [x] Review `data` and `sync`: include all key repository roles, correct the claim that `sync`
  implements `Syncable` itself, and remove unsupported local-write/scheduling assurances.
- [x] Review `feature/auth`, `feature/home`, `feature/profile`, `feature/settings`: remove nonexistent
  undo and Route wrappers; check settings/sign-out and actual repository boundaries.
- [x] Review `firebase/auth`, `firebase/firestore`, `firebase/analytics`: accurate auth filter,
  timestamp, rule deployment, and Crashlytics guidance without duplicated defensive explanations.
- [x] Review `benchmarks` and `lint`: preserve practical commands, remove invented adopter behavior
  (“first hour”), and distinguish the actual startup scenario from unsupported CI/runtime promises.
- [x] Replace `core/testing`'s “does the thing” example with a useful real test; remove “a mock can't
  express it” and stale consuming-module lists. Explain how consumers add and use the helpers.
- [x] Verify API names and source pointers. In particular, `core/network`'s enum inventory must not
  imply `LOSING` is emitted when the implementation only logs it.
- [x] Audit repository-relative links and rendered Dokka destinations. Review completed; actual
  README rendering is absent and publication acceptance is deferred to DF-14 by user choice.
  See the completion record; working GitHub links do not establish working Dokka introductions.

Accept: all nineteen module references have a recorded review; each helps a reader use the module
and reaches accurate deeper guidance from both GitHub and Dokka. **The publication portion of this
acceptance criterion is deferred to DF-14 by user choice; the README review is complete.**

### DF-11 completion record

Completed 2026-10-17 from clean working-tree revision `fa19cbbd83ae609dcc0886a1b6b37379e91a12cc`.
Changed the nineteen module READMEs below and this execution record only. No runtime, public API,
type, dependency, source-comment, convention-plugin, build or backend changes; no commit or deployment.

The user chose **document and defer** for missing Dokka publication coverage. This completes the
README/editorial review, not the original end-to-end publication acceptance claim. Actual generated
API coverage and README inclusion remain incomplete as described below; DF-14 owns their repair.

#### Nineteen-module review and disposition

All nineteen retain their `# Module :...` heading. Existing useful descriptions/snippets were
checked against the corresponding implementation, build declarations and repaired guide sections;
previously corrected content was retained. Changed paths are this table's module directories plus
`README.md`, and `DOC_FIX_TASKS.md`.

| Module directory | Reviewed content and final disposition |
|---|---|
| `app` | Checked MainActivity, JetpackAppState and version calculation; qualified wiring description, labeled excerpts, retained AppCompat/locales and debug-signing caveat. |
| `core/android` | Checked event/coroutine/Flow helpers; replaced event-delivery promise with atomic consumption, clarified sharing versus separate-job lifetime. |
| `core/navigation` | Checked Navigator/NavigationState and repaired identity guide; removed enforced-encapsulation and key-type collision claims, described equality versus string content keys. |
| `core/network` | Checked REST API/data source, monitor and image-loader bindings; corrected emitted-state inventory, capability/availability boundaries and shared app use. |
| `core/preferences` | Checked model, source, serializer and DataStore construction; retained accurate JSON corruption/default/evolution/reset guidance from prior repair; updated deeper links. |
| `core/room` | Checked entity, DAO and persistence guide; retained pending flags, clock units, tombstones, cursor and destructive-upgrade limitations; updated deeper links. |
| `core/testing` | Checked rule/fakes, exported dependencies and SettingsRepositoryImplTest; added consumer setup and complete success/failure example; removed empty example, stale consumers and mock restrictions. |
| `core/ui` | Checked helper/component/permission ownership against guides; clarified lint execution and state-helper exception/concurrency/event limits; retained screen/content preview guidance. |
| `data` | Checked all four repository roles, binding module and sync contracts; added auth role and getJetpack interface member, separated SyncManager implementation from Syncable repository execution. |
| `sync` | Checked manager, proxy, worker constraints/retries and diagnostics; clarified Hilt factory delegation and attempted foreground execution, retained scheduling/cancellation limits and exposed foreground configuration gap. |
| `feature/auth` | Checked screen signature, ViewModels and AuthRepository boundary; made the repository/data-source relationship explicit and labeled the signature excerpt. |
| `feature/home` | Checked list/item signatures and repaired state guide; removed undo claim, labeled signature, exposed save/load and observation-lifetime limitations. |
| `feature/profile` | Checked screen/ViewModel and ProfileRepository; clarified auth-before-reset order, Result boundary and read-only UI; no Room/remote erasure promise. |
| `feature/settings` | Checked dialog/ViewModel and SettingsRepository; clarified DataStore versus locales, sign-out/reset scope, observation/load limitations and full localization path. |
| `firebase/auth` | Checked interface/request builders and repaired Firebase guide; distinguished Firebase Auth from Credential Manager, labeled private excerpt and removed account-existence guarantee. |
| `firebase/firestore` | Checked model/query/write source and data guide; retained null-on-write timestamps, SDK visibility, rules/legacy/query limitations; qualified current Home integration. |
| `firebase/analytics` | Checked CrashReporter/FirebaseCrashReporter and guide; distinguished non-fatal calls, uncaught crashes and optional UI report action from analytics events. |
| `benchmarks` | Checked generator, benchmark modes and build/CI setup; removed performance/CI/adopter assurances, retained startup sequence and added class-filtered comparison command and environment/output guidance. |
| `lint` | Checked registry, detector implementation and core:ui publication; removed stale line number and historical naming rationale, added actual prefix behavior and :lint:test command. |

#### Links and reading surfaces

Cross-file guide/source links use absolute GitHub permalinks at the reviewed revision above, rather
than repository-relative paths that would resolve inside the generated API directory. Module-topic
links lead to their repaired canonical guides instead of older module prose at that revision.
Checked all **72** permalink occurrences against local Git objects and rendered heading slugs:
all files/fragments exist. This validates the exact repository content, not remote HTTP availability.
DF-14 must reconcile these preview/review links with the final release strategy.

Generated GitHub-flavored Markdown with the previously installed temporary cmarkgfm renderer under
`/private/tmp/df10-render-deps`; no project dependency added. All nineteen pages render tables,
code fences and links. Chrome visual review covered testing, data, Home, Firebase Auth, benchmarks
and lint. This is local GFM output, not proof of GitHub-specific sanitization/alert styling.
Also inspected actual generated Dokka network landing output and the MkDocs state-management page.

#### Verification

| Check | Result and limits |
|---|---|
| `./gradlew dokkaGeneratePublicationHtml :data:testDebugUnitTest --tests dev.atick.data.repository.settings.SettingsRepositoryImplTest` | Passed; seven existing repository tests, zero failures/errors/skips. Dokka was up-to-date; this alone did not validate README rendering. |
| `./gradlew :core:network:dokkaGenerateModuleHtml --rerun :dokkaGeneratePublicationHtml --rerun` | Passed with a forced network module and root publication render. Thirteen unresolved source KDoc-link warnings; README introduction still absent. Other module generators were not forced. |
| Documented README/API preparation, then `.venv/bin/python -m mkdocs build --strict` | Passed using actual Dokka output after the forced render. Existing informational index-omitted-from-nav notice remains. No dummy API pages. |
| `.venv/bin/python /private/tmp/df11_verify.py` | Passed: nineteen module headings preserved and 72 file/fragment permalink checks. Temporary preview HTML generated outside the repo. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` | Passed; 180 tasks up-to-date. No formatter apply or commit performed. |
| `./gradlew check` | Passed; 965 tasks, 18 executed, one from cache, 946 up-to-date. |
| `git diff --check`, changed-path inspection and `git check-ignore` | Passed; only nineteen module READMEs and this record changed. Generated homepage/API/site and local venv remain ignored. |

The worked test class is a complete adaptation of the two existing reference test cases; its real
counterparts were executed, not a separately added example class. No runtime tests were added.
Device/Firebase/benchmark execution is not claimed; commands were checked against current setup
and the previously repaired build guide. No protected credential files were opened.

Logs/scripts/previews: `/private/tmp/df11-*`. Initial Gradle cache access and localhost serving were
sandbox-blocked; reruns with granted access succeeded. Before a later commit, run the required
Spotless apply command, inspect any formatter changes and repeat applicable gates if code changes.

#### Explicit publication and source follow-ups

- **DF-14 — Missing aggregate modules:** root Dokka dependencies omit `core/navigation`,
  `core/testing`, `benchmarks` and `lint`. The first two already apply the Dokka convention;
  benchmarks/lint do not. Root API output contains fifteen module landing pages, not nineteen.
  Publication/configuration changes remain deferred by the user's scope choice.
- **DF-14 — Missing README content:** none of those fifteen aggregate landing pages contains its
  README's Related Documentation section. A forced network render also has no README introduction.
  The convention attaches includes only when the Dokka source set is named `main`, while generated
  network output identifies `release`. Review source-set inclusion and Gradle input tracking before
  claiming module README links were exercised in real Dokka HTML. Absolute links address path
  relocation; they do not make omitted README content appear. Do not change the convention plugin
  without the required review.
- **DF-12/14 — Unresolved network KDoc:** the forced render reported thirteen occurrences involving
  `suspendRunCatching`, `IOException`, `HttpException`, `withContext`, `ConnectivityManager`, its
  `NetworkCallback`, `StateFlow` and `Flow.distinctUntilChanged`. Source comments are unchanged;
  reconcile their references under DF-12 and recheck generated links under DF-14. A successful
  Gradle or strict MkDocs build does not establish warning-free API documentation.
- Existing runtime limitations retain their DF-02/DF-10 dispositions. This task explains them;
  it does not repair sync consistency, foreground configuration, event delivery or ViewModel lifetime.

## DF-12 — Edit every source comment touched by the PR

Depends on: DF-01–02 and the relevant guide repairs. Scope: sites below plus their enclosing KDoc.

Perform an editorial and factual pass at every listed site. Retain licenses, valid contracts, useful
issue links and non-obvious constraints. Edit prose only here; protected rule/build changes and
runtime fixes belong to separately scoped work.

- [x] `app/proguard-rules.pro`: remove unsupported intent; state actual keep scope; verify the linked
  workaround against current Navigation/R8 before retaining a necessity claim. Keep rule tokens intact.
- [x] `app/build.gradle.kts`: concise signing consequence/action; remove the removed-placeholder
  story while retaining useful issue tracking for custom APK naming.
- [x] `gradle/init.gradle.kts`: explain invocation and observed scope, not hypothetical reasons for
  the architecture. Correct the “every Gradle invocation” claim; review adjacent stale TODO wording.
- [x] `build-logic/.../AndroidCompose.kt`: explain that missing/false flags leave report destinations
  unset; avoid the long provider/null lecture. Follow the repo's convention-plugin boundary.
- [x] `firebase/auth/consumer-rules.pro`: verify discovery from SDK source; do not assume
  `ServiceLoader`. Explain the actual kept provider classes and condition without changing rules.
- [x] `firebase/auth/.../AuthDataSourceImpl.kt`: verify the lint issue's actual meaning and error
  handling, then shorten the suppression/filter explanations and redundant cross-function comment.
- [x] `core/ui/.../theme/Background.kt`, `Gradient.kt`, `Tint.kt`: fix `LocalGradientTheme` to the
  actual `LocalGradientColors` where appropriate; verify any recommended rule-setting syntax.
  Explain the present suppression without implementing a Spotless/EditorConfig change here.
- [x] `core/ui/.../utils/StatefulComposable.kt`: reconcile enclosing KDoc and body comments with
  DF-07, especially effect keys, consumption, thrown exceptions, and concurrent-update limits.
- [x] `feature/home/.../HomeViewModel.kt`, `ItemViewModel.kt`, `ItemScreen.kt`, and
  `feature/settings/.../SettingsViewModel.kt`: concise lifecycle/input/locale guidance; remove
  “most-copied idiom,” obsolete Route KDoc, and unsupported parser examples.
- [x] `core/network/.../NetworkUtils.kt` and `NetworkUtilsImpl.kt`: state initial emission and
  subsequent callback behavior; remove the overbroad “only reports transitions” assertion and long
  retelling of the fixed startup bug. Distinguish available network from validated Internet access.
- [x] `core/ui/.../components/SwipeToDismiss.kt`: explain the deletion side effect and transition
  lifetime briefly; preserve the fix and its regression test.
- [x] `data/.../HomeRepositoryImpl.kt`, `data/.../model/home/Jetpack.kt`: local sync-request timing,
  field mapping, server cursor, and echo behavior consistent with DF-02/06. Avoid universal claims
  that an echo cannot change content.
- [x] `core/room/.../JetpackDao.kt`, `JetpackDatabase.kt`, `LocalDataSource.kt`,
  `LocalDataSourceImpl.kt`, `JetpackEntity.kt`: check query, units, null/default cursor, tombstones,
  and schema documentation. Move repeated clock-skew narratives into one guide explanation.
- [x] `firebase/firestore/.../FirebaseDataSource.kt`, `FirebaseDataSourceImpl.kt`,
  `model/FirebaseJetpack.kt`: retain null-on-write server timestamp behavior and the constructor
  visibility constraint succinctly. Clarify what nanosecond conversion preserves without repeating
  “silently and permanently” or overstating first-pull coverage of old documents.
- [x] `sync/.../Sync.kt`, `SyncManagerImpl.kt`, `SyncWorker.kt`: remove unjustified safety assurances,
  reconcile cross-links, and shorten retry/backoff documentation to the relevant contract.
- [x] Check comment/KDoc consistency in the five changed test files: `NetworkUtilsImplTest`,
  `JetpackDaoTest`, `SwipeToDismissTest`, `HomeRepositoryImplTest`, `ItemViewModelTest`. Preserve
  meaningful setup explanations and regression intent; remove claims beyond their assertions.
- [x] Review adjacent surviving boilerplate only where it contradicts the edited contract. Verify
  the removed `DarkThemeConfigSerializer` leaves no instructions telling readers to use it.
- [x] Compare the final source diff to the pre-repair head and confirm editorial commits contain
  no unintended Kotlin, Gradle, R8, annotation, SQL, or test-behavior changes.

Accept: every PR-touched source site has a disposition; comments help with behavior or maintenance,
and neither invented intent nor documented bugs are presented as architectural guarantees.

### DF-12 completion record

Completed 2026-10-17 on `fix/docs-v3`, starting from
`f06290a8b6fb2f65cafc592f952897d0f8a82bc1` with a clean working tree. This record covers the
uncommitted editorial patch; no commit, push, deployment, or external update was made.

#### Coverage and dispositions

Compared the pinned PR base `1c2103890d16f81f6c6ae4c2d8626281b841e417` with the starting HEAD:
46 Kotlin, Gradle, and rule files. Reviewed each file's surviving comments and enclosing contracts;
33 required edits, and 13 retained the already-correct guidance or contained no further substantive
comment changes. Two adjacent interfaces required corrections. License headers are unchanged.

Paths below use their module's `src/main/kotlin/dev/atick/` package root unless a full path or test
root is specified. Every file in the 46-file inventory has a disposition here.

| Area / files | Disposition |
|---|---|
| `app/build.gradle.kts`, `app/proguard-rules.pro` | Shortened signing consequences and removed placeholder history; described actual serialization keep scope without claiming the historical issue proves current necessity. Preserved rule directives. |
| `gradle/init.gradle.kts`, `build-logic/convention/src/main/kotlin/dev/atick/AndroidCompose.kt` | Corrected explicit invocation, main-build formatting scope and included-build header limitation; explained unset report destinations. Comments only, including the convention helper. |
| `firebase/auth/consumer-rules.pro`, `firebase/auth/data/AuthDataSourceImpl.kt` | Replaced ServiceLoader claim with manifest-metadata/reflection discovery; documented broad keep scope and the SDK's narrower rule. Corrected lint's project-wide NoCredentialException-use check and account filtering. |
| `core/ui/theme/Background.kt`, `Gradient.kt`, `Tint.kt` | Explained the current file suppressions using the declared local names, including LocalGradientColors. Removed proposed EditorConfig syntax rather than adding a formatting policy change. |
| `core/network/utils/NetworkUtils.kt`, `NetworkUtilsImpl.kt` | Documented the initial capability sample, sample/registration race, emitted/logged callbacks, cleanup and duplicate suppression. Removed startup-bug history and backend-reachability assurances. |
| `core/android/utils/OneTimeEvent.kt`, `core/ui/utils/StatefulComposable.kt`, `components/SwipeToDismiss.kt` | Shortened redundant body comments, retained established consumption/exception/concurrency limits, and explained callback freshness and dismissal effects without delivery guarantees. |
| `core/room/data/JetpackDao.kt`, `LocalDataSource.kt`, `LocalDataSourceImpl.kt` | Corrected acknowledgement/version wording and the false row-ID return documentation; replaced fictional repository examples with current local-storage responsibilities. Retained server-cursor units, deleted-row inclusion and null-to-zero behavior. |
| `data/model/home/Jetpack.kt`, `repository/home/HomeRepositoryImpl.kt` | Shortened construction-time sync-request and server-clock explanations; retained null-on-write mapping, pending-write risks and possible content changes from upload echoes. |
| `feature/home/ui/item/ItemScreen.kt`, `feature/settings/ui/SettingsViewModel.kt` | Clarified assisted argument wiring, immediate upstream restart, surviving collectors, locale mapping and the unused load-error state. |
| `firebase/firestore/data/FirebaseDataSource.kt`, `FirebaseDataSourceImpl.kt`, `model/FirebaseJetpack.kt` | Replaced obsolete cursor/merge/deletion guidance and fictional APIs with the actual path, strict server boundary, default-source cache limitation, generated-ID add versus ID-addressed set, hard deletes and null-only ID check. Corrected device-timestamp semantics and shortened constructor/precision constraints. |
| `sync/manager/Sync.kt`, `SyncManagerImpl.kt`, `worker/SyncWorker.kt` | Retained the repaired KEEP, retry and cancellation contracts; shortened repeated work-name/constraint guidance and removed the incorrect WiFi-only interpretation of UNMETERED. |
| Tests under each module's `src/test/kotlin/dev/atick/`: `core/network/utils/NetworkUtilsImplTest.kt`, `core/room/data/JetpackDaoTest.kt`, `core/ui/components/SwipeToDismissTest.kt`, `data/repository/home/HomeRepositoryImplTest.kt`, `feature/home/ui/item/ItemViewModelTest.kt`, `feature/settings/ui/SettingsViewModelTest.kt` | Corrected overbroad regression/fake claims and retained meaningful setup explanations. Assertions and test behavior are unchanged. Network tests cover initial samples, not duplicate suppression or callback ordering; Settings state tests do not render snackbars. |
| `core/android/extensions/FlowExtensions.kt`; `core/preferences/model/UserDataPreferences.kt`, `utils/UserDataSerializer.kt` | Retained prior repairs: sharing versus separately launched jobs, JSON model defaults, and corruption propagation without a handler. No DarkThemeConfigSerializer usage instructions remain. |
| `core/room/data/JetpackDatabase.kt`, `di/DatabaseModule.kt`, `model/JetpackEntity.kt` | Retained the actual schema declaration, destructive-fallback limits, field units, pending metadata and deleted-row lifetime. |
| `data/build.gradle.kts`, `data/utils/SyncUtils.kt`, `feature/home/build.gradle.kts` | Retained test-classpath explanation, cold-sync/progress contracts and existing grouping comments. |
| `feature/home/ui/home/HomeViewModel.kt`, `ui/item/ItemViewModel.kt` | Retained the earlier lifecycle, raw-input, ignored-save-Result, unused-load-error and event-consumption repairs. |
| `core/ui/src/test/kotlin/dev/atick/core/ui/utils/StatefulComposableEventTest.kt`, `UiStateContractTest.kt` | Retained characterization coverage and the supervised-scope setup explanation. No assertions or behavior changed. |
| Adjacent: `core/network/data/NetworkDataSource.kt` | Qualified unresolved KDoc links/exception types without adding imports; labeled the repository/mapping example as an adopter extension. |
| Adjacent: `core/preferences/data/UserPreferencesDataSource.kt` | Replaced contradictory Proto DataStore and direct-ViewModel examples with the JSON storage contract, repository boundary, error propagation and stored-profile-versus-auth distinction. Removed the unresolved withContext reference. |

#### Factual evidence and unresolved necessity

- Credentials discovery was checked in the exact `androidx.credentials:credentials:1.6.0` source
  archive's `CredentialProviderFactory.kt`: service metadata under CREDENTIAL_PROVIDER_KEY supplies
  class names, followed by Class.forName and a Context constructor. The resolved play-services AAR
  manifest names CredentialProviderPlayServicesImpl; its consumer rules keep that class, while this
  repository's rule keeps the entire playservices package. No shrinker-removal diagnosis is claimed.
- Checked CredentialManagerMisuse in resolved lint-checks 32.3.1 and its
  [official check documentation](https://googlesamples.github.io/android-custom-lint-rules/checks/CredentialManagerMisuse.md.html).
  It looks for NoCredentialException use across the project, rather than requiring a nearby catch.
  Repository wrappers still provide generic Result handling, without a distinct no-credential path.
- Checked filtering against Google's
  [GetGoogleIdOption.Builder contract](https://developers.google.com/identity/android-credential-manager/android/reference/com/google/android/libraries/identity/googleid/GetGoogleIdOption.Builder).
  Authorization filtering does not establish whether the Firebase user exists.
- The historical [navigation issue](https://issuetracker.google.com/issues/353898971) did not expose
  usable issue content during this review. Necessity with Navigation 3 1.1.4/current R8 remains
  unverified and is explicitly labeled in the comment. No rule removal or narrowing is proposed.

#### Verification

Validated the final source patch against starting HEAD. Source-only `git diff -- '*.kt' '*.kts'
'*.pro'` SHA-256: `aba9c9e707fe5144dbfc94d15a11158011175e3ca43f1147dd1fc088bbf94ee4`.
The completion record itself was added afterward.

| Check | Result |
|---|---|
| Kotlin-aware executable-token comparison | Passed for all 33 changed Kotlin/Gradle files using Kotlin 2.4.10's KotlinLexer, excluding only whitespace/comment tokens. This preserves string literals, annotations, SQL and test assertions. An initial overbroad edit was caught and restored before final validation. |
| R8 directive comparison | Passed for both changed rule files, excluding only comments/blank lines. |
| `git diff --check` | Passed. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache check dokkaGeneratePublicationHtml` | BUILD SUCCESSFUL, 23 seconds, 1,417 tasks: 124 executed, 3 from cache, 1,290 up-to-date. Final network and preferences module/publication HTML tasks executed; this was not just cached API output. |
| Generated API HTML | Inspected eight aggregate pages: network state/data-source reference, Firestore pull/add/delete, nanosecond conversion, local cursor and UiState. Checked revised prose and all 80 local link targets; all exist. This is HTML/content/link inspection, not a browser visual review. |
| Obsolete-symbol search | No DarkThemeConfigSerializer, LocalGradientTheme or ServiceLoader instructions remain in the reviewed preferences/theme/auth source sites. |

The final incremental run reported no unresolved-link warnings. The earlier broader generation did
report warnings in untouched files: CoroutineModule/CoroutineUtils/ThrowableExtensions,
Button/Tabs/TextField/ActivityExtensions/Theme, AuthDataSource, Settings and HomeRepository.
Those warnings remain publication follow-ups under DF-14; cached tasks do not establish their
resolution. The edited network references resolved during both generated module and publication
passes. No blanket warning-free API-site claim is made.

The existing DF-11 aggregate-module/README-inclusion gaps remain DF-14 work. Runtime limitations
retain DF-02's dispositions. No MkDocs/site assembly, device, Firebase or release validation was
needed for these source-only edits. Spotless apply remains required before a later commit; it was
not run as part of this uncommitted editorial change. Root formatting success does not establish
included-build formatting coverage.

Logs, the SDK source archive, token-audit helper and source inventory/dispositions are under
`/private/tmp/df12-*`. Initial Gradle lock-file access and SDK download were sandbox-blocked;
reruns with granted access succeeded.

## DF-13 — Reconcile root guidance, changelog, and review history

Depends on: DF-03–12. Scope: `README.md`, `AGENTS.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, old plan/task files.

- [x] Make root README a useful starting map: setup, feature walkthrough, reference, testing,
  customization, releases, and local docs preview. Preserve project-specific voice and presentation.
- [x] Fix `AGENTS.md`'s module diagram/rules to reflect actual declared dependencies, including
  `core:navigation`, `core:testing`, Firebase → core, and sync → data. Keep intended boundaries clear.
- [x] Remove the stale claim that `app/build.gradle.kts` still contains a no-op `onVariants` block.
  Reconcile state/error helper guidance, Firebase terminology, and all command flags with the guides.
- [x] Review inherited contribution workflow advice such as targeting a “development” branch against
  the current project workflow; avoid changing unrelated author preferences without evidence.
- [x] Rework the new changelog prose around reader-visible changes and compatibility effects. Make
  schema/data loss and backend field/rule changes explicit; remove test counts and long bug narratives.
- [x] Ensure material runtime fixes in this PR have appropriate release-note coverage. Distinguish
  newly recorded earlier work from runtime changes actually introduced by this branch.
- [x] Keep previous plan/task logs as dated history with a pointer to the new work. Correct active
  “complete/all examples verified” implications without erasing historical verification records.

Accept: root instructions agree with the guides and code; release notes disclose actual upgrade
implications; readers can tell which plan is active.

### DF-13 completion record

Completed 2026-10-17 from clean starting HEAD `a17a7e99e` on `fix/docs-v3`.
This is an uncommitted editorial patch. Production code, public APIs, dependencies, configuration,
backend rules, and workflow files are unchanged.

Changed `README.md`, `AGENTS.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, `PLAN.md`, `TASKS.md`, and this
execution record:

- README now describes a starting template, links release/upgrade and runtime limitations, exposes
  feature testing and customization/localization reading paths, repairs library-label formatting,
  replaces unsupported Firebase Performance coverage with Crashlytics, and closes the footer paragraph.
  Existing branding, images, badges, quotation, and project links remain.
- Agent guidance now follows the declared Gradle module edges, including navigation API exposure,
  Firebase/core relationships, sync's dependency on data, and test-only support. It states returned
  Result handling and exception/overlap/collector/event limitations, fixes Spotless flags and discovery
  scope, removes the no-op variant-block claim, and links Credential Manager's local client-ID setup.
  Existing approval boundaries are preserved.
- Contribution guidance targets `main`, evidenced by local `origin/HEAD → origin/main` and the
  deployment workflow's main trigger. The issue-first process is retained. Full Spotless flags and
  the pre-commit `check` gate agree with the build guide and repository instructions. No remote
  default-branch query or contribution-policy change was performed.
- Unreleased release notes distinguish previously unrecorded navigation/tooling/AGP work from this
  branch's behavior changes. They cover swipe deletion, initial connectivity, price editing,
  server-clock sync, both affected serialization APIs, and variant-block removal. Upgrade notes
  explicitly disclose Room data loss, missing-field document exclusion, old-client rule rejection,
  and the need for a separately planned migration/backfill/client-rules rollout.
- Historical plan/task introductions retain original checkmarks, counts, and verification evidence
  while dating the original completion to 2026-08-29, confirmed by commit `0fb788298`. They point to
  the active repair queue and do not present old results as current validation.

#### Evidence and verification

Dependency statements were compared with module `build.gradle.kts` declarations and the canonical
architecture dependency table. Helper and connectivity claims were checked against current source;
storage/backend compatibility agrees with the data and Firebase guides and DF-02's behavior ledger.
Release-note provenance was checked against `git log 1c210389..HEAD`, the base changelog, and the
model/schema/source changes. The base changelog's Unreleased section was empty; navigation, tooling,
and AGP were already implemented at the audit base, whereas the behavior changes above belong to
this branch. Earlier released changelog entries are unchanged.

| Check | Result |
|---|---|
| `git diff --check` | Passed. |
| Local root-document links and fragments | All 58 local links in the six edited root pages resolve; fragment targets checked against rendered Markdown headings. External URL availability was not audited. |
| `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache` | BUILD SUCCESSFUL in 6 seconds; 180 tasks up-to-date. No formatter edits. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache check` | BUILD SUCCESSFUL in 7 seconds; 1,141 tasks, 18 executed and 1,123 up-to-date. Included-build formatting coverage is not inferred from this root invocation. |
| Browser Markdown preview | Inspected README's reading map and repaired library labels, and changelog's removal/upgrade sections. Headings, lists, bold labels, inline code, and links render legibly. These are isolated Python-Markdown previews with local CSS, not GitHub or MkDocs theme validation. |
| Changed-path review | Only the seven Markdown files listed above; no generated site/API output, credentials, or production-source changes. No tests added for prose edits. |

Full GitHub/MkDocs/Dokka rendering, source-link reconciliation, old-URL compatibility, and CI
validation remain DF-14. The browser preview showed the existing external license badge displaying
`INVALID`; external badge/image availability remains a publication-check follow-up there. Runtime
and upgrade limitations retain DF-02's bounded follow-ups; no migration, backfill, or rule deployment
was attempted. No commit, push, PR update, or site publication was performed.

Validation logs, the link-check/render helper, and preview HTML are under `/private/tmp/df13-*`.
Gradle cache locking and the loopback preview server were initially sandbox-blocked; reruns with
granted access succeeded.

## DF-14 — Repair site assembly, links, and validation

Depends on: final page destinations from DF-03–13. Scope: `mkdocs.yml`, `.github/workflows/docs.yml`,
documentation links, generated-output handling in `.gitignore`; PR validation workflow if needed.

- [x] Build the site from a clean checkout using DF-04's documented process, including the generated
  README homepage and real Dokka API output. Do not use dummy API files to obtain a strict pass.
- [x] Confirm the generated homepage is reachable at the site root and navigable from every page;
  decide whether Home needs an explicit nav entry. Check the README link transformation does not
  damage external URLs or anchors.
- [x] Audit local links, headings/anchors, source paths, API links, images, and section links across
  all root/module/docs files. Resolve the duplicate `pymdownx.superfences` entry if confirmed redundant.
- [x] Map every deleted public page URL and relevant old anchor to its replacement. Implement
  lightweight compatibility pages/anchors where feasible with existing tooling; record any
  unavoidable break explicitly. Do not add a redirects dependency without approval.
- [x] Check branch-specific source/README links that point to `main`: before merge they can show
  incompatible content. Choose a deliberate preview/release link strategy and verify it at both stages.
- [x] Review module README links in generated Dokka, KDoc unresolved references, generated asset
  paths, and the API nav entry. A passing MkDocs build is only one part of this check.
  Include DF-11’s recorded missing aggregate modules, absent README introductions/source-set
  inclusion, thirteen network KDoc-link warnings, and pinned review-link release reconciliation.
- [x] Inspect representative GitHub and MkDocs rendering: tables, Mermaid, Kotlin fences, alerts,
  details/summary contents, narrow-screen layout, and link destinations. Render claims require
  an actual rendering check, not inference from syntax alone.
- [x] Add or wire strict docs validation for PRs and before deployment using the same preparation
  steps as local preview. Keep deployment separate from validation; avoid a workflow that ignores
  docs/config-only changes. Explain any remaining checks that require a human.
- [x] Keep `docs/index.md`, `docs/api/`, `site/`, and local environment artifacts out of commits;
  confirm `.gitignore` covers the chosen process without hiding authored recovery pages.

Accept: the site builds with real inputs, old navigation has a recorded outcome, docs changes are
validated before merge, and both guide and API reading surfaces work.

### DF-14 completion record

Completed 2026-10-18 from clean starting HEAD `fa1d26001d9d7f8bf30af50a78f056cf99f82994`
on `fix/docs-v3`. This is an uncommitted documentation/publication patch. The agreed scope includes
the Dokka convention repair, seventeen aggregate API modules, and separate benchmark/lint guide
references. No runtime APIs, application dependencies, SDK/toolchain versions, Firebase configuration,
signing material, database policy, or Spotless configuration changed.

#### Publication changes

- `scripts/prepare_docs.py` assembles real Dokka output, authored guides, the README homepage and
  two tooling references into disposable `build/docs-source/`; `build/docs-mkdocs.yml` inherits the
  authored configuration. Preparation rejects missing API output, removes stale assembled output,
  preserves external destinations/fragments, and keeps published API links inside the preview.
  The local-preview guide and CI use this same process. Legacy ignored `docs/index.md` and
  `docs/api/` are no longer inputs. Build/site/environment outputs and Python caches remain ignored;
  the thirteen authored compatibility pages are not ignored.
- Root Dokka aggregation now includes navigation and testing. The convention includes each README
  in actual Android source sets, with Gradle-tracked file inputs, and fails on documentation warnings.
  API pages show declared members; superclass links expose inherited SDK references. This also
  avoids generated inherited-overload links whose hashes do not match their emitted anchors.
  Benchmark and lint READMEs become Tooling pages without changing their build configurations.
- Unresolved KDoc references now resolve to qualified declarations, source/guide links, or plain code
  for private/nonexistent targets. Explicit property references avoid nonexistent `--root--` pages;
  links to unpublished internal implementations lead to their source files. Removed the obsolete
  `androidTheme` parameter claim. Kotlin changes outside the convention plugin are comments only.
  Module alert markers that Dokka parsed as unresolved links use bold blockquote labels instead.
- Home is explicit in navigation; duplicate SuperFences registration is removed. A small MkDocs
  hook points generated-page source actions at the authored README files. Strict page/anchor
  validation is supplemented by `scripts/check_docs.py`, which checks authored root/module/guide
  links, generated HTML links/assets/fragments, and module README introductions. Dokka's unused
  per-module navigation fragments are excluded; its actual root navigation is checked. Confirmed
  the runtime loader fetches that root fragment through `pathToRoot`.
- `docs.yml` validates every PR and main push, including workflow-only changes. Read-only validation
  checks out the PR head/main commit, builds and checks the real site, and uploads an artifact.
  A separate, dependent main-push-only job publishes that artifact without rebuilding, using the
  existing MkDocs dependency `ghp-import`. Only deployment has write permission. No deployment ran.
- The license badge returned valid Apache-2.0 content. The contributors counter repeatedly returned
  an `INVALID` SVG; it is now a static, branded “contributors welcome” link without a count claim.
  The existing build badge continues to report upstream CI state.

#### Source-link policy

Generated site/source links use the selected commit: PR head for validation, the main push commit
for deployment, and HEAD by default locally. Historical evidence pins are preserved. Repository
module READMEs use absolute `main` links because a checked relative-link attempt was rejected by
Dokka as unresolved module-documentation references. Unmerged guide review therefore uses the
assembled preview; repository README links follow released guides until merge. At release, the
same preparation pins those links to the main build's commit. Other authored guide links stay
relative where supported. Uncommitted local changes have no matching remote commit; local path
checks do not establish that new remote source URLs already exist.

#### Old URL and anchor disposition

Every retired guide path below is an authored compatibility page, published at its original
MkDocs directory URL. Each preserves the old heading anchors and links to the maintained section.
No redirects package or automatic JavaScript redirect was added.

| Retired page | Maintained destination(s) |
|---|---|
| `data-flow/` | Data; Architecture |
| `dependency-injection/` | Data → Dependency injection; Guide |
| `dependency/` | Build & Tooling → Apply or extend the build configuration |
| `faq/` | Getting Started; Troubleshooting |
| `fastlane/` | Build & Tooling → Play access and Bundler; Lanes, metadata, and publication |
| `github/` | Build & Tooling → CI/CD; Repository secrets |
| `license/` | Authoritative root LICENSE; also present in site navigation |
| `performance/` | Build & Tooling → Compose compiler metrics and reports; Components → Performance checks |
| `philosophy/` | Architecture; Data |
| `plugins/` | Build & Tooling → Convention plugins; Guide |
| `quick-reference/` | Build & Tooling → Command reference; Guide |
| `spotless/` | Build & Tooling → Spotless, via an init script; Code quality |
| `tips/` | Build & Tooling → Local documentation preview; Components → Previews |

The thirteen pages preserve all 343 heading IDs extracted from the pinned audit base, plus eight
GitHub-style duplicate-heading aliases. The generated homepage remains `/`; README includes 22
missing historical homepage anchors at the corresponding current reading-map sections. Existing
homepage anchors remain intact. Old statistics bookmarks lead to the reading map; removed numerical
claims are not restored. Old section bookmarks reach replacement guidance, not the deleted text.

#### Verification

| Check | Result |
|---|---|
| `python -m unittest discover -s scripts -p 'test_*.py'` in the documentation environment | Passed. Tests cover destination/fragment preservation, root paths, preview API links, historical pins, absent API input, stale-output removal, isolated tooling inputs, broken HTML links/assets and missing README introductions. |
| Fresh checkout plus authored patch | Disposable worktree at `/private/tmp/jetpack-df14-clean`; no local settings, API output or site output copied. The ignored legacy homepage was absent for final assembly. Android SDK supplied through ANDROID_HOME; existing Gradle dependency/build caches were permitted. |
| `./gradlew dokkaGeneratePublicationHtml -PdocsRevision="$(git rev-parse HEAD)"` | Passed with real output. All seventeen module introductions and both tooling references are checked by the publication gate. |
| Forced aggregate regeneration | Ran every `:<module>:dokkaGenerateModuleHtml --rerun` from the root's seventeen `dokka(projects...)` declarations, followed by `:dokkaGeneratePublicationHtml --rerun`, with the same revision property. BUILD SUCCESSFUL in 27 seconds; 265 tasks, 22 executed and 243 up-to-date. No unresolved-link warnings. |
| `python scripts/prepare_docs.py`, then `python -m mkdocs build --strict -f build/docs-mkdocs.yml`, then `python scripts/check_docs.py --site site` | Passed in the main worktree and disposable checkout with real inputs. The checker includes root historical records as well as active docs. Final link audit: 19,629 destinations, zero errors. |
| Historical anchor replay | Verified all 377 original heading anchors across the thirteen retired guides and old homepage in both authored Markdown and built HTML; the eight extra duplicate-heading aliases also remain. |
| Negative KDoc probe | A temporary unresolved reference in the disposable network interface made generation fail with warningCount=1. Restored the source and regenerated successfully. |
| Negative site probes | A temporary missing Markdown destination failed strict MkDocs. Broken API fragment/image references failed the HTML checker. Probe changes were confined to disposable/generated files and restored. |
| `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache` | Passed, 8 seconds; formatting changes reviewed. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache check` | Passed, 6 seconds; 1,141 tasks. |
| `./gradlew :build-logic:convention:check` | Passed separately, 2 seconds; six tasks. Combining it with the init script reproduced the already documented missing build-logic/spotless header-template issue; no formatting configuration was changed. Root Spotless success does not establish included-build formatting coverage. |
| Workflow review | YAML parsed; PR/main triggers, head revision, read-only validation permissions, artifact transfer, deployment dependency and main-only write boundary inspected. GitHub Actions and public deployment were not executed locally. |
| Rendering | Browser-reviewed homepage/navigation, navigation-module API introduction/table/Kotlin snippet, tooling reference, compatibility page, and expanded troubleshooting at 390×844. Rendered the unchanged sync Mermaid excerpt with the actual MkDocs renderer in a disposable QA page. GitHub's Markdown API rendered the updated app README; its returned HTML and a local GFM preview were inspected. These are engine/local-browser checks, not a hosted PR-page review. |
| `git diff --check`; generated-output review | Passed; no generated site/API files, environments, credentials or local configuration are included in the patch. |

Validated authored patch SHA-256, including the new compatibility pages and scripts but excluding
this execution record: `63b35c881e9db619af661ad71ade00b30c7a13748927d57609c88293ac6d90ae`.

Temporary logs and QA files are under `/private/tmp/df14-*`; the disposable checkout contains the
patch and generated verification outputs. Final hosted PR rendering, the first actual Actions run,
and deployment remain observable only after the separate commit/publication workflow. DF-15 owns
the full reader walkthrough and PR-update preparation. DF-02 runtime limitations are unchanged.
No commit, push, PR update, Firebase operation, device installation or site publication was performed.

## DF-15 — Validate the finished repair and prepare the PR update

Depends on: DF-00–14. Scope: final diff and completion evidence.

- [ ] Complete DF-00's recovery ledger and every task's evidence note. Reconcile remaining DF-02
  behavior concerns with the final docs; none may remain hidden behind a promise of safety.
- [ ] Perform reader walkthroughs: fresh debug run; configure Firebase; add and test a feature;
  customize a component/locale; diagnose a failed sync; prepare a signed release; preview docs.
  Record the steps actually exercised and those checked by inspection only.
- [ ] Review every changed example for existence, placement, imports, omitted code, return/error
  behavior, and agreement with the final implementation. Compile meaningful complete examples or
  exercise their real counterparts; a symbol search alone is insufficient.
- [ ] Search authored docs and source comments for obsolete APIs/claims (`NavController`, `NavHost`,
  Route wrappers, `networkBoundResource`, `LocalGradientTheme`, planned tests/profiles, periodic
  sync, undo, Proto-style storage). Review each match in context; valid historical references are
  not failures. Exclude generated output and old audit logs from claims about active instructions.
- [ ] Read the prose aloud, especially comments and warnings. Remove repetition, invented intent,
  rhetorical comparisons, and false certainty while retaining concrete constraints and useful depth.
- [ ] Run the applicable checks below against the finished revision and record exact commands and
  outcomes. Never copy the old plan's successful results into the new completion record.
- [ ] Inspect `git diff --check`, final changed paths, generated outputs, and secret/config handling.
  Confirm any runtime or protected configuration edits have their own review scope and authorization.
- [ ] Prepare a concise PR title/body covering the final scope, recovered guidance, validation, and
  remaining runtime/compatibility concerns. Remove line-count victory claims and unsupported “all
  snippets verified” wording. Obtain authorization before posting the update or any review comment.

Checks for the final repair (do not publish, deploy, or install on a user's device as a validation step):

| Check | When / what to record |
|---|---|
| `git diff --check` | Every change set; also inspect new untracked Markdown before it is staged. |
| `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache` | Required before any commit by `AGENTS.md`; inspect formatter changes. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` | Formatting gate for source/comment changes and before committing. |
| `./gradlew check` | Required project quality gate before committing; record environment blockers explicitly. |
| `./gradlew dokkaGeneratePublicationHtml` | Module README/KDoc changes; inspect warnings and rendered links. |
| `mkdocs build --strict` | After documented README/API preparation, using the selected local environment. |
| `./gradlew assembleDebug` | Verify first-build instructions and source/build integration after the full repair. |
| Targeted module tests, then `./gradlew test` as appropriate | Required for separately scoped behavior changes; cover actual failure modes. Do not add tests that only mirror prose edits. |
| `./gradlew :app:dependencyGuard :app:checkReleaseBadging` | Verify any separately approved dependency/manifest/build changes; the original PR already includes runtime/build edits. Do not refresh baselines merely to silence a failure. |
| Manual GitHub/MkDocs/Dokka review | Record pages and links inspected; strict builds do not prove usability or factual correctness. |

Completion record (fill when the repair is finished):

| Revision | Check or reader workflow | Result | Limitation / follow-up |
|---|---|---|---|
| Pending implementation | — | Not run | — |

Accept: repair goals are met with evidence at the final revision, and the PR accurately describes
what is ready and what remains separately tracked.

### DF-15 reader-audit chunk — partial completion

Completed 2026-10-18 from clean starting HEAD
`3be551e9f31cdd283f0fdbc2f8528eae6f751744` on `fix/docs-v3`. The user selected reader audit first,
with full validation gates and the local PR draft reserved for a second chunk. This record describes
an uncommitted editorial patch, not completion of DF-15 or validation of a future commit.

#### Recovery and behavior reconciliation

Reviewed the recovery ledger against the completed task records and maintained destinations. There
are no unchecked items before DF-15. The older records remain historical: DF-11's missing aggregate
modules/README introductions and unresolved network references were subsequently addressed by
DF-12/14; their old incomplete-publication statements do not describe the current implementation.
The thirteen compatibility pages and homepage aliases retain the DF-14 disposition. No recovery
item was dropped merely because the previous task was marked complete.

| Remaining concern | Current reader-facing disposition | Evidence boundary |
|---|---|---|
| Sync consistency | Data's delivery/conflict section, feature guide, sync troubleshooting, Room/sync/Home references describe the single snapshot, ID-only acknowledgement, overwrites and absent cross-device delete delivery. | Repository race tests rerun; WorkManager scheduling and multi-device delivery are not covered. |
| Pull checkpoints | Data distinguishes committed prefixes, equal-timestamp gaps and SDK cache completeness. Firebase explains missing legacy timestamps. | Cursor/tie tests rerun; cache-gap SDK integration remains unverified. |
| Observation lifetime | State guide, tutorial and ViewModel comments expose separately launched collectors; Profile remains in the follow-up. | Home/Item/Settings lifetime tests rerun; no Profile lifecycle fix or device test. |
| Async state lifecycle | State/helper contracts distinguish returned Results from throws/cancellation, overlap and suspended-edit replacement. Data describes the worker's cancellation catch. | Existing helper contract tests rerun; no helper/worker behavior changed. |
| Event delivery | State guide and event KDoc distinguish consumption from callback completion. LiveData and snackbar KDoc now agree. | Existing Compose event and OneTimeEvent tests rerun; no claim of exactly-once delivery. |
| Item failure handling | State, feature and troubleshooting guides retain failed-save navigation and discarded load-error limitations; proposed save replacement remains labeled. | Existing failure characterizations rerun; proposed runtime replacement was not installed. |
| Upgrade compatibility | Firebase, Data, Getting Started and changelog expose destructive Room upgrade, legacy field exclusion and older-writer/rules incompatibility. | Inspection only; no migration, backfill, rules deployment or mixed-client test. |
| Foreground sync | Troubleshooting distinguishes notification visibility from missing service-type configuration. | Worker/manifests inspected; affected Android API behavior remains a runtime follow-up. |

#### Reader journeys

All seven paths were followed through the authored instructions and their implementation/configuration
counterparts. “Inspection” below does not mean the operational workflow was executed. In particular,
this chunk establishes neither a fresh debug launch nor Firebase/release readiness.

| Journey | Instructions and expected outcome checked | Exercised / limitation |
|---|---|---|
| First debug build/run | Getting Started: clone/cd, JDK 21, SDK 37, SDK location, assemble, APK location, install then launch; app/catalog corroborate the settings. Template buildability is distinguished from configured services. | Inspection only. Existing checkout/dependency caches were used for JVM checks; no clean checkout build, app assembly, install or launch. |
| Configure Firebase | Firebase: application ID and fingerprints, providers, separate JSON/web OAuth inputs, ignored properties and generated client ID, rules, remote smoke-check expectations and upgrade caveats. Auth build/request code, remote model and release workflow inspected. | No protected configuration read or changed, Firebase console operation, signing report or backend request. Rule allow/deny and authentication smoke checks remain unexecuted. |
| Add/test a feature | Guide: existing Home wiring, source/resource placement, Room registration, Hilt bindings, repository Results/Flows, keyed entries and app assembly. Excerpts retain explicit omissions and migration/worker integration steps. | Ran existing Home, navigation, DAO and repository suites; compiled/executed the exact complete Home/Item tutorial tests. No new production module/feature or device acceptance run. |
| Customize component/locale | Components: static/dynamic palette precedence, wrapper contracts, previews, accessibility, resource prefixes and full language-picker path; compared Theme, SettingsDialog/ViewModel, Language and locale helpers. | Exact RetryButton/preview example compiled in core UI's temporary test sources; existing Settings/UI tests rerun. Contrast, TalkBack, RTL rendering, locale persistence/recreation and actual component interaction remain device/manual checks. |
| Diagnose failed sync | Troubleshooting: unique work name, RUNNING vs waiting/retry, KEEP, diagnostics broadcast, first worker error, retry counts and foreground setup; compared worker, manager and repository. | Repository concurrency/cursor characterizations rerun. No diagnostics broadcast, device inspector, real worker scheduling or remote reproduction. |
| Prepare signed release | Getting Started and Build & Tooling: app-relative keystore path, debug fallback, versionCode formula, Appfile/package alignment, secret formats, locked Bundler/Fastlane, lane consequences and independent publication jobs. Compared app build, Fastfile/Appfile and cd.yml. | Inspection only; no signing credentials, service-account key, release build, Fastlane command, tag, upload or publication. |
| Preview docs | Build & Tooling: Android/Python prerequisites, real Dokka input, preparation, strict build, link audit and serve; compared scripts and docs.yml. | Authored-link check ran. Existing site served locally and browser-reviewed; no fresh Dokka/site generation. See provenance below. |

#### Example coverage and editorial changes

Inventoried fenced blocks in changed active Markdown against audit base `1c2103890`: 112 current
blocks, 108 changed. Excluded generated output, historical PLAN/TASKS and DOC_FIX audit records.
The 108 changed blocks comprise eight root-guidance blocks, 21 blocks across nineteen module READMEs,
and 79 blocks in the ten maintained guides. Diagrams and command/configuration excerpts are included;
these counts describe review coverage, not a completion target or a claim that every block compiled.

| Example group | Disposition |
|---|---|
| Root instructions and contributor examples | Commands checked against the build/task definitions; Home/NavKey/query excerpts checked against their real owners. Abbreviations remain explicit. Formatting check ran; full build/guardrail commands remain for the next chunk. |
| Module README excerpts | Source paths, signatures, query predicates, defaults and plugin/test dependencies checked against their owners. SettingsRepositoryExampleTest extracted verbatim and executed. Benchmark, auth, remote, reporting and lint excerpts were inspected, not exercised against devices/services. |
| Architecture, data, navigation and feature excerpts | Models/mappings, DAO/provider/binding construction, return/error paths, runtime arguments, registrations and algorithm diagrams compared with current code. Meaningful Home/navigation/Room/repository counterparts exercised. Proposed save handling remains an inspected proposal, not a shipped fix. |
| State and component examples | Exact HomeFeatureExampleTest and ItemInputExampleTest compiled/executed; RetryButton with its documented package and preview compiled. Helper/event/swipe/state counterparts exercised. Palette, locale, permission and other context-dependent snippets inspected with their surrounding declarations; no claim of device/UI acceptance. |
| Setup, Firebase, release, tooling and troubleshooting commands | Paths, placeholders, prerequisites and side effects checked against build/scripts/workflows. External-service, signing, installation, instrumentation, baseline generation and publication commands were not run. |

Corrected the following reader-facing contradictions; all Kotlin changes are comments/KDoc only:

- `core/navigation/.../Navigator.kt`: default entry identity is `key.toString()`, not the key object;
  sharing a key type alone does not imply collision. Rechecked the cached Navigation 3 source used
  for DF-08 (`NavEntry.kt`, `defaultContentKey`), the pinned catalog and the maintained identity guide.
- `core/ui/.../components/Navigation.kt`: removed the obsolete NavHost example and unsupported fixed
  drawer mapping; the wrapper delegates selection to Material defaults.
- `core/ui/.../extensions/LifecycleExtensions.kt`: use the Fragment view lifecycle for view updates;
  distinguish cancellation/recollection from pausing, and per-instance event consumption from
  completed delivery. Removed the Navigation 2 event example.
- `core/ui/.../utils/SnackbarAction.kt`: labels do not implement report/undo behavior; resource
  lookup in the suspend callback uses a captured Context. Illustrative Screen examples are labeled.
- `core/ui/.../utils/LocaleUtils.kt`: replaced fictional Settings wiring and universal persistence/
  formatting promises with the actual setter excerpt and resource/model/picker integration link.
- `core/ui/.../utils/TextFieldData.kt`: corrected the email-validator name, used JetpackTextField,
  labeled illustrative declarations, and replaced a conflicting UiText type redefinition with
  extension guidance. Null error does not prove validation ran.
- `feature/auth/.../signup/SignUpViewModel.kt`: corrected Screen terminology and the false claim
  that field errors disable submission. The screen currently supplies no validation-based enabled
  condition; this is source-reviewed, not a tested behavior fix.
- `sync/.../worker/DelegatingWorker.kt`: replaced invented architectural motives and the claim Hilt
  cannot inject workers with the actual entry-point/factory path and its limited forwarding surface.
  Checked [official Hilt/WorkManager integration](https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager).
- `core/ui/.../theme/Theme.kt`: replaced the copied “Now in Android theme” summary with this API's role.
- `docs/state-management.md`: removed the stale statement that feature-walkthrough repair is queued.
- This execution record is the only other changed path. No runtime API/type, dependency, resource,
  module, migration, rule, protected configuration or formatting policy changed.

Newly exposed follow-up: **Auth form submission validation** needs its own product decision and
runtime tests for untouched/invalid fields and backend failures before claiming the submit button
blocks invalid input. This chunk corrects that claim without changing registration behavior.
Worker lifecycle forwarding beyond doWork/getForegroundInfo is not established by the wrapper;
review cancellation/progress if changing that integration rather than assuming every callback forwards.

The obsolete-claim search included active Markdown and Kotlin source, excluding generated output
and historical audit logs. Reviewed remaining matches: explicit absence of periodic sync/undo,
Screen-vs-Route guidance, the prohibition on NavController/NavHost, SnackbarAction.UNDO's label,
Git index-hint reversal and historical compatibility anchors are legitimate references. Removed
remaining positive Navigation 2 examples. Read the edited comments and key warnings for plain,
factual wording; preserved release, compatibility, sync and state limitations.

#### Fresh checks and provenance

Temporary extracted examples, the init script and logs are under `/private/tmp/df15-reader-audit/`.
They are not production sources or tracked test additions. The init script attaches extra test
source directories only to this invocation; normal module configuration remains unchanged.

| Command / check | Result and limits |
|---|---|
| `./gradlew :feature:home:testDebugUnitTest :core:navigation:testDebugUnitTest :core:room:testDebugUnitTest :data:testDebugUnitTest :core:ui:testDebugUnitTest :feature:settings:testDebugUnitTest` | Passed; all tasks up-to-date. Used only to establish baseline resolution; not counted as fresh test execution. |
| `./gradlew --init-script /private/tmp/df15-reader-audit/examples.init.gradle --no-configuration-cache :feature:home:testDebugUnitTest --rerun :core:navigation:testDebugUnitTest --rerun :core:room:testDebugUnitTest --rerun :data:testDebugUnitTest --rerun :core:ui:testDebugUnitTest --rerun :feature:settings:testDebugUnitTest --rerun` | Passed, 13s; all six test tasks executed. 99 tests, zero failures/errors/skips: Home 14 (including three extracted example tests), navigation 14, Room 18, data 19, UI 27, Settings 7. RetryButton/preview compiled as temporary UI test source. The initial harness attempt failed during included-build evaluation because the Home project was absent there; adding an early null guard fixed the temporary script, with no repository build change. |
| `./gradlew --init-script /private/tmp/df15-reader-audit/examples.init.gradle --no-configuration-cache :data:testDebugUnitTest --tests dev.atick.data.repository.settings.SettingsRepositoryExampleTest` | Passed, 1s; two exact README example tests. This filtered run replaces data's local XML report with the two selected tests; the earlier full-suite result is in targeted-tests.log. |
| `./gradlew :core:android:testDebugUnitTest --tests dev.atick.core.utils.OneTimeEventTest --tests dev.atick.core.utils.CoroutineUtilsTest --tests dev.atick.core.extensions.StringExtensionsTest --rerun :core:preferences:testDebugUnitTest --rerun :core:network:testDebugUnitTest --rerun` | Passed, 5s; 33 tests, zero failures/errors/skips (20 selected Android, 10 serializer, 3 connectivity). Together the fresh runs exercised 134 tests, including five extracted documentation tests. No emulator/backend execution. |
| `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` | Passed, 13s. Formatting check only; no formatter applied and no commit made. Included-build formatting caveat remains unchanged. |
| `.venv/bin/python scripts/check_docs.py` | Passed for authored links; no `--site` claim for this patch. Repeated after adding this record. |
| Comment-only comparison | Removed line/nested block comments while preserving literals, then compared all nine changed Kotlin files with HEAD: executable content identical. |
| `git diff --check`; changed-path review | Passed. No extracted examples, generated site/API files, credentials or local configuration are tracked changes. |

Browser review used existing `site/` output with HTML modification time 2026-10-18 09:48 local time.
Its visible source links pin `fa1d26001d9d7f8bf30af50a78f056cf99f82994`, so it is explicitly an older
preview, not a build of this patch. Served it on loopback port 8015 and followed Home → Guide →
Testing and Components; inspected navigation, reading structure, tables and source-link provenance.
This does not verify newly edited KDoc, current GitHub rendering or a hosted PR. No site was rebuilt
or deployed. Fresh comprehensive rendering remains for the next chunk.

#### Handoff to the final-validation chunk

Keep DF-15 pending. Run the original final-check matrix against the resulting revision: formatting
apply/review/check as required before committing, project check, debug assembly, real Dokka,
preparation/strict MkDocs/HTML-link audit, and applicable dependency/badging gates for the whole PR.
Recheck regenerated KDoc links and review GitHub/MkDocs/Dokka rendering, including the edited API
comments. Record current results rather than copying prior success, and retain the inspection-only
limits on Firebase, signing and device workflows. Prepare a local PR title/body describing recovered
guidance, actual verification and outstanding runtime/compatibility concerns. Posting, committing,
pushing, installing and publishing are outside this chunk.
