# Documentation repair tasks

Companion goals, findings, and recovery map: [DOC_FIX_PLAN.md](DOC_FIX_PLAN.md).

Status: DF-00 complete; DF-01–DF-14 pending. Checked items identify completed audit work only. Reviewed base: `1c2103890d16f81f6c6ae4c2d8626281b841e417`.
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

- [ ] Replace “comments explain why, not what” with the plan's guidance on behavior, constraints,
  contracts, and supported rationale. Allow lists and KDoc links where they improve readability.
- [ ] Remove mandatory code-emoji/Markdown-alert parity and numeric alert/line budgets. Preserve
  GitHub-compatible alert syntax and concrete signing/data-loss notices where useful.
- [ ] Add a short example of a helpful comment and a useful tutorial excerpt. Explain how to label
  omissions and hypothetical additions without pretending they are existing code.
- [ ] Mark the old `PLAN.md` and `TASKS.md` as historical and point to these repair files. Preserve
  evidence in the old logs; remove their authority over new editorial work.
- [ ] Apply the new standard to subsequent tasks: no invented motives, blame, self-congratulation,
  blanket bans on reasonable alternatives, or repeated historical explanations of fixed bugs.

Accept: contributor guidance evaluates usefulness and accuracy; it no longer rewards deleting
content to meet a quota or adding a justification to every unusual line.

## DF-02 — Settle contracts and separate runtime work

Depends on: DF-00. Scope: targeted inspection/tests and a local behavior-follow-up record.

- [ ] Classify the PR's executable changes separately: swipe effect, initial connectivity emission,
  raw price state, unused serializer removal, no-op variant-block removal, and server-clock sync
  with Room schema/model/query changes. Classify the backoff correction as documentation-only.
- [ ] For `HomeRepositoryImpl.sync()`, examine edits after the pending-row snapshot, edits to a row
  while its push is in flight, `markAsSynced(id)` clearing a newer edit, and a pull overwriting that
  edit. Examine whether a request dropped by `KEEP` is ever guaranteed another run. Do not call
  these paths safe merely because the first write was stored locally.
- [ ] Review partial pull/retry behavior: rows are individually upserted while the cursor is their
  maximum server timestamp. Determine what happens if collection fails after storing only some
  returned documents. Check query ordering, equal timestamps, and any SDK cache assumptions before
  claiming complete incremental delivery.
- [ ] Check the lifecycle of collectors launched from `onStart` in Home, Item, and Settings
  ViewModels. `launchIn(viewModelScope)` outlives the shared upstream; document or test repeated
  subscribe/unsubscribe cycles beyond the five-second timeout.
- [ ] Review `StatefulComposable` event consumption during composition and effect cancellation/key
  behavior. Verify at-most-once consumption separately from successful snackbar delivery.
- [ ] Review update-helper behavior for returned failures, thrown exceptions, cancellation,
  overlapping calls, and synchronous edits during a suspended update. The loading flag is checked
  before launch and set inside it; avoid describing it as a general concurrency lock.
- [ ] Trace Item save/load failures: save discards the repository `Result`; load `catch` constructs
  an unused `UiState`. Record that the guide copies these paths, so code agreement alone does not
  make the example a sound error-handling tutorial.
- [ ] Assess schema 1 → 2 for existing installs and pending offline changes. Assess Firestore
  documents without `serverUpdatedAt`, clients writing the old field set, and the updated rule
  requirements. The old task log's assumption that no adopter has old data is not evidence.
- [ ] Record each issue as confirmed from source, reproduced by a targeted test, or still unverified.
  Give it a bounded outcome: separately scoped fix, accurate documented limitation with follow-up,
  or disproved concern with evidence. Do not silently expand the documentation repair into runtime
  or backend changes. New external issues/comments need user authorization.

Accept: DF-06–08 and DF-12 have clear contracts to document. No uncertainty has been converted into
a claim of safety. The PR's existing tests are distinguished from missing concurrency/upgrade tests.

Behavior ledger (fill during implementation):

| Concern | Evidence/reproduction | Decision and follow-up | Documentation affected |
|---|---|---|---|
| Pending DF-02 review | Initial source findings are in the plan | — | — |

## DF-03 — Make first-run and Firebase setup complete

