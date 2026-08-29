# Module :benchmarks

**Purpose:** Generates the release build's baseline profile and measures the cold-start improvement
it produces, using a Gradle Managed Device so CI doesn't need a physical or manually-started emulator.

## Key APIs

| API | What it does |
|---|---|
| `BaselineProfileGenerator` | `BaselineProfileRule` that drives app start → theme → Compose setup → navigation → first frame, deliberately nothing past startup (see Gotchas) |
| `StartupBenchmark` | `MacrobenchmarkRule` comparing `timeToInitialDisplayMs` with `CompilationMode.None()` vs. `Partial(baselineProfileMode = Require)` |

```kotlin
// benchmarks/src/main/kotlin/dev/atick/benchmarks/BaselineProfileGenerator.kt
@Test
fun generate() = baselineProfileRule.collect(
    packageName = PACKAGE_NAME,
    includeInStartupProfile = true,
) {
    pressHome()
    startActivityAndWait()
    device.waitForIdle()
}
```

## Gotchas

- Startup-only is deliberate: most adopters delete `:feature:home` in their first hour, and a
  generator that also drives those screens would break the moment they do.
- Regenerate with `./gradlew :app:generateReleaseBaselineProfile`; run the comparison with
  `./gradlew :benchmarks:pixel6Api33BenchmarkReleaseAndroidTest`.

## Related Documentation

- [Build & Tooling § Gradle Managed Devices](../docs/build-and-tooling.md#gradle-managed-devices) — the `pixel6Api33` device definition and baseline-profile task table
