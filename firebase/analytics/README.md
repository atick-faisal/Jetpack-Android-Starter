# Module :firebase:analytics

**Purpose:** Crash reporting via Firebase Crashlytics, behind one small abstraction.

> [!NOTE]
> Despite the module name, this is **Crashlytics** (crash reporting), not Firebase Analytics (event
> tracking) — no analytics events are logged anywhere in this template.

## Key APIs

| API | What it does |
|---|---|
| `CrashReporter` / `FirebaseCrashReporter` | `reportException(throwable)` → `crashlytics.recordException(throwable)` |

```kotlin
// firebase/analytics/src/main/kotlin/dev/atick/firebase/analytics/utils/FirebaseCrashReporter.kt
override fun reportException(throwable: Throwable) {
    crashlytics.recordException(throwable)
}
```

Uncaught crash collection is handled by the SDK. `reportException` records a non-fatal exception;
handled UI errors are reported only when the app invokes the reporting action. Collection and
release mapping upload depend on the Firebase/build configuration described in the guide.

## Related Documentation

- [Firebase Setup § Crashlytics](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/firebase.md#crashlytics) — mapping-file upload, how to confirm reporting end-to-end
- [Build & Tooling](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/build-and-tooling.md) — `FirebaseConventionPlugin`
