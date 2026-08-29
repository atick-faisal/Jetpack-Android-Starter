# Module :core:navigation

**Purpose:** The Jetpack Navigation 3 state model — one class allowed to mutate navigation state,
one class that holds it. No navigation graph resource, no route strings.

## Key APIs

| API | What it does |
|---|---|
| `NavigationState` / `rememberNavigationState` | One back stack per top-level destination (`subStacks`), plus a `topLevelStack` recording visit order |
| `Navigator` | The only thing that mutates `NavigationState` — `navigate(key)`, `goBack()`, `canGoBack()` |
| `NavigationState.backStack` | The flattened list `NavDisplay` renders — every visited top-level destination's stack, in visit order |
| `rememberDefaultEntryDecorators()` | `rememberSaveableStateHolderNavEntryDecorator` + `rememberViewModelStoreNavEntryDecorator` — per-destination saved state and `ViewModelStore` |

```kotlin
// core/navigation/src/main/kotlin/dev/atick/core/navigation/Navigator.kt
fun navigate(key: NavKey) {
    when (key) {
        state.currentTopLevelKey -> clearSubStack()
        in state.topLevelKeys -> goToTopLevel(key)
        else -> goToKey(key)
    }
}
```

## Gotchas

- A non-top-level key must belong to exactly one top-level destination. Reusing the same key type
  across two tabs puts it in `backStack` twice, and Nav3 identifies an entry by `NavEntry.contentKey`
  (defaults to the key), so both copies would share one `rememberSaveable` bundle and `ViewModelStore`.

## Related Documentation

- [Navigation](../../docs/navigation.md) — the full Nav3 model: defining destinations, registering entries, `ListDetailSceneStrategy`, assisted-inject ViewModels for keyed args
- [Guide](../../docs/guide.md) — wiring a new feature's entries into `NavDisplay`
