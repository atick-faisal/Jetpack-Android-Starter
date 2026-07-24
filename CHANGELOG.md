# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
