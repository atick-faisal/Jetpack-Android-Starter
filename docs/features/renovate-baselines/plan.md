---
slug: renovate-baselines
title: Renovate Baseline Auto-Commit
status: in-progress
created: 2026-10-18
---

# Renovate Baseline Auto-Commit

## Summary

Since Dependency Guard was added, every Renovate PR fails CI. Renovate bumps versions in
`gradle/libs.versions.toml` but cannot update the Dependency Guard baseline
(`app/dependencies/releaseRuntimeClasspath.txt`). The hosted Mend Renovate app does not run
`postUpgradeTasks`, so Renovate cannot regenerate the file itself. As a result, dependencies can
no longer be updated through Renovate.

This feature adopts the approach [Now in Android](https://github.com/android/nowinandroid) uses
in `.github/workflows/Build.yaml`:

- When the Dependency Guard check fails on a pull request from a branch in this repo, CI
  regenerates the baseline, commits it back to the PR branch, and continues with the remaining
  checks.
- Pull requests from forks still fail, with a message telling the contributor how to update the
  baseline.

The goal file asks the template to stay parallel to Now in Android, and this keeps it that way.
The baseline still gets recorded in the PR, so the Dependency Guard constraint holds.

## Tech Stack / Approach

**Pattern followed.** Now in Android's `Build.yaml`, step for step:

1. Run `dependencyGuard` with `continue-on-error: true`.
2. If it failed and the PR comes from a fork, fail with instructions.
3. If it failed and this is a pull request, run `dependencyGuardBaseline`.
4. Push the result with `stefanzweifel/git-auto-commit-action`, using the commit message
   `🤖 Updates baselines for Dependency Guard`.

Their Renovate config adds `gitIgnoredAuthors` so Renovate keeps rebasing branches that carry
the bot commit.

**Files touched:**

- `.github/workflows/ci.yml` — the `build` job (`🏗️ Build & Test`):
  - Replace the single `🔒 Check Dependency Baseline` step (lines 70-73) with the
    verify → guard → regenerate → push sequence.
  - Give this one job `permissions: contents: write`. The workflow-level permissions stay
    `contents: read` + `security-events: write`. A job-level block replaces the workflow-level
    one, so the job must also list `security-events: write` again, for the SARIF upload.
  - Keep the existing job setup: checkout@v7, `.github/ci-gradle.properties`, Temurin 21,
    `gradle/actions/setup-gradle@v6.3.0`.
- `.github/renovate.json` — add `gitIgnoredAuthors`, listing `renovate[bot]` (both the plain
  and the `29139614+` noreply forms) and `github-actions[bot]` (plain and `41898282+`). This
  matches Now in Android's config.
- Docs that describe the old "run the baseline task and commit it" behaviour:
  - `docs/build-and-tooling.md:86-88` (Renovate), `:187-200` (Dependency Guard), and `:319`
    (workflow table)
  - `docs/troubleshooting.md:383-391`
  - `AGENTS.md:237` (Build gotchas)

**Two deliberate departures from Now in Android:**

- **Non-PR events must still fail.** `ci.yml` also runs on
  `repository_dispatch: libs-versions-updated`. With `continue-on-error`, a guard failure
  on that event would be silently swallowed. Now in Android has the same gap on pushes to
  `main`. The fail step's condition must cover "failed and (fork or not a pull request)".
- **Checkout ref.** `git-auto-commit-action` documents that `pull_request` runs must check
  out `${{ github.head_ref }}`. The default checkout is the merge ref, and pushing that would
  put a merge commit on the branch. Checking out the head branch means CI tests the PR head,
  not the merge result. This needs to be confirmed during implementation and, if it holds,
  logged as a pitfall.

**Known tradeoffs, accepted with this approach:**

- **No CI run on the bot's commit.** A push made with `GITHUB_TOKEN` does not start a workflow
  run, so the bot's baseline commit has no check runs of its own. Now in Android has the same
  gap: PRs #1775 and #1767 show 0 checks on the head commit and were merged by a maintainer.
  The steps after the push (tests, build, lint, badging) do run on a working tree that already
  contains the new baseline, so that run effectively validates the final content. If
  `🏗️ Build & Test` is a required check in branch protection, these PRs must be merged as
  admin, or the check must be made non-required.
- **Gradle runs while holding a write token.** The job runs freshly bumped Gradle plugins with
  `contents: write`. Now in Android accepts this risk. The token is scoped to this repo, and it
  is only used on PRs from branches inside the repo.

**Badging stays blocking.** `:app:checkReleaseBadging` (line 107-110) is unchanged, as in Now
in Android. A dependency bump that changes merged-manifest permissions, such as a Firebase BOM
or Play Services major version, still fails CI. A human then runs `:app:updateReleaseBadging`
and reviews the permission change.

## Open Questions

## Resolved

### Q1: Which pull requests get the baseline auto-committed?

- [x] Renovate PRs only, gated on `github.actor == 'renovate[bot]'` (recommended) — fixes the
  stated problem. Human PRs still have to record their own dependency changes, which keeps the
  goal's guardrail constraint visible to people. The write token is only used on bot PRs.
- [ ] Every PR from a branch in this repo, as Now in Android does — exact parity and more
  convenient when you add a dependency yourself. The cost is that a human PR can change the
  classpath without its author ever looking at the baseline diff.

### Q2: How should the commit be pushed?

- [x] `stefanzweifel/git-auto-commit-action`, pinned to a version tag like the repo's other
  actions (recommended) — what Now in Android uses. It handles "nothing to commit" and sets the
  author to the triggering actor (`renovate[bot]`), which is what keeps Renovate rebasing.
- [ ] Plain `git config` / `git commit` / `git push` shell steps — no third-party action runs
  with the write token. The cost is about 8 lines of shell that re-implement the no-change
  check and author handling, and a departure from Now in Android.

### Q3: Should Kotlin, AGP and Compose bumps be held back for approval in the same change?

- [x] No, keep `group:all` as is and leave this to a separate change (recommended) — matches
  Now in Android's preset. Renovate PRs are not automerged here, so a human still reviews
  every bump before it lands.
- [ ] Yes, add a `packageRules` entry with `dependencyDashboardApproval: true` for Kotlin,
  AGP and Compose — enforces the goal's "versions change only after a conversation"
  constraint in the tool itself. The cost is that these bumps stop arriving until you approve
  them on the Dependency Dashboard, and the config drifts from Now in Android.

## Out of Scope

- Auto-updating the APK badging golden file. Permission changes stay a human decision, as in
  Now in Android.
- A separate workflow, a GitHub App, or a PAT secret for pushing. The built-in `GITHUB_TOKEN`
  is used, so forks and adopters need no new secrets. This respects the "nothing that gets in
  the way of an adopter" Non-Goal.
- Making Dependency Guard non-blocking, or guarding fewer configurations. Either would weaken
  the goal's CI guardrail constraint.
- Renovate `postUpgradeTasks`. They are not available on the hosted Mend app.
- Dependabot. There is no Dependabot config, and its PRs get a read-only token.
- Automerging Renovate PRs.
