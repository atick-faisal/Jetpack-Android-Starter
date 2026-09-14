# Documentation repair plan

Make the documentation useful to someone adopting or maintaining this project: accurate setup steps,
complete explanations of the important patterns, and comments that help the next reader understand
the code. Keep the useful updates from this branch and recover guidance that was cut without an
adequate replacement.

Implementation checklist: [DOC_FIX_TASKS.md](DOC_FIX_TASKS.md).

## Audit scope

Reviewed on 2026-09-12:

- Branch: `fix/docs-v3`.
- Open [PR #647](https://github.com/atick-faisal/Jetpack-Android-Starter/pull/647),
  “Refactor the documentation to match the codebase.”
- PR base: `1c2103890d16f81f6c6ae4c2d8626281b841e417` (`main`).
- Reviewed head: `0fb7882982cfefd55df300e06549731c6fbb33bb`.
- Diff: **90 files, 3,734 insertions, 15,711 deletions**. Fourteen tracked pages were removed from
  `docs/`, including the generated homepage source. Ten authored pages remain.

The audit read the current ten guides, nineteen module READMEs, root guidance, previous plan/task
records, PR description, deleted-page inventories and selected original sections, and the relevant
implementation/build files. This is a documentation audit with targeted code inspection, not a full
runtime correctness review. Remaining section-by-section recovery work is explicit in the checklist.

GitHub reports successful build/test and lint checks at this head. The PR and old task log also
report successful Dokka and strict MkDocs builds. Those historical results were not rerun for this
planning change; they do not establish that the prose, examples, or upgrade instructions are correct.

## What went wrong

The previous plan made document count, line count, and alert budgets acceptance criteria. It also
required “why, not what” comments and rejected all illustrative examples. Those rules encouraged
short reference inventories in places that needed instructions, and long justifications in places
that needed a sentence explaining behavior. Some newly added statements attribute intent without
evidence; others describe a known limitation and then reassure the reader that it is safe.

The original docs also contained obsolete Navigation 2 examples, nonexistent APIs, repetition, and
incorrect claims about missing tests. Recover useful information by topic, checking it against the
current code. Restoring whole old files would restore those errors too.

## Goals

1. **Help readers finish a task.** A fresh adopter should be able to build, configure Firebase, add a
   feature, test it, customize the UI, and prepare a release without reconstructing missing steps
   from source files or the previous planning log.
2. **Describe behavior precisely.** Separate guarantees, assumptions, limitations, and possible bugs.
   Check both new prose and inherited claims on pages the branch rewrote.
3. **Write with respect for the reader.** Explain the relevant behavior or constraint directly. Keep
   useful rationale, but remove invented motives, lectures, defensive comparisons, and accounts of
   how the previous refactor was performed.
4. **Recover depth where it helps.** Give each subject a main home, with enough local context at the
   point of use. Tutorials, reference tables, examples, and troubleshooting serve different needs.
   Document size is an observation, not a completion criterion.
5. **Keep every reading surface usable.** Review GitHub Markdown, the MkDocs site, Dokka module
   pages, source KDoc, root instructions, and links to removed pages.
6. **Make completion evidence reproducible.** Track missing content, changed claims, tested commands,
   remaining behavior concerns, and the actual revision validated.

## Findings that drive the work

“Added” means this branch introduced the wording or behavior. “Retained” means an older problem
survived the rewrite. Recovery gaps concern material removed or reduced by this branch.

| Finding | Evidence and consequence | Work |
|---|---|---|
| Added defensive comment policy | `CONTRIBUTING.md` and `PLAN.md` prescribe rationale-only comments and an emoji/alert vocabulary. `app/proguard-rules.pro` calls the rule's breadth “deliberate” without establishing its design history. | DF-01, DF-12 |
| Added false sync assurances | `Sync.initialize`, `sync/README.md`, and `docs/data.md` call dropped requests safe. `HomeRepositoryImpl.sync()` takes one pending-row snapshot; it does not keep draining new writes or guarantee another run. Its pull unconditionally upserts rows. Push-before-pull alone does not protect edits made during sync. | DF-02, DF-06 |
| Added contradictory timestamp explanations | `docs/data.md` and `core/room/README.md` call local timestamps display-only, while `JetpackDao.getUnsyncedJetpacks()` compares `lastUpdated > lastSynced`. Remote mapping also carries `lastSynced`; calling it “local only” is misleading. | DF-06 |
| Added incorrect corruption guidance | `core/preferences/README.md` promises a default-value fallback. `UserDataSerializer.readFrom()` throws `CorruptionException`; `DatastoreModule` installs no corruption handler. Storage is JSON (`user_preferences.json`), despite “Proto-style” descriptions. | DF-06, DF-11 |
| Recovery gap in Firebase setup | `docs/firebase.md` names `GOOGLE_WEB_CLIENT_ID` but never tells the reader to set it in `local.properties`; its linked build guide does not supply that step. `firebase/auth/build.gradle.kts` and `cd.yml` show the actual wiring. | DF-03 |
| Recovery gap in release setup | Deleted `docs/github.md` explained secret encoding; deleted `docs/fastlane.md` covered authentication, `Appfile`, and metadata. The replacement lists five secret names and links to generated `fastlane/README.md`, which only lists commands. | DF-04 |
| Recovery gap in local documentation workflow | Deleted `docs/tips.md` explained local preview. `docs/index.md` is now absent on a clean checkout, and `docs/api/` must be generated. The full assembly process lives in `docs.yml` and historical `TASKS.md`, not an adopter guide. | DF-04, DF-14 |
| Recovery gaps in reference material | Compose metrics flags, DI construction/qualifiers, component customization/accessibility, and the module dependency map lost their useful explanations or examples. Naming a source file does not replace those instructions. | DF-05, DF-06, DF-09 |
| Retained architecture inaccuracies | `docs/architecture.md` still describes periodic WorkManager sync, contains fictional `HomeAction`/`Data` examples, and has no actual Gradle module graph despite many links promising one. | DF-05 |
| Added and retained state-contract errors | `docs/state-management.md` says synchronous `updateState` does not replace data, although it does. Existing helper KDoc claims thrown exceptions are caught, but the helpers inspect returned `Result`s. Added comments defend the snackbar effect key while acknowledging event consumption during composition. | DF-02, DF-07 |
| Retained failure paths copied into the tutorial | `ItemViewModel.createOrUpdateJetpack()` ignores the repository's `Result` and returns success; `getJetpack().catch` constructs a state without emitting/storing it. Both need explicit disposition before the guide promises successful-save navigation and automatic error reporting. | DF-02, DF-07, DF-08 |
| Retained obsolete and unsupported UI claims | `core/ui/README.md` and troubleshooting still describe Screen/Route wrappers; current destinations inject the ViewModel in a `Screen`. `feature/home/README.md` promises undo without an undo path in the screen/ViewModel. | DF-09, DF-10, DF-11 |
| Added comment advice contains wrong symbols | All three theme suppression comments recommend `LocalGradientTheme`; the declared symbol is `LocalGradientColors`. Auth-provider discovery and suppression explanations assert SDK behavior that needs source verification. | DF-12 |
| Behavioral changes need visible compatibility notes | The PR changes Room schema 1 → 2, the Firestore query field/model and example security rules, price editing, connectivity emission, and swipe deletion. The old record assumes adopters have no old Firestore documents. Existing data and unsynced local edits require an explicit compatibility assessment. | DF-02, DF-06, DF-13 |
| Passing site checks leave coverage gaps | Removing files fixes internal links only if all references are updated; it does not preserve published URLs. Docs deployment only runs on `main` and uses `mkdocs gh-deploy` without strict validation. Branch-specific source links currently point at `main`. | DF-14, DF-15 |

## Content recovery map

Use the base revision above to recover the old sections. The destination is a responsibility, not
a requirement to recreate the old page. DF-00 records the final disposition of each useful section.

| Removed source | Recover or preserve | Intended home |
|---|---|---|
| `docs/data-flow.md` | Choosing local, REST, or offline-first data; useful flow diagrams. Label proposed caching/listener patterns as extensions if retained. | `docs/data.md`, `docs/architecture.md` |
| `docs/dependency-injection.md` | Actual `@Provides` construction, `@Binds`, qualifiers, scopes, module wiring, testing with fakes. Exclude fictional APIs/scopes presented as existing. | `docs/data.md`, `docs/guide.md` |
| `docs/dependency.md` | Adding a catalog alias and adopting the actual Renovate configuration. Do not restore the nonexistent Dependabot config. | `docs/build-and-tooling.md` |
| `docs/faq.md` | Unique adopter questions about customization, multiple operations, storage choice, release readiness. Preserve answers only after code checks. | Relevant guide sections; troubleshooting for actual symptoms |
| `docs/fastlane.md` | Play authentication, package/key paths, lane configuration, metadata, release preparation. Refresh old Ruby/lane examples. | `docs/build-and-tooling.md` release section |
| `docs/github.md` | Secret formats and destinations, tag workflow, release troubleshooting. | `docs/build-and-tooling.md` |
| `docs/index.md` | Useful reading paths and module-map information, rebuilt from current dependencies. Keep the homepage generated from root README. | `README.md`, `docs/architecture.md`, site assembly instructions |
| `docs/license.md` | Preserve access to the authoritative license and the old published URL where practical. | Root `LICENSE` link and site compatibility handling |
| `docs/performance.md` | Compiler metrics/reports, how to inspect their output, profiling and current baseline-profile workflow. Exclude “profiles planned” and unimplemented monitoring claims. | `docs/build-and-tooling.md`, UI-specific guidance in `docs/components.md` |
| `docs/philosophy.md` | Helpful explanations of tradeoffs and when to extend the template, without repeating architecture slogans. | `docs/architecture.md` |
| `docs/plugins.md` | Applying the existing plugins, customization points, and how module setup fits together. | `docs/build-and-tooling.md`, `docs/guide.md` |
| `docs/quick-reference.md` | Discoverable commands and actual extension functions. Replace old navigation examples. | Build command reference; links from `README.md` |
| `docs/spotless.md` | Exact invocation, file targets, header template locations and customization guidance. | `docs/build-and-tooling.md` |
| `docs/tips.md` | Local docs preview, actual secrets keys, locale setup, previews, validation utilities. Replace invented `API_KEY` examples with real keys. | Build, Firebase, components, and feature guides |

Also recover useful material cut from surviving pages and module READMEs: component accessibility
and extension steps, Room migration guidance, DataStore construction/evolution, `UiText`, permission
helpers, sync progress, and real test examples. Some old examples were wrong; preserve the reader
need and replace the example.

## Writing standard

- Lead a section with what the reader needs to understand or do. Provide steps, expected results,
  and relevant failure cases before optional internals.
- A comment can explain behavior, units, lifetime, ordering, a constraint, or a reason. Use the
  shortest explanation that preserves the important fact. Move longer background into a guide
  and link to it where useful.
- Attribute intent only when there is evidence. “This currently does X” is often all that is needed.
  Describe limitations directly; do not turn them into reasons to approve the implementation.
- Remove phrases such as “which is the whole point,” “the entire contract in one flag,” “nothing
  more,” and “most adopters delete this in their first hour” when they add no useful information.
  Avoid absolute “always,” “never,” “safe,” and “exactly once” claims beyond what is established.
- Use examples from the code when teaching an existing pattern. Clearly label shortened excerpts
  and illustrative additions. A tutorial may show a new feature; it must explain what to add and
  must not pretend an invented symbol already exists.
- Keep API contracts in KDoc, and preserve useful parameter descriptions and links. Ordinary
  Markdown links and lists are appropriate in docs; source comments need no severity taxonomy.
- Keep warnings where they help someone avoid a concrete mistake. Choose emphasis by need, with
  no page quota or repository-wide emoji target. Preserve the project's existing personality.

For the comment that prompted this audit, a starting point is:

```proguard
# Keeps all @Serializable classes and their members, including navigation keys.
```

That states the rule's effect. DF-12 separately verifies whether the linked navigation workaround
still applies before retaining a stronger claim about why the rule is needed. This example does
not propose changing the keep rule.

## Delivery order and boundaries

1. Record coverage and replace the writing policy (DF-00–01).
2. Resolve factual uncertainty and identify runtime follow-ups (DF-02).
3. Repair setup/release paths, architecture/data/state references, and the feature tutorial
   (DF-03–08).
4. Restore UI guidance, repair troubleshooting and module references, and edit source comments
   (DF-09–12).
5. Reconcile root guidance and release notes, repair site navigation/validation, then perform the
   final reader walkthrough and PR review (DF-13–15).

This planning change creates only the two `DOC_FIX_*.md` files. During implementation, keep editorial
changes separately reviewable from runtime fixes. Finding a bug does not justify silently changing
the sync protocol, state helpers, security rules, dependencies, or database migration policy. Record
such work under DF-02; either fix it in an explicitly scoped change or document its limitation and
link the follow-up. Follow the repository's existing approval boundaries for protected configuration.

`PLAN.md` and `TASKS.md` remain historical evidence. The new files own the repair work; the older
“complete” status and numeric targets must not govern it. Update the PR description only after the
repair is reviewable and the user authorizes that external update.

## Done when

A reader can complete the documented workflows; recovered topics have explicit destinations;
examples and contracts agree with the reviewed code; comments explain without defending; old links
have a recorded disposition; GitHub, MkDocs, and Dokka render correctly; and checks have results
attached to the revision tested. Any unresolved runtime concern is visible with a bounded follow-up.