Depends on: DF-00; DF-02 for compatibility statements. Scope: `docs/getting-started.md`, `docs/firebase.md`.

- [ ] Walk clone → change into the checkout → select JDK/SDK → build → install/run. State expected
  behavior with the template Firebase config and with a configured Firebase project.
- [ ] Verify the Android Studio/AGP compatibility requirement from official documentation; replace
  “Hedgehog or newer” if incompatible with the pinned AGP. Recheck IDE preview instructions too.
- [ ] Document the actual local configuration path: `GOOGLE_WEB_CLIENT_ID=<web OAuth client ID>` in
  ignored `local.properties`, defaults in `secrets.defaults.properties`, and how to obtain the
  correct client type. Distinguish this from downloading `app/google-services.json`.
- [ ] Explain package/application ID matching, enabled auth providers, certificate fingerprints for
  the build being tested, and the steps needed after replacing template identifiers in an adopter app.
- [ ] Verify Credential Manager's authorized-account filter and Firebase account creation semantics
  from the pinned SDK or official docs. Remove “entire sign-in/register contract in one flag.”
- [ ] Describe handling of the tracked template config accurately. `--skip-worktree` is a local index
  flag, not a security control or true untracking; include how to inspect/undo it if retaining advice.
- [ ] Keep Firestore rules, model fields, path, and soft-delete behavior consistent. Explain that
  editing a Markdown rules example does not deploy it; include the compatibility outcome from DF-02.
- [ ] Verify Crashlytics setup/report collection and mapping-upload claims against build config and
  official guidance. Remove “almost always timing” diagnoses without evidence.
- [ ] Keep signing guidance precise: a debug-signed release can be installed, but switching signing
  certificates affects upgrades. Avoid the unqualified “cannot be upgraded” statement.

Accept: someone following only the linked setup path knows every local setting required to build
and try auth/sync. No real credentials, console changes, or deployment are needed for doc validation.

## DF-04 — Restore build, release, and local docs instructions

Depends on: DF-00–01. Scope: `docs/build-and-tooling.md`; root README navigation.

- [ ] Restore a compact command reference for build/install, unit and device tests, formatting,
  Dokka, Dependency Guard, APK badging, and baseline profiles; give prerequisites and output paths.
- [ ] Restore release secret formats from `cd.yml`: `GOOGLE_SERVICES`, `KEYSTORE`,
  `KEYSTORE_PROPERTIES`, and `PLAY_STORE_JSON` are base64-decoded; `GOOGLE_WEB_CLIENT_ID` is written
  as plain text. Map each to its actual destination, including `app/key.jks` and matching `storeFile`.
- [ ] Restore Play service-account setup references, `fastlane/Appfile` package/key path, Bundler
  workflow, actual metadata/changelog behavior, version-code preparation, and tag-triggered release
  steps. Verify current provider requirements with official sources. Use placeholders for secrets.
- [ ] Describe GitHub APK publication and Play AAB upload as separate jobs. Inspect the legacy `beta`
  lane before presenting it as usable; do not treat generated `fastlane/README.md` as a setup guide.
- [ ] Restore Compose metrics/report flags and actual output layout from `AndroidCompose.kt`:
  `enableComposeCompilerMetrics`, `enableComposeCompilerReports`, and root
  `build/<module>/compose-metrics` / `compose-reports`. Show how to inspect results.
- [ ] Retain baseline profile generation and measurement instructions; verify managed-device task
  names and required API from the configured plugin/SDK. Remove unsupported “silently ignored” claims.
- [ ] Explain existing convention-plugin use, catalog aliases, actual Renovate setup, and approval
  boundaries for build changes. Correct “all versions live in the catalog”: the init script and
  workflow contain explicit tool versions. Do not move versions as part of a prose edit.
- [ ] Explain the Spotless init script's observed scope and invocation. Remove “every Gradle
  invocation” and unverified claims that the script formats the included build. Use
  `--init-script gradle/init.gradle.kts --no-configuration-cache` consistently.
- [ ] Recover header-template customization instructions from `docs/spotless.md`, without changing
  headers or Spotless configuration as part of this task.
