# Renovate Baseline Auto-Commit — Tasks

| ID | Description | Complexity | Suggested Model | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| RENOVATE-BASELINES-01 | In `ci.yml`'s `build` job, swap the Dependency Guard step for verify → fail-on-fork-or-non-PR → regenerate → `git-auto-commit-action` push, gated to `renovate[bot]`; add job-level `contents: write` + `security-events: write`; add `gitIgnoredAuthors` to `.github/renovate.json` | moderate | sonnet | done | Confirm the `head_ref` checkout requirement. `ci.yml` ignores `.github/workflows/**`-only PRs, so this PR may not run CI on itself |
| RENOVATE-BASELINES-02 | Update docs for the new behaviour: `docs/build-and-tooling.md` (Renovate, Dependency Guard, workflow table), `docs/troubleshooting.md`, `AGENTS.md` Build gotchas | small | sonnet | pending | Depends on -01 |
