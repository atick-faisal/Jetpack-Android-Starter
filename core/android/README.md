# Module :core:android

**Purpose:** The lowest-level shared module — coroutine/error-handling utilities, dispatcher
qualifiers, and extension functions with no UI or domain dependency.

## Key APIs

| API | What it does |
|---|---|
| `suspendRunCatching` | `runCatching` for `suspend` functions that re-throws `CancellationException` instead of swallowing it — the repository-boundary error wrapper used everywhere |
| `OneTimeEvent<T>` | Thread-safe (`AtomicBoolean`) wrapper so an event fires once even if observed from multiple recompositions |
| `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher` (`di/DispatcherModule.kt`) | Qualified `CoroutineDispatcher`s — always inject rather than hardcode `Dispatchers.IO` |
| `stateInDelayed` (`extensions/FlowExtensions.kt`) | `stateIn` preset to `WhileSubscribed(5000)` |
| `isEmailValid`, `isPasswordValid`, `isValidFullName` (`extensions/StringExtensions.kt`) | Form-input validators used by `feature:auth` |

```kotlin
// core/android/src/main/kotlin/dev/atick/core/utils/CoroutineUtils.kt
suspend inline fun <T> suspendRunCatching(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
```

## Related Documentation

- [Data Layer](../../docs/data.md) — `suspendRunCatching` at the repository boundary
- [State Management](../../docs/state-management.md) — `OneTimeEvent` inside `UiState`
