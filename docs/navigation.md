# Navigation

This template uses [Jetpack Navigation 3](https://developer.android.com/guide/navigation/navigation-3),
not `navigation-compose`. There is no navigation graph resource, no route strings, and no back-stack
argument bundle — destinations are typed keys, and a small `core/navigation` module owns the state and the
one class allowed to change it.

## Concepts

| Type | What it does | Where |
|---|---|---|
| `NavKey` | Marker for a destination. `@Serializable`, so it survives process death. | one per destination, next to its feature's entries (e.g. `HomeNavKey`, `ItemNavKey`) |
| `NavigationState` | Holds one back stack per top-level destination, plus the order they were visited in. | `core/navigation/src/main/kotlin/dev/atick/core/navigation/NavigationState.kt` |
| `Navigator` | The only thing that mutates `NavigationState`. | `core/navigation/src/main/kotlin/dev/atick/core/navigation/Navigator.kt` |
| `NavDisplay` | Renders the flattened back stack. | `app/src/main/kotlin/dev/atick/compose/ui/JetpackApp.kt` |
| `entry<T>` / `entryProvider` | Maps a `NavKey` type to a composable. | per-feature `navigation/*Navigation.kt` files |
| `ListDetailSceneStrategy` | Adaptive list/detail layout for wide windows. | `JetpackApp.kt`, `feature/home/.../navigation/HomeNavigation.kt` |

## Defining a destination

A destination with no argument is a `data object`; one with an argument is a `data class`:

```kotlin
// feature/auth/src/main/kotlin/dev/atick/feature/auth/navigation/AuthNavigation.kt
@Serializable
data object SignInNavKey : NavKey

// feature/home/src/main/kotlin/dev/atick/feature/home/navigation/HomeNavigation.kt
@Serializable
data class ItemNavKey(val itemId: String?) : NavKey
```

## Registering entries

Each feature module exposes an `EntryProviderScope<NavKey>` extension that registers its destinations.
Metadata (like `ListDetailSceneStrategy.listPane()`) attaches per entry, not per graph:

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/navigation/HomeNavigation.kt
fun EntryProviderScope<NavKey>.homeEntries(
    navigator: Navigator,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
) {
    entry<HomeNavKey>(
        metadata = ListDetailSceneStrategy.listPane(),
    ) {
        HomeScreen(
            onJetpackClick = navigator::navigateToItem,
            onShowSnackbar = onShowSnackbar,
        )
    }

    entry<ItemNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(),
    ) { key ->
        ItemScreen(
            itemId = key.itemId,
            onBackClick = navigator::goBack,
            onShowSnackbar = onShowSnackbar,
        )
    }
}
```

The app module combines each feature's entries under one `entryProvider { }` per graph (see
[Wiring `NavDisplay`](#wiring-navdisplay)). Each feature also declares `fun Navigator.navigateToX()`
extensions next to its keys, so navigating never requires importing another feature's key types:

```kotlin
fun Navigator.navigateToItem(itemId: String?) = navigate(ItemNavKey(itemId))
```

## Multi-back-stack state

`rememberNavigationState` builds one back stack per top-level destination (`subStacks`) plus a separate
stack recording the order those destinations were visited (`topLevelStack`):

```kotlin
// core/navigation/src/main/kotlin/dev/atick/core/navigation/NavigationState.kt
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val topLevelStack = rememberNavBackStack(startKey)

    val subStacks = topLevelKeys.associateWith { topLevelKey ->
        key(topLevelKey) { rememberNavBackStack(topLevelKey) }
    }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}
```

> [!TIP]
> `key(topLevelKey)` is what gives each sub stack its own saved-state slot. Without it, every iteration
> of the `associateWith` lambda shares the composition's compound key hash, so all the stacks register
> under one key and are restored by position rather than by which destination they belong to — a tab can
> silently come back with another tab's history after process death. `NavigationStateTest` (see
> [Testing](#testing)) is the regression test for this.

`NavigationState` derives the current tab and current screen from those two stacks, and never mutates
itself — only `Navigator` does:

```kotlin
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }
    val topLevelKeys: Set<NavKey> get() = subStacks.keys
    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey]
            ?: error("Sub stack for $currentTopLevelKey does not exist")
    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}
```

`NavDisplay` needs one flat list, not a map of stacks — the `backStack` extension property flattens them
in visit order, which is also what makes back-from-another-tab return to the start tab rather than exiting
the app:

```kotlin
val NavigationState.backStack: List<NavKey>
    get() = topLevelStack.flatMap { subStacks[it].orEmpty() }
```

## The `Navigator`

`navigate(key)` picks its behavior from what kind of destination `key` is:

| `key` is... | Effect |
|---|---|
| the current top-level destination | pops its stack back to the root — "tap the selected tab again to go home" |
| another top-level destination | switches to it, preserving where the user was |
| anything else | pushes onto the current top-level destination's stack |

```kotlin
// core/navigation/src/main/kotlin/dev/atick/core/navigation/Navigator.kt
fun navigate(key: NavKey) {
    when (key) {
        state.currentTopLevelKey -> clearSubStack()
        in state.topLevelKeys -> goToTopLevel(key)
        else -> goToKey(key)
    }
}

fun goBack() {
    when (state.currentKey) {
        state.startKey -> Unit
        state.currentTopLevelKey -> state.topLevelStack.removeLastOrNull()
        else -> state.currentSubStack.removeLastOrNull()
    }
}

