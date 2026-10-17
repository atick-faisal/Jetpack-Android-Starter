# Module :core:navigation

**Purpose:** Jetpack Navigation 3 multi-back-stack state and a `Navigator` for changing it.
Destination keys implement `NavKey`; screens register entries in the app’s entry provider.

## Key APIs

| API | What it does |
|---|---|
| `NavigationState` / `rememberNavigationState` | One back stack per top-level destination (`subStacks`), plus a `topLevelStack` recording visit order |
| `Navigator` | The intended mutation API for `NavigationState` (its stacks are publicly accessible) — `navigate(key)`, `goBack()`, `canGoBack()` |
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

- `Navigator` deduplicates equal keys within the current sub-stack, not across tabs. Different
  arguments of the same data-class key can identify different destinations.
- In the pinned Navigation 3 version, `NavEntry.contentKey` defaults to `key.toString()`. Duplicate
  content keys in flattened stacks can collide in saved state and ViewModel ownership; sharing a
  key type alone is not a collision. See [entry identity](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/navigation.md#entry-identity-and-duplicate-destinations).

## Related Documentation

- [Navigation](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/navigation.md) — the full Nav3 model: defining destinations, registering entries, `ListDetailSceneStrategy`, assisted-inject ViewModels for keyed args
- [Guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/guide.md) — wiring a new feature's entries into `NavDisplay`
