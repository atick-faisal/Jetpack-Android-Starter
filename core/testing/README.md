# Module :core:testing

**Purpose:** Shared JVM test helpers — a `MainDispatcherRule` and in-memory fakes for two data
sources — added as a `testImplementation` by the modules that need them (`data`, `core/ui`,
`feature/settings` today; not wired into every module automatically).

## Key APIs

| API | What it does |
|---|---|
| `MainDispatcherRule` | Swaps `Dispatchers.Main` for a `TestDispatcher` (`UnconfinedTestDispatcher` by default) for the test's duration — needed because `updateStateWith`/`updateWith` dispatch to Main via `viewModelScope` |
| `FakeUserPreferencesDataSource` | In-memory `UserPreferencesDataSource` backed by a real `MutableStateFlow`, so a test can write through the interface and assert on what a collector observes |
| `FakeAuthDataSource` | In-memory `AuthDataSource` fake, same pattern |

```kotlin
// core/testing/src/main/kotlin/dev/atick/core/testing/rule/MainDispatcherRule.kt
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `does the thing`() = runTest { /* ... */ }
}
```

## Gotchas

- These are fakes, not mocks — real state in a `MutableStateFlow` — because that's the behavior
  that matters for a DataStore-backed source, and a mock can't express it.
- Not every module gets the generic JUnit4/Turbine/Truth/Robolectric stack (`AndroidTest.kt`) *plus*
  these fakes for free — the generic stack is automatic, `core:testing` itself is an opt-in
  `testImplementation`.

## Related Documentation

- [State Management § Testing](../../docs/state-management.md#testing) — `StatefulComposableTest`, which exercises `updateState`/`updateStateWith`/`updateWith` using this rule
- [Architecture § Testing](../../docs/architecture.md#testing) — the generic Robolectric + Compose stack every module gets from `AndroidTest.kt`
