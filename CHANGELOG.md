# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `firebase.json`, so `firebase deploy --only firestore:rules` deploys `firestore.rules`.

## [2.0.0] - 2026-10-23

### Added

- Tracked `firestore.rules`: transitional rules that accept both the pre-2.0.0 7-field document
  and the new 8-field document with `serverUpdatedAt`, so older installs keep syncing during the
  rollout.
- Custom lint checks for design-system usage and test names, distributed through `core:ui`.
- Baseline profile generation and startup benchmarking in `:benchmarks`.
- Shared Robolectric/Compose test support, dispatcher rules, and fakes in `:core:testing`.
- Dependency Guard and APK badging checks for release dependency and manifest drift.
- Renovate PRs regenerate and commit the Dependency Guard baseline in CI.

### Changed

- Navigation uses Jetpack Navigation 3 with typed keys, multiple back stacks, and adaptive list/detail
  scenes.
- Bump Android Gradle Plugin to 9.4.1 and Kotlin to 2.4.21.
- Update dependencies, including Firebase BoM 35.1.0 and Room 2.8.5.
- Enable resource shrinking for release builds.
- Enable parallel IDE sync (`org.gradle.tooling.parallel`) for Gradle 9.4+.
- Rework setup, Firebase, release, feature-development, testing, customization, and local
  documentation-preview guidance to match the implementation and expose known limitations.
- Sync pulls use Firestore's `serverUpdatedAt`, stored locally as `serverUpdatedAtNanos`, instead of
  a device-clock cursor. This changes Room schema 1 → 2 and the remote document shape.

### Fixed

- Swipe dismissal invokes deletion from a Compose effect instead of during recomposition.
- Connectivity observation emits an initial active-network capability sample and suppresses
  duplicate states. This does not establish backend reachability.
- Item price editing preserves raw text, including empty and partial input. Submit parses the
  text, with invalid or empty input becoming `0.0`; no non-negative-price validation is added.
- Robolectric tests open `jdk.internal.access`, which Robolectric 4.17 needs to set up its sandbox.

### Removed

- Unused public `DarkThemeConfigSerializer`. Adopters using that symbol must replace their references;
  this removal does not add DataStore corruption recovery.
- `FirebaseJetpack`'s Kotlin serialization `@Serializable` annotation. Adopters using its generated
  serializer need a replacement; Firestore mapping remains in use.
- The no-op `onVariants` version assignment. Custom APK filenames remain unimplemented.

### Upgrade considerations

- **Local data loss:** Room has no registered 1 → 2 migration and uses destructive fallback.
  Upgrading can erase local rows, including edits and deletions not yet uploaded. Remote re-fetching
  cannot recover changes that never reached the server. Plan a data-preserving migration for
  existing installs; none is supplied here.
- **Backend compatibility:** Legacy Firestore documents missing `serverUpdatedAt` are excluded by
  the new query, even with a zero cursor. The updated example rules require that field to equal
  `request.time` and reject older writers that omit it. Existing projects need a separately planned
  backfill and coordinated client/rules rollout. Editing the documented rules deploys nothing.
- Review [existing-install guidance](docs/firebase.md#existing-installs-and-documents),
  [sync limitations](docs/data.md#delivery-and-conflict-limitations), and
  [state/error-handling limitations](docs/state-management.md) before adopting these changes.

## [1.3.0] - 2026-07-24

### Added

- Gradle toolchains resolver for consistent JVM configuration

### Changed

- Migrate to Android Gradle Plugin 9.1.0
- Bump `compileSdk` to 37
- Update `OssLicensesMenuActivity` to v2
- Refactor `SettingsDialog` and `openPermissionSettings` to use modern APIs
- Update all dependencies to their latest versions

### Fixed

- Resolve Hilt `kotlin-metadata.jvm` dependency error
- Suppress Compose lint warnings for `LocalContext` in suspend callbacks
- Fix AGP 9.0 compatibility with Dokka and OSS licenses plugin
- Fix code inspection issues
