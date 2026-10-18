# Module :feature:home

**Purpose:** The template's reference feature — a Jetpack list with create/edit/delete, offline
support, and background sync. The one feature the [Guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/guide.md) walks through end to end.

## Key APIs

| API | What it does |
|---|---|
| `HomeScreen` / `HomeViewModel` | Jetpack list and swipe-to-delete; no undo action is implemented |
| `ItemScreen` / `ItemViewModel` | Create/edit a single jetpack; assisted-injected with the Nav3 `itemId` argument |

Destination signature excerpt; imports and function body are omitted.

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

## Current limitations

Home and Item can accumulate repository collectors after resubscription while their ViewModels
remain alive. Item currently ignores a failed save Result and can navigate back; its load catch
does not store the error. See [subscription lifetimes](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md#subscription-lifetimes)
and the [feature walkthrough](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/guide.md) for the current behavior and instructional alternatives.
A local write does not acknowledge remote delivery.

## Related Documentation

- [Guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/guide.md) — full walkthrough of this feature's ViewModel, screen, and navigation entries
- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Navigation](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/navigation.md) — assisted-inject ViewModels for keyed Nav3 arguments
- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md) — the `HomeRepository` this feature reads/writes through
