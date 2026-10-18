# Jetpack Android Starter

## Problem

Starting a production Android app means rebuilding the same foundation every time: a
multi-module Gradle build, DI, local storage, networking, Firebase auth and sync, navigation,
test support, CI/CD and release signing. Doing it by hand is slow and easy to get subtly wrong.
This repo is a template that provides that foundation so a team can start on its own features.
The example app code (the `home`/Jetpack feature and its neighbours) shows the patterns working
end to end. It is not the product.

## Success Criteria

- A fresh clone builds and opens the app before the developer configures their own Firebase project.
- `just verify` (the same checks CI runs) passes on `main`.
- A developer can add a new feature by following `docs/guide.md`, without improvising the module
  or file layout.
- The template stays parallel to [Now in Android](https://github.com/android/nowinandroid): when
  it adopts a new library or pattern (as with the Navigation 3 migration), the template follows its
  approach, while keeping its own opinionated layer (`UiState` + `StatefulComposable`, two-layer
  MVVM).
- The docs match the code: every pattern, snippet and limitation they describe is true of the
  current implementation.
- Limitations that affect a shipped app (sync conflicts, error-event delivery, debug-key signing
  fallback, upgrade compatibility) are documented before the reader reaches release.
- Adopting the template is how a team adopts Now in Android's best practices: after the setup
  steps in `docs/getting-started.md`, their first change is their own feature, not boilerplate
  or build setup.

## Non-Goals

- Adding application code, or tests for application code, beyond what is needed to demonstrate
  a pattern. Example features are not grown for their own sake.
- A domain layer. It is intentionally omitted until a real app needs one.
- Anything that gets in the way of an adopter starting their own app. Turning the template into
  an adopter's app takes minimal cleanup and refactoring, done with the template's refactor tool
  (to be added to this repo).

## Constraints

- Stack: Kotlin, Jetpack Compose + Material 3, Hilt, Room + DataStore, Retrofit/OkHttp,
  Firebase, WorkManager, Navigation 3. Gradle Kotlin DSL, version catalog, convention plugins, JDK 21.
- Two-layer MVVM (UI + Data). Feature modules never depend on each other.
- Kotlin, AGP, Compose and min/target SDK versions change only after a conversation.
- CI guardrails (Dependency Guard, APK badging) fail any PR whose dependency or manifest change
  is not recorded in the baselines.
- The guides (MkDocs) and API reference (Dokka) are published from this repo and validated in CI.
- Apache 2.0. Downstream apps start by cloning or forking this repo.
