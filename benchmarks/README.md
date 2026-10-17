# Module :benchmarks

**Purpose:** Generates a release baseline profile and compares cold-start timings with and
without it. The configured generation device is a Gradle Managed Device; these tasks are not
automatically run by the current CI workflows.

## Key APIs

| API | What it does |
|---|---|
| `BaselineProfileGenerator` | `BaselineProfileRule` that presses Home, starts the activity and waits for idle; no in-app screen journey |
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

- Regenerate with `./gradlew :app:generateReleaseBaselineProfile`. Generation is not automatic
  during app builds. Add representative interaction journeys when extending the app.
- Run only the comparison class with:

  ```bash
  ./gradlew :benchmarks:pixel6Api33BenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.atick.benchmarks.StartupBenchmark
  ```

- These tasks boot an emulator and require its SDK image and virtualization. Compare
  `timeToInitialDisplayMs` between compilation modes; improvement is not guaranteed. Use a physical
  device for representative timing. The build guide lists generated profiles and result locations.

## Related Documentation

- [Build & Tooling § Gradle Managed Devices](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/build-and-tooling.md#gradle-managed-devices) — the `pixel6Api33` device definition and baseline-profile task table
