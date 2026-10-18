# Module :core:android

**Purpose:** The lowest-level shared module — coroutine/error-handling utilities, dispatcher
qualifiers, and extension functions with no UI or domain dependency.

## Key APIs

| API | What it does |
|---|---|
| `suspendRunCatching` | `runCatching` for `suspend` functions that re-throws `CancellationException` instead of swallowing it — the repository-boundary error wrapper used everywhere |
| `OneTimeEvent<T>` | Atomic one-time consumption with non-consuming `peekContent()`; consumption does not acknowledge delivery |
| `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher` (`di/DispatcherModule.kt`) | Qualified `CoroutineDispatcher`s — always inject rather than hardcode `Dispatchers.IO` |
| `stateInDelayed` (`extensions/FlowExtensions.kt`) | `stateIn` with `WhileSubscribed(5000)`; collection requires a subscriber, and separate jobs launched by callbacks have their own lifetime |
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

- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md) — `suspendRunCatching` at the repository boundary
- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md) — `OneTimeEvent` inside `UiState`