- [ ] Document local site setup using the dependencies/extensions in `docs.yml` and `mkdocs.yml`:
  isolated Python environment, Dokka generation, API copy, README-to-index transformation, strict
  build, then preview. Explain which files are generated and ignored. Cross-check with DF-14.

Accept: build/format/preview commands are usable from a clean checkout; release preparation includes
formats and paths rather than only names. Publishing commands are documented, not executed.

## DF-05 — Repair the architecture explanation and module map

Depends on: DF-00–02. Scope: `docs/architecture.md` and architecture links.

- [ ] Replace generic fictional `HomeAction`, `Data`, `Repository`, and `Params` examples with actual
  types, or label a small conceptual example honestly. Remove repeated principles and summaries.
- [ ] Add a current module dependency diagram/table from Gradle declarations. Distinguish build
  dependencies from runtime calls: `sync → data`, Firebase modules depend on `core:android`,
  `core:ui` exposes `core:navigation`, and `core:testing` has test-helper dependencies.
- [ ] Explain the `SyncManager` interface in `data` and its Hilt binding/implementation in `sync`
  without drawing a Gradle dependency cycle. Include app assembly, lint, and benchmarks appropriately.
- [ ] Correct “periodic” sync in prose and the sequence diagram; show actual trigger, pending local
  write, push, pull, Room emission, and lifecycle-aware UI collection.
- [ ] Describe the two-layer design and useful extension criteria once. Avoid blanket prohibitions
  on combining REST/Firestore or adding a domain layer when an adopter's requirements call for it.
- [ ] Link to a real test walkthrough and avoid hardcoded test counts or “full harness” claims that
  imply coverage of behavior the tests do not exercise.

Accept: each architecture arrow and example is supported by source; module-map links lead to a real
map; an adopter can understand where a change belongs.

## DF-06 — Repair data, DI, persistence, and sync guidance

Depends on: DF-02, DF-05. Scope: `docs/data.md`, related module READMEs/KDoc, Firebase cross-links.

- [ ] Restore real DI examples: repository `@Binds`, Room/Retrofit/DataStore `@Provides`, qualifiers,
  `@ApplicationContext`, scope/install location, and fake injection in tests. Avoid calling `@Binds`
  merely a cast or inventing scopes used by this project.
- [ ] Describe JSON-backed typed DataStore and the actual corruption exception path. Explain
  defaults, adding a preference, serializer compatibility, and reset/sign-out behavior without
  claiming automatic corruption recovery.
- [ ] Restore Room entity/DAO/database registration and version/migration guidance. Explicitly
  describe destructive fallback and loss of unsynced data; distinguish version upgrades from
  same-version schema-integrity failures. Use the compatibility decision from DF-02.
- [ ] Correct the timestamp table using DAO queries and both mapping directions: milliseconds versus
  nanoseconds, local pending-change checks, server pull cursor, and transmitted `lastSynced`.
- [ ] State what the sync code does in order. Correct “every read requests sync” (`getJetpack(id)`
  does not), “drain everything,” guaranteed follow-up runs, and conflict-free push/pull claims.
- [ ] Explain soft-delete/tombstone retention accurately. Rows remain after successful push; no
  automatic tombstone cleanup is implemented. Keep server-cursor inclusion of deleted rows clear.
- [ ] Replace “writes always succeed” with the actual boundary: local persistence and enqueueing
  can fail; a successful local operation does not mean remote sync succeeded.
- [ ] State cancellation behavior and error boundaries accurately. `suspendRunCatching` rethrows
  cancellation; data sources may throw; Flow failures need handling; update helpers consume Results.
- [ ] Document DF-02 sync limitations and upgrade requirements near the algorithm, without a
  blanket safety assertion. Explain the server timestamp mechanism once, with short local reminders.
- [ ] Restore how to observe sync progress and locate scheduling/retry constraints. Keep the worker
  and repository roles distinct; verify retries versus total attempts and the existing backoff.

Accept: documented persistence/error/sync guarantees do not exceed implementation; an adopter can
extend storage and understand the consequences for existing data.

## DF-07 — Make state and event documentation trustworthy

Depends on: DF-02. Scope: `docs/state-management.md`, `StatefulComposable.kt`, linked ViewModel KDoc.

- [ ] Correct the update-function table: `updateState` replaces `data` synchronously via its receiver
  lambda; `updateStateWith` replaces it after a successful Result; `updateWith` preserves it.