fun canGoBack(): Boolean = state.currentKey != state.startKey
```

Calling `goBack()` from the start key does nothing — there is nowhere left to go, and only the system back
handler finishes the activity. A screen can wire a back button straight to `navigator::goBack`; use
`canGoBack()` to decide whether to *handle* back at all (see [Wiring `NavDisplay`](#wiring-navdisplay)).

> [!WARNING]
> A non-top-level key must belong to exactly one top-level destination. Navigation 3 identifies an entry
> by `NavEntry.contentKey`, which defaults to the key itself, so pushing the same key from two different
> tabs puts it in `backStack` twice — and the two copies share one `rememberSaveable` bundle and one
> `ViewModelStore`. Give each tab its own key type rather than reusing one across tabs.

## Wiring `NavDisplay`

`JetpackApp` picks between two independent graphs based on `appState.isUserLoggedIn`. Each graph builds
its own `NavigationState`, so signing in or out discards the other graph's back stack rather than leaving
history the user could return to:

| Graph | Composable | `startKey` | `topLevelKeys` |
|---|---|---|---|
| Signed-out | `SignedOutNavigation` | `SignInNavKey` | `setOf(SignInNavKey)` |
| Signed-in | `SignedInNavigation` | `TopLevelDestination.START.key` | `TopLevelDestination.keys` |

Both follow the same setup:

```kotlin
val navigationState = rememberNavigationState(
    startKey = TopLevelDestination.START.key,
    topLevelKeys = TopLevelDestination.keys,
)
val navigator = remember(navigationState) { Navigator(navigationState) }
```

`NavDisplay` renders the flattened `backStack`, and each entry gets its saved state and `ViewModelStore`
from `rememberDefaultEntryDecorators()`:

```kotlin
// app/src/main/kotlin/dev/atick/compose/ui/JetpackApp.kt
NavDisplay(
    backStack = navigationState.backStack,
    entryDecorators = rememberDefaultEntryDecorators(),
    sceneStrategies = listOf(rememberListDetailSceneStrategy<NavKey>()),
    onBack = navigator::goBack,
    entryProvider = entryProvider {
        homeEntries(navigator, onShowSnackbar)
        profileEntries(onShowSnackbar)
    },
)
```

`entryDecorators` comes from `core/navigation`:

```kotlin
@Composable
fun rememberDefaultEntryDecorators(): List<NavEntryDecorator<NavKey>> = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),
    rememberViewModelStoreNavEntryDecorator(),
)
```

`NavDisplay` installs its own back handler, but only while the current scene has entries behind it. The
two-pane list-detail scene (see below) has the list and detail as one scene with nothing behind it, so the
app also wires an explicit handler next to every `NavDisplay`:

```kotlin
BackHandler(enabled = navigator.canGoBack()) { navigator.goBack() }
```

## Adaptive list-detail

The signed-in `NavDisplay` installs `rememberListDetailSceneStrategy<NavKey>()`. An entry opts into it with
pane metadata — `HomeNavKey` is the list pane, `ItemNavKey` is the detail pane (both registered in
`homeEntries`, shown under [Registering entries](#registering-entries)). On a wide window the two render
side by side; on a phone they behave as separate screens, and which layout applies is decided entirely by
the scene strategy, not by the entries themselves.

One consequence: on a wide window the list pane is still on screen beside the detail, but the last key in
the back stack is the detail key. `JetpackApp.kt` tracks this explicitly rather than guessing from window
size, since guessing wrong stacks two top app bars on what should be a single full-screen destination:

```kotlin
val isAtTopLevel = navigationState.currentKey == navigationState.currentTopLevelKey
```

## Passing arguments to ViewModels

Navigation 3 carries destination arguments on the `NavKey` itself, not in a `SavedStateHandle`. The
argument reaches the ViewModel through Hilt's assisted-injection support, using `ItemNavKey.itemId` as the
example end to end:

1. The key carries the argument:
   ```kotlin
   data class ItemNavKey(val itemId: String?) : NavKey
   ```
2. The entry reads it off the key and passes it to the composable:
   ```kotlin
   entry<ItemNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
       ItemScreen(itemId = key.itemId, onBackClick = navigator::goBack, onShowSnackbar = onShowSnackbar)
   }
   ```
3. The composable requests the ViewModel with a `creationCallback`:
   ```kotlin
   // feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemScreen.kt
   itemViewModel: ItemViewModel = hiltViewModel<ItemViewModel, ItemViewModel.Factory>(
       creationCallback = { factory -> factory.create(itemId) },
   ),
   ```
4. The ViewModel takes the argument through an `@AssistedInject` constructor:
   ```kotlin
   // feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemViewModel.kt
   @HiltViewModel(assistedFactory = ItemViewModel.Factory::class)
   class ItemViewModel @AssistedInject constructor(
       private val homeRepository: HomeRepository,
       @Assisted private val existingJetpackId: String?,
   ) : ViewModel() {

       @AssistedFactory
       interface Factory {
           fun create(existingJetpackId: String?): ItemViewModel
       }
       // ...
   }
   ```

Every destination that needs a keyed argument in its ViewModel follows this same four-step chain.

## Testing

`core/navigation`'s own test suite covers this module directly — `NavigatorTest` builds a
`NavigationState` by hand and exercises `navigate()`/`goBack()`'s branches, while `NavigationStateTest`
drives an actual save/restore cycle with `StateRestorationTester` to guard the `key()` behavior described
under [Multi-back-stack state](#multi-back-stack-state). See `core/navigation/src/test/kotlin/dev/atick/core/navigation/`
for both.

## Further reading

- [Architecture](architecture.md) — where `core/navigation` sits in the module graph
- [State Management](state-management.md) — how `StatefulComposable` and `OneTimeEvent` interact with
  navigation events like `ItemViewModel`'s `navigateBack`
- [Guide](guide.md) — adding a new feature, including its destinations
