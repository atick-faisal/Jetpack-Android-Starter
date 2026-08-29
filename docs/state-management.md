# State Management

Every screen exposes one `StateFlow<UiState<ScreenData>>` from its ViewModel. `UiState<T>` wraps the
screen's data with a loading flag and a one-time error event, three extension functions cover every
kind of update, and `StatefulComposable` turns that single stream into loading indicator, error
snackbar, and content — automatically, with no per-screen boilerplate.

## Concepts

| Type | What it does | Where |
|---|---|---|
| `UiState<T>` | Wraps screen data with a `loading` flag and a one-time `error` event | `core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt` |
| `StatefulComposable` | Renders `content`, overlays a loading indicator, surfaces the error as a snackbar | same file |
| `updateState` | Synchronous state update, no coroutine | same file |
| `updateStateWith` | Async update that replaces `data` with a new value | same file |
| `updateWith` | Async update that performs a side effect without returning new data | same file |
| `OneTimeEvent<T>` | Delivers its content exactly once, even if observed multiple times | `core/android/src/main/kotlin/dev/atick/core/utils/OneTimeEvent.kt` |

## The `UiState<T>` wrapper

```kotlin
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null),
)
```

`data` is always present, even mid-request or after a failed one — screens never null-check their way
through a loading state. Without this wrapper, every ViewModel re-invents the same three flows:

```kotlin
// ❌ One flow per concern - the three states can drift out of sync
private val _posts = MutableStateFlow<List<Post>>(emptyList())
private val _isLoading = MutableStateFlow(false)
private val _error = MutableStateFlow<String?>(null)
```

```kotlin
// ✅ One flow, one source of truth
private val _uiState = MutableStateFlow(UiState(PostsScreenData()))
val uiState = _uiState.asStateFlow()
```

`StatefulComposable` is the other half — it reads that single stream and renders all three states:

```kotlin
// core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt
@Composable
fun <T : Any> StatefulComposable(
    state: UiState<T>,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    content: @Composable (T) -> Unit,
) {
    content(state.data)

    if (state.loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            ContainedLoadingIndicator(modifier = Modifier.size(56.dp).align(Alignment.Center))
        }
    }

    state.error.getContentIfNotHandled()?.let { error ->
        LaunchedEffect(onShowSnackbar) {
            onShowSnackbar(error.message.toString(), SnackbarAction.REPORT, error)
        }
    }
}
```

## Update functions

| Function | Use when | Replaces `data`? | Async? |
|---|---|---|---|
| `updateState` | Reacting to current state, no I/O (form input, toggles) | No | No |
| `updateStateWith` | An operation returns new screen data (load, search, save-and-return) | Yes | Yes |
| `updateWith` | An operation has no new data to show (delete, save settings) | No | Yes |

> [!TIP]
> `updateStateWith` and `updateWith` both guard against duplicate requests — `if (value.loading) return`
> at the top of each — and clear any pending error event before starting. A second tap while a request
> is in flight is a no-op, not a duplicate network call.

### `updateState`

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemViewModel.kt
fun updateName(name: String) {
    _itemUiState.updateState { copy(jetpackName = name) }
}

fun updatePrice(priceString: String) {
    val price = priceString.trim().toDoubleOrNull() ?: return
    _itemUiState.updateState { copy(jetpackPrice = price) }
}
```

### `updateStateWith`

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemViewModel.kt
fun createOrUpdateJetpack() {
    _itemUiState.updateStateWith {
        val jetpack = Jetpack(id = jetpackId, name = jetpackName.trim(), price = jetpackPrice)
        homeRepository.createOrUpdateJetpack(jetpack)
        Result.success(copy(navigateBack = OneTimeEvent(true)))
    }
}
```

Sets `loading = true`, runs the block, then either replaces `data` on success or sets `error` and
keeps the old `data` on failure — `loading` returns to `false` either way.