- [ ] Describe returned failure handling separately from thrown exceptions/cancellation. Remove
  claims that helpers automatically catch everything or reset loading on every possible exit.
- [ ] Explain the loading check's actual call assumptions, dropped actions, and suspended-operation
  snapshot behavior. Give readers guidance for independent operations and edits during loading,
  using an explicitly labeled extension if the template does not implement it.
- [ ] Document subscription/restart behavior from DF-02 and show an appropriate test pattern that
  actively collects `stateInDelayed`; do not teach that a `.value` assertion always exercises it.
- [ ] Replace exactly-once delivery guarantees with the actual `OneTimeEvent` consumption contract;
  align error/snackbar and navigation explanations with the DF-02 outcome.
- [ ] Fix the save example after DF-02 disposition. Clearly show how the repository Result affects
  the success event, or identify the current limitation; do not advertise the ignored Result as safe.
- [ ] Keep raw price input guidance, but explain parsing/formatting separately. Verify edge-case
  examples such as `1.` against the actual parser instead of claiming every issue is parse failure.
- [ ] Remove fabricated Posts/User.EMPTY demonstrations and blanket “nullable fields are bad” advice.
  Explain renderable initial state and legitimate optional data using current screen models.
- [ ] Reduce duplicate helper tutorials in KDoc; retain callable contracts, relevant examples,
  parameters, and resolvable API links. Put longer lifecycle explanations in the guide.

Accept: a reader can select an update function, handle a failed operation, and test a collected
ViewModel state without relying on unsupported lifecycle/error guarantees.

## DF-08 — Turn the feature tour into a usable walkthrough

Depends on: DF-05–07. Scope: `docs/guide.md`, `docs/navigation.md`.

- [ ] State which excerpts explain the existing home feature and which steps create an adopter's
  new feature. Include exact placement and integration steps omitted by a source-code tour.
- [ ] Cover module registration/plugin/dependency wiring when a new module is needed; otherwise
  explain adding a screen to an existing module. Respect the existing approval boundary for new modules.
- [ ] Cover entity/DAO/database registration and DI, repository binding, screen data, ViewModel,
  destination entries, app-level registration, and tests. For synced features, identify the worker
  integration point rather than implying `Syncable` alone registers the feature.
- [ ] Update the `JetpackEntity` excerpt to include `serverUpdatedAtNanos` or explicitly mark the
  omitted metadata. Check all other snippets against their source and label abbreviations.
- [ ] Preserve Navigation 3, assisted injection, multi-stack restoration, list/detail panes, and back
  handling. Explain adding a top-level destination and account-boundary stack behavior.
- [ ] Clarify navigation identity: equal content keys matter, not simply reuse of a key type. Verify
  duplicate-entry advice against the pinned Navigation 3 implementation before proposing a solution.
- [ ] Use a small real feature test with `MainDispatcherRule`, a fake, active Flow collection, and
  meaningful success/failure assertions. Existing test links alone are not a worked example.
- [ ] Walk the complete sequence in a disposable checkout or a clearly labeled example, checking
  imports, visibility, resource prefixes, and Gradle registration. Do not add a demonstration module
  to the production tree merely to validate documentation.

Accept: a reader can follow the guide through app integration and validation; omitted code is
identified, and examples do not disguise unresolved DF-02 behavior as a recommended pattern.

## DF-09 — Restore practical UI customization guidance

Depends on: DF-00–01. Scope: `docs/components.md`, UI/localization references.

- [ ] Rewrite the opening to explain what the components provide. Put lint rules alongside guidance
  for choosing/adding a component, instead of opening with a threat that the build will fail.
- [ ] Keep the useful catalog and link to source/API details. Verify overloads, behavior, default
  colors, adaptive navigation modes, image-loader/tint claims, and supported lint substitutions.
- [ ] Remove fragile component/file counts and unsupported “used/unused” declarations, or verify
  usages including calls inside other wrappers. Avoid repeated “real” as a quality claim.
- [ ] Restore short examples for theme colors, typography, dynamic color, previewing, and creating a
  component. Explain public API choices and extending `DesignSystemDetector`/its tests when relevant.
