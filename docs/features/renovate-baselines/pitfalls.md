# Renovate Baseline Auto-Commit — Pitfalls

- [x] [RENOVATE-BASELINES-01] `git-auto-commit-action` on a `pull_request` run needs `actions/checkout` with `ref: github.head_ref`, because the default checkout is a detached merge ref (only stated in the action README) — without it the baseline push fails or puts GitHub's merge commit on the Renovate branch
