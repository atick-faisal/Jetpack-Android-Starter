# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Migrate navigation to Jetpack Navigation 3 (`NavKey`, `Navigator`, `NavDisplay`,
  multi-back-stack `NavigationState`, `ListDetailSceneStrategy`)
- Add `:lint` module with custom lint checks (`DesignSystemDetector`, `TestMethodNameDetector`),
  shipped to consumers via `lintPublish`
- Add baseline profile generation and startup benchmarking (`:benchmarks`,
  `BaselineProfileGenerator`, `StartupBenchmark`)
- Add a Robolectric + Compose test harness (`:core:testing`, `MainDispatcherRule`, fakes) and
  ~110 reference tests across the repository, ViewModel, and DAO layers
- Add Dependency Guard, pinning the release runtime classpath to a committed baseline
- Add APK badging, diffing `aapt2 dump badging` output against a committed golden file to catch
  manifest/permission drift

### Changed

- Bump Android Gradle Plugin to 9.3.1

### Fixed

- Order sync pulls by a Firestore server timestamp instead of `System.currentTimeMillis()`.
  The pull cursor was the largest local `lastUpdated`, a device clock, so a single device running
  fast moved every other device's cursor into the future and records written by correctly-clocked
  devices were dropped from every later pull, silently and permanently. `FirebaseJetpack` now
  carries a server-assigned `serverUpdatedAt`, stored locally as `JetpackEntity.serverUpdatedAtNanos`
  and read back by `JetpackDao.getSyncCursor`. Room schema 1 → 2, handled by the existing
  destructive-migration fallback. The Firestore security rules in `docs/firebase.md` gained the
  field, pinned to `request.time` so a client cannot forge the ordering.

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