### `updateWith`

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeViewModel.kt
fun deleteJetpack(jetpack: Jetpack) {
    _homeUiState.updateWith {
        homeRepository.markJetpackAsDeleted(jetpack)
    }
}
```

Same lifecycle as `updateStateWith`, but `data` is left untouched on success — only `loading` and
`error` change.

## Context parameters

`updateStateWith` and `updateWith` are declared `context(viewModel: ViewModel)`, so they can reach
`viewModelScope` without every call site passing it in:

```kotlin
// core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt
context(viewModel: ViewModel) inline fun <reified T : Any> MutableStateFlow<UiState<T>>.updateStateWith(
    crossinline operation: suspend T.() -> Result<T>,
) {
    if (value.loading) return
    viewModel.viewModelScope.launch {
        // ...
    }
}
```

Kotlin resolves the `ViewModel` context automatically at any call site that is itself inside a
`ViewModel`, which is why `_uiState.updateStateWith { ... }` never mentions `viewModelScope`.

Context parameters have been stable, default compiler behavior since Kotlin 2.4 — this template is on
2.4.10, and no `-Xcontext-parameters` flag is needed. An earlier version of the build set that flag
explicitly; `build-logic/convention/src/main/kotlin/dev/atick/KotlinAndroid.kt` documents why it was
dropped once the compiler started flagging it as redundant.

## `OneTimeEvent<T>`

`getContentIfNotHandled()` returns its content on the first call and `null` on every call after —
thread-safe via `AtomicBoolean.compareAndSet`, so it can't double-fire even if two collectors observe
the same emission. `UiState.error` is one use; a screen event like "navigate back after save" is
another:

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemViewModel.kt
data class ItemScreenData(
    // ...
    val navigateBack: OneTimeEvent<Boolean> = OneTimeEvent(false),
)
```

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemScreen.kt
LaunchedEffect(screenData.navigateBack, onBackClick) {
    if (screenData.navigateBack.getContentIfNotHandled() == true) {
        onBackClick()
    }
}
```

Without `OneTimeEvent`, a plain `Boolean` flag would re-fire the navigation on every recomposition
that re-reads the same state — a config change would pop the screen a second time.

## Anti-patterns to avoid

**Manual loading management** — `updateStateWith`/`updateWith` already handle the try/set-loading/
catch/reset-loading cycle; writing it by hand is what those functions exist to prevent:

```kotlin
// ❌ DON'T: reimplement the loading/error lifecycle by hand
viewModelScope.launch {
    _uiState.update { it.copy(loading = true) }
    try {
        val posts = repository.getPosts().getOrThrow()
        _uiState.update { it.copy(data = it.data.copy(posts = posts), loading = false) }
    } catch (e: Exception) {
        _uiState.update { it.copy(loading = false, error = OneTimeEvent(e)) }
    }
}

// ✅ DO: let updateStateWith handle it
_uiState.updateStateWith {
    repository.getPosts().map { posts -> copy(posts = posts) }
}
```

**Nullable fields in screen data** — a nullable property forces every consumer to null-check; a
default value keeps `data` always renderable:

```kotlin
// ❌ DON'T
data class BadScreenData(val user: User? = null)

// ✅ DO
data class GoodScreenData(val user: User = User.EMPTY)
```

## Testing

`StatefulComposableTest` (`core/ui/src/test/kotlin/dev/atick/core/ui/utils/`) exercises `updateState`,
`updateStateWith`, and `updateWith` directly — including the loading re-entrancy guard and the
error-reset behavior described above — without needing a real `ViewModel` subclass.

## Further reading

- [Architecture](architecture.md) — where `UiState` and `StatefulComposable` fit in the UI layer
- [Navigation](navigation.md) — how `OneTimeEvent` carries navigation events like `ItemViewModel`'s
  `navigateBack`
- [Guide](guide.md) — building a new feature's ViewModel and screen data from scratch
- [Core UI Module](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/core/ui/README.md) — the rest of `core/ui`'s components and utilities