- [ ] Restore actionable accessibility guidance: labels/decorative icons, touch interaction,
  semantics, loading/error announcements, and checking customized theme contrast. Do not restore
  claims that theme colors automatically guarantee accessibility.
- [ ] Restore the localization path: resources and prefixes, `Language`, language option UI,
  `getPreferredLanguage`, locale config, AppCompat persistence, and RTL checks.
- [ ] Give `UiText`, permission helpers, validation utilities, and shared preview annotations useful
  examples or maintained links. Separate lint conventions from actual preview failure causes.

Accept: a reader can customize or extend the UI accessibly and test previews/locales using the
project's components, without reading the entire component implementation first.

## DF-10 — Rebuild troubleshooting around reliable diagnosis

Depends on: DF-03–09. Scope: all entries in `docs/troubleshooting.md`.

- [ ] Review all 29 existing entries and unique deleted symptoms from DF-00. Each should state the
  symptom, plausible causes, how to distinguish them, a fix, and how to confirm recovery.
- [ ] Correct claims that `collectAsState()` prevents updates, ordinary `@Preview` or non-private
  previews inherently fail, or every repeated event is caused by omitting `OneTimeEvent`.
- [ ] Correct sync retry diagnosis: swallowed errors do not explain retry exhaustion. Distinguish
  pending constraints, dropped requests, authentication/rules failures, and exhausted retries.
- [ ] Replace client-clock conflict-resolution advice with the DF-02/06 outcome. Verify debugging
  commands; do not assume an Android device ships a usable `sqlite3` binary.
- [ ] Check configuration-cache/plugin diagnoses against current build config and upstream sources.
  Remove “nothing to do” as a universal answer to configuration-cache warnings.
- [ ] Correct Room integrity/migration advice and notification/foreground-service guidance against
  the actual manifest and permission flow. Link to the specific setup/configuration steps.
- [ ] Replace “clean build --refresh-dependencies resolves most mysterious states” with targeted
  checks. Include missing new-tool symptoms such as dependency/badging baseline drift and lint errors.
- [ ] Move “How do I run tests?” to the command/testing guide; replace remaining “Cause: N/A” entries
  with appropriate instructional content. Remove editorial commentary about nonexistent symptoms.
- [ ] Preserve readable/searchable summaries and functional links, alerts, and code inside details
  blocks in both GitHub and MkDocs rendering.

Accept: each entry helps diagnose rather than guessing a single cause; restored topics are easy
to find, with no arbitrary line ceiling.

## DF-11 — Reconcile all module READMEs and API entry points

Depends on: DF-03–10. Scope: nineteen module READMEs, Dokka output.

- [ ] Preserve the leading `# Module :...` syntax used by Dokka. Keep a purpose statement, important
  entry points, enough usage context, and links; choose structure by module needs, not a fixed cap.
- [ ] Review `app`, `core/android`, `core/navigation`, `core/network`, `core/preferences`, `core/room`,
  `core/testing`, and `core/ui`; reconcile all findings with their repaired guide sections.
- [ ] Review `data` and `sync`: include all key repository roles, correct the claim that `sync`
  implements `Syncable` itself, and remove unsupported local-write/scheduling assurances.
- [ ] Review `feature/auth`, `feature/home`, `feature/profile`, `feature/settings`: remove nonexistent
  undo and Route wrappers; check settings/sign-out and actual repository boundaries.
- [ ] Review `firebase/auth`, `firebase/firestore`, `firebase/analytics`: accurate auth filter,
  timestamp, rule deployment, and Crashlytics guidance without duplicated defensive explanations.
- [ ] Review `benchmarks` and `lint`: preserve practical commands, remove invented adopter behavior
  (“first hour”), and distinguish the actual startup scenario from unsupported CI/runtime promises.
- [ ] Replace `core/testing`'s “does the thing” example with a useful real test; remove “a mock can't
  express it” and stale consuming-module lists. Explain how consumers add and use the helpers.
- [ ] Verify API names and source pointers. In particular, `core/network`'s enum inventory must not
  imply `LOSING` is emitted when the implementation only logs it.
- [ ] Check both repository-relative links and their rendered Dokka destinations. A link working
  from a README on GitHub does not prove it works in generated API HTML.

