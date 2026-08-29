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

## Related Documentation

- [Firebase Setup § Crashlytics](../../docs/firebase.md#crashlytics) — mapping-file upload, how to confirm reporting end-to-end
- [Build & Tooling](../../docs/build-and-tooling.md) — `FirebaseConventionPlugin`
