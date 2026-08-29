# Module :feature:home

**Purpose:** The template's reference feature — a Jetpack list with create/edit/delete, offline
support, and background sync. The one feature the [Guide](../../docs/guide.md) walks through end to end.

## Key APIs

| API | What it does |
|---|---|
| `HomeScreen` / `HomeViewModel` | Jetpack list, swipe-to-delete with undo |
| `ItemScreen` / `ItemViewModel` | Create/edit a single jetpack; assisted-injected with the Nav3 `itemId` argument |

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemScreen.kt
@Composable
internal fun ItemScreen(
    itemId: String?,
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    // Navigation 3 passes destination arguments through the NavKey rather than a
    // SavedStateHandle, so the id is handed to the ViewModel by assisted injection.
    itemViewModel: ItemViewModel = hiltViewModel<ItemViewModel, ItemViewModel.Factory>(
        creationCallback = { factory -> factory.create(itemId) },
    ),
)
```

## Related Documentation

- [Guide](../../docs/guide.md) — full walkthrough of this feature's ViewModel, screen, and navigation entries
- [State Management](../../docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Navigation](../../docs/navigation.md) — assisted-inject ViewModels for keyed Nav3 arguments
- [Data Layer](../../data/README.md) — the `HomeRepository` this feature reads/writes through