Accept: all nineteen module references have a recorded review; each helps a reader use the module
and reaches accurate deeper guidance from both GitHub and Dokka.

## DF-12 — Edit every source comment touched by the PR

Depends on: DF-01–02 and the relevant guide repairs. Scope: sites below plus their enclosing KDoc.

Perform an editorial and factual pass at every listed site. Retain licenses, valid contracts, useful
issue links and non-obvious constraints. Edit prose only here; protected rule/build changes and
runtime fixes belong to separately scoped work.

- [ ] `app/proguard-rules.pro`: remove unsupported intent; state actual keep scope; verify the linked
  workaround against current Navigation/R8 before retaining a necessity claim. Keep rule tokens intact.
- [ ] `app/build.gradle.kts`: concise signing consequence/action; remove the removed-placeholder
  story while retaining useful issue tracking for custom APK naming.
- [ ] `gradle/init.gradle.kts`: explain invocation and observed scope, not hypothetical reasons for
  the architecture. Correct the “every Gradle invocation” claim; review adjacent stale TODO wording.
- [ ] `build-logic/.../AndroidCompose.kt`: explain that missing/false flags leave report destinations
  unset; avoid the long provider/null lecture. Follow the repo's convention-plugin boundary.
- [ ] `firebase/auth/consumer-rules.pro`: verify discovery from SDK source; do not assume
  `ServiceLoader`. Explain the actual kept provider classes and condition without changing rules.
- [ ] `firebase/auth/.../AuthDataSourceImpl.kt`: verify the lint issue's actual meaning and error
  handling, then shorten the suppression/filter explanations and redundant cross-function comment.
- [ ] `core/ui/.../theme/Background.kt`, `Gradient.kt`, `Tint.kt`: fix `LocalGradientTheme` to the
  actual `LocalGradientColors` where appropriate; verify any recommended rule-setting syntax.
  Explain the present suppression without implementing a Spotless/EditorConfig change here.
- [ ] `core/ui/.../utils/StatefulComposable.kt`: reconcile enclosing KDoc and body comments with
  DF-07, especially effect keys, consumption, thrown exceptions, and concurrent-update limits.
- [ ] `feature/home/.../HomeViewModel.kt`, `ItemViewModel.kt`, `ItemScreen.kt`, and
  `feature/settings/.../SettingsViewModel.kt`: concise lifecycle/input/locale guidance; remove
  “most-copied idiom,” obsolete Route KDoc, and unsupported parser examples.
- [ ] `core/network/.../NetworkUtils.kt` and `NetworkUtilsImpl.kt`: state initial emission and
  subsequent callback behavior; remove the overbroad “only reports transitions” assertion and long
  retelling of the fixed startup bug. Distinguish available network from validated Internet access.
- [ ] `core/ui/.../components/SwipeToDismiss.kt`: explain the deletion side effect and transition
  lifetime briefly; preserve the fix and its regression test.
- [ ] `data/.../HomeRepositoryImpl.kt`, `data/.../model/home/Jetpack.kt`: local sync-request timing,
  field mapping, server cursor, and echo behavior consistent with DF-02/06. Avoid universal claims
  that an echo cannot change content.
- [ ] `core/room/.../JetpackDao.kt`, `JetpackDatabase.kt`, `LocalDataSource.kt`,
  `LocalDataSourceImpl.kt`, `JetpackEntity.kt`: check query, units, null/default cursor, tombstones,
  and schema documentation. Move repeated clock-skew narratives into one guide explanation.
- [ ] `firebase/firestore/.../FirebaseDataSource.kt`, `FirebaseDataSourceImpl.kt`,
  `model/FirebaseJetpack.kt`: retain null-on-write server timestamp behavior and the constructor
  visibility constraint succinctly. Clarify what nanosecond conversion preserves without repeating
  “silently and permanently” or overstating first-pull coverage of old documents.
- [ ] `sync/.../Sync.kt`, `SyncManagerImpl.kt`, `SyncWorker.kt`: remove unjustified safety assurances,
  reconcile cross-links, and shorten retry/backoff documentation to the relevant contract.
- [ ] Check comment/KDoc consistency in the five changed test files: `NetworkUtilsImplTest`,
  `JetpackDaoTest`, `SwipeToDismissTest`, `HomeRepositoryImplTest`, `ItemViewModelTest`. Preserve
  meaningful setup explanations and regression intent; remove claims beyond their assertions.
- [ ] Review adjacent surviving boilerplate only where it contradicts the edited contract. Verify
  the removed `DarkThemeConfigSerializer` leaves no instructions telling readers to use it.
- [ ] Compare the final source diff to the pre-repair head and confirm editorial commits contain
  no unintended Kotlin, Gradle, R8, annotation, SQL, or test-behavior changes.

Accept: every PR-touched source site has a disposition; comments help with behavior or maintenance,
and neither invented intent nor documented bugs are presented as architectural guarantees.

## DF-13 — Reconcile root guidance, changelog, and review history

Depends on: DF-03–12. Scope: `README.md`, `AGENTS.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, old plan/task files.

- [ ] Make root README a useful starting map: setup, feature walkthrough, reference, testing,
  customization, releases, and local docs preview. Preserve project-specific voice and presentation.
- [ ] Fix `AGENTS.md`'s module diagram/rules to reflect actual declared dependencies, including
  `core:navigation`, `core:testing`, Firebase → core, and sync → data. Keep intended boundaries clear.
- [ ] Remove the stale claim that `app/build.gradle.kts` still contains a no-op `onVariants` block.
  Reconcile state/error helper guidance, Firebase terminology, and all command flags with the guides.
- [ ] Review inherited contribution workflow advice such as targeting a “development” branch against
  the current project workflow; avoid changing unrelated author preferences without evidence.
- [ ] Rework the new changelog prose around reader-visible changes and compatibility effects. Make
  schema/data loss and backend field/rule changes explicit; remove test counts and long bug narratives.
- [ ] Ensure material runtime fixes in this PR have appropriate release-note coverage. Distinguish
  newly recorded earlier work from runtime changes actually introduced by this branch.
- [ ] Keep previous plan/task logs as dated history with a pointer to the new work. Correct active
  “complete/all examples verified” implications without erasing historical verification records.

Accept: root instructions agree with the guides and code; release notes disclose actual upgrade
implications; readers can tell which plan is active.

## DF-14 — Repair site assembly, links, and validation

Depends on: final page destinations from DF-03–13. Scope: `mkdocs.yml`, `.github/workflows/docs.yml`,
documentation links, generated-output handling in `.gitignore`; PR validation workflow if needed.

- [ ] Build the site from a clean checkout using DF-04's documented process, including the generated
  README homepage and real Dokka API output. Do not use dummy API files to obtain a strict pass.
- [ ] Confirm the generated homepage is reachable at the site root and navigable from every page;
  decide whether Home needs an explicit nav entry. Check the README link transformation does not
  damage external URLs or anchors.
- [ ] Audit local links, headings/anchors, source paths, API links, images, and section links across
  all root/module/docs files. Resolve the duplicate `pymdownx.superfences` entry if confirmed redundant.
- [ ] Map every deleted public page URL and relevant old anchor to its replacement. Implement
  lightweight compatibility pages/anchors where feasible with existing tooling; record any
  unavoidable break explicitly. Do not add a redirects dependency without approval.
- [ ] Check branch-specific source/README links that point to `main`: before merge they can show
  incompatible content. Choose a deliberate preview/release link strategy and verify it at both stages.
- [ ] Review module README links in generated Dokka, KDoc unresolved references, generated asset
  paths, and the API nav entry. A passing MkDocs build is only one part of this check.
- [ ] Inspect representative GitHub and MkDocs rendering: tables, Mermaid, Kotlin fences, alerts,
  details/summary contents, narrow-screen layout, and link destinations. Render claims require
  an actual rendering check, not inference from syntax alone.
- [ ] Add or wire strict docs validation for PRs and before deployment using the same preparation
  steps as local preview. Keep deployment separate from validation; avoid a workflow that ignores
  docs/config-only changes. Explain any remaining checks that require a human.
- [ ] Keep `docs/index.md`, `docs/api/`, `site/`, and local environment artifacts out of commits;
  confirm `.gitignore` covers the chosen process without hiding authored recovery pages.

Accept: the site builds with real inputs, old navigation has a recorded outcome, docs changes are
validated before merge, and both guide and API reading surfaces work.

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
