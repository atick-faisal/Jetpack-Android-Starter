# State Management

Screens expose `StateFlow<UiState<ScreenData>>` from their ViewModels and collect it with
`collectAsStateWithLifecycle()` inside the destination's `Screen` composable. `UiState` keeps data,
loading, and an error event together. The helpers handle common Result-returning operations, with
important boundaries around exceptions, concurrent calls, and event delivery described below.

## Concepts

| API | Purpose |
|---|---|
| `UiState<T>` | Current data, a loading flag, and `OneTimeEvent<Throwable?>` for an error |
| `StatefulComposable` | Renders data, a loading indicator, and an error callback |
| `updateState` | Synchronously replaces data while preserving loading and error |
| `updateStateWith` | Launches an operation returning `Result<T>` and replaces data on success |
| `updateWith` | Launches an operation returning `Result<Unit>` without replacing data |
| `OneTimeEvent<T>` | Allows at-most-once consumption per wrapper instance |

The state helpers and wrapper live in `core/ui/.../utils/StatefulComposable.kt`; the event wrapper
lives in `core/android/.../utils/OneTimeEvent.kt`.

## The `UiState<T>` wrapper

```kotlin
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null),
)
```

Data remains present during loading and after a returned failure. Start with values the screen can
render: `HomeScreenData` uses an empty list, while `ItemScreenData` uses empty name/price strings and
a generated ID for a new item. A non-null data container does not prohibit nullable fields. Use null
when absence is meaningful, and make the UI handle that case.

The wrapper's properties are read-only, but an event's consumed flag is mutable. Copying a `UiState`
without replacing `error` retains the same event and its consumption state. Separate flows or custom
state types can be appropriate for independently observed concerns; this wrapper is a shared default.

`StatefulComposable` renders content even while loading and adds a centered loading indicator. It
consumes an error and attempts to call `onShowSnackbar(message, SnackbarAction.REPORT, error)`;
the callback's Boolean return is ignored. It does not implement retry or guarantee that the snackbar
is displayed. See [event delivery](#onetimeeventt) before relying on it for critical acknowledgement.

## Update functions

| Function | Use when | Replaces `data`? | Async? |
|---|---|---|---|
| `updateState` | Form edits and toggles | Yes | No |
| `updateStateWith` | An operation returns new screen data | On returned success | Yes |
| `updateWith` | An action returns no new data | No | Yes |

The async helpers return `Unit`, not the operation's `Result` or a `Job`. Map the result inside the
operation when success must change screen data. They use the context ViewModel's scope; they do not
choose an IO dispatcher. Repository/data-source code owns the relevant dispatcher boundary.

### `updateState`

These are actual Item form methods:

```kotlin
fun updateName(name: String) {
    _itemUiState.updateState { copy(jetpackName = name) }
}

fun updatePrice(priceString: String) {
    _itemUiState.updateState { copy(jetpackPrice = priceString) }
}
```

The lambda returns replacement data. Loading and the existing error event remain unchanged. Because
it uses `MutableStateFlow.update`, the transform can be evaluated again under contention: keep side
effects out of it. Thrown exceptions propagate to the caller.

Keep the price as raw text so clearing it, entering `12e`, or typing `1.` preserves the exact input.
`"1.".toDoubleOrNull()` accepts the trailing decimal point, but converting the result back to text
would normalize it to `"1.0"`. Preserving input and accepting a parsed number are different concerns.
At save, Item trims the text and calls `toDoubleOrNull() ?: 0.0`. Empty or unparseable text becomes
zero; this is fallback behavior, not price validation. No non-negative-price validation is present.

### `updateStateWith`

Starting the coroutine sets loading and replaces the old error with `OneTimeEvent(null)`. A returned
success replaces data and clears loading. A returned failure stores its throwable in a new event,
preserves the current data, and clears loading. The implementation also rejects a successful null
payload, although `T : Any` excludes null in ordinary typed calls.

**Current Item limitation:** `createOrUpdateJetpack()` ignores the repository's Result and returns
success with `navigateBack = OneTimeEvent(true)`. A failed repository save therefore still requests
navigation and exposes no error. Its load `catch` also constructs a state without storing it, leaving
the default form without a load error. These behaviors are covered by characterization tests and
tracked under [Item failure handling](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fix/docs-v3/DOC_FIX_TASKS.md#bounded-runtime-follow-ups).

The following is a **proposed replacement** for the save method inside `ItemViewModel`, not the
current implementation. It uses existing imports and APIs and maps a returned repository failure
through to the helper:

```kotlin
fun createOrUpdateJetpack() {
    _itemUiState.updateStateWith {
        val jetpack = Jetpack(
            id = jetpackId,
            name = jetpackName.trim(),
            price = jetpackPrice.trim().toDoubleOrNull() ?: 0.0,
        )
        homeRepository.createOrUpdateJetpack(jetpack).map {
            copy(navigateBack = OneTimeEvent(true))
        }
    }
}
```

This addresses returned save failures only. It does not fix load errors, thrown exceptions, event
delivery, or edits made during suspension. Repository success concerns local persistence and the
sync-request boundary, not confirmed remote synchronization; see [Data](data.md).

### `updateWith`

Home deletes an item through a Result-returning repository operation:

```kotlin
fun deleteJetpack(jetpack: Jetpack) {
    _homeUiState.updateWith {
        homeRepository.markJetpackAsDeleted(jetpack)
    }
}
```

This helper has the same loading/error setup and returned-failure handling as `updateStateWith`,
but does not replace data on completion. Data changes instead arrive through the repository Flow.

### Exceptions, cancellation, and concurrent calls

Neither async helper catches thrown exceptions or uses `finally` to reset loading. If the operation
throws, including `CancellationException`, execution exits without recording a returned failure and
can leave loading true. Return a `Result` for expected operation failures; keep cancellation
propagating. The repository's `suspendRunCatching` boundary is described in [Data](data.md).
Calling `getOrThrow()` inside a helper is not equivalent to returning the repository Result.

A call that sees `loading = true` is dropped, rather than queued. The check occurs before launching,
but loading is set inside the coroutine. Calls queued before that coroutine runs can both start.
This is not a concurrency lock; overlapping completions can clear loading while another operation
is still running. The [Async state lifecycle follow-up](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fix/docs-v3/DOC_FIX_TASKS.md#bounded-runtime-follow-ups)
tracks exception cleanup and overlap behavior.

The operation receives a snapshot of data read inside the coroutine. If `updateStateWith` suspends
and later returns a copy of that snapshot, it overwrites synchronous edits made in the meantime.
`updateWith` also receives a snapshot, but does not write it back on completion.

For independent operations, use separate operation state when their loading/error lifetimes need
to differ. A single loading flag cannot represent independent progress. As an **extension**, choose
whether to disable edits during save, merge selected result fields into current data, or serialize
operations. Those policies require implementation and tests; the helpers do not supply them.

### Resetting and optimistic updates

An illustrative Item data reset is `_itemUiState.updateState { ItemScreenData() }`. This resets the
form, creates a new ID, and preserves loading/error. Replacing the whole wrapper with
`_itemUiState.value = UiState(ItemScreenData())` also resets loading/error, but does not cancel an
in-flight job; its eventual completion can still write state. Decide how to end or coordinate that
job before resetting an active form. Resetting an edited item to a new UUID changes its identity.

The template persists local writes before background synchronization; that does not confirm remote
success. An **optimistic UI extension** that updates display state before local persistence needs
explicit failure handling and rollback. Restoring an old snapshot can erase newer edits, so define
how concurrent edits are preserved before adding rollback.

## Context parameters

The async helpers declare `context(viewModel: ViewModel)` and launch into that ViewModel's
`viewModelScope`. Inside a ViewModel member, Kotlin can supply the enclosing ViewModel context:

```kotlin
// Declaration excerpt; the body is omitted.
context(viewModel: ViewModel) inline fun <reified T : Any> MutableStateFlow<UiState<T>>.updateStateWith(
    crossinline operation: suspend T.() -> Result<T>,
)
```

The project uses Kotlin 2.4.10 without an explicit `-Xcontext-parameters` flag. Follow the existing
build configuration; no additional flag or dependency is needed to call these helpers.

## `OneTimeEvent<T>`

`getContentIfNotHandled()` atomically marks the wrapper consumed, then returns its content. Later
calls return null. A null payload is indistinguishable from an already-consumed event through this
method. `peekContent()` reads without consuming. Competing consumers share one flag; creating a new
wrapper creates a new flag, even for equal content.

Consumption does not confirm delivery or completion of navigation/snackbar work, retry cancellation,
or persist across process death. The current `StatefulComposable` consumes during composition and
keys its effect on the callback. Existing Compose tests demonstrate two limitations:

- Recomposition after consumption can remove the effect and cancel an unfinished callback.
- A new error under the same callback key can be consumed without launching a new callback.

A discarded composition can also consume before committing; this is a source-reviewed risk rather
than a reproduced test case. The [Event delivery follow-up](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fix/docs-v3/DOC_FIX_TASKS.md#bounded-runtime-follow-ups)
tracks changing consumption and effect lifetime together.

ItemScreen consumes its navigation event inside an effect instead:

```kotlin
LaunchedEffect(screenData.navigateBack, onBackClick) {
    if (screenData.navigateBack.getContentIfNotHandled() == true) {
        onBackClick()
    }
}
```

This avoids consuming during the composition body, but still does not acknowledge completed
navigation. Do not infer that a plain Boolean necessarily repeats on every recomposition: effect
keys and effect lifetime also determine execution. For the current Item save failure behavior, see
[updateStateWith](#updatestatewith).

## Subscription lifetimes

`stateInDelayed(initialValue, viewModelScope)` shares the upstream with
`SharingStarted.WhileSubscribed(5000L)`. It starts when collected, stops the upstream five seconds
after the last subscriber leaves, retains its latest value, and restarts upstream for a later
subscriber. Reading `.value` alone does not start collection.

Home and Item place `onStart { getJetpacks() }` / `onStart { getJetpack() }` before this sharing
operator. Those methods launch repository collection separately in `viewModelScope`. When the shared
upstream stops, those separate collectors remain active. Restarting it launches another collector;
they accumulate until the ViewModel is cleared. Settings uses the same separate-job pattern with a
zero sharing timeout. Screen lifecycle collection alone does not fix that lifetime mismatch.
The [Observation lifetime follow-up](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fix/docs-v3/DOC_FIX_TASKS.md#bounded-runtime-follow-ups) includes Profile
and preserving locale refresh behavior.

## Testing

Place the following complete example alongside Item's existing unit tests in `feature:home`.
The module already has the test dependencies. The fake is supplied explicitly; no Hilt test graph
or database is needed. Turbine starts collection before editing, so the exposed shared state actually
observes the change. This example checks input preservation, not repository persistence.

```kotlin
package dev.atick.feature.home.ui.item

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.atick.core.testing.rule.MainDispatcherRule
import dev.atick.data.model.home.Jetpack
import dev.atick.data.repository.home.HomeRepository
import dev.atick.data.utils.SyncProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ItemInputExampleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps a trailing decimal point`() = runTest {
        val viewModel = ItemViewModel(InputRepository(), existingJetpackId = null)
        viewModel.itemUiState.test {
            assertThat(awaitItem().data.jetpackPrice).isEmpty()
            viewModel.updatePrice("1.")
            assertThat(awaitItem().data.jetpackPrice).isEqualTo("1.")
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class InputRepository : HomeRepository {
    override fun getJetpacks(): Flow<List<Jetpack>> = emptyFlow()
    override fun getJetpack(id: String): Flow<Jetpack> = emptyFlow()
    override suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit> =
        Result.success(Unit)
    override suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit> =
        Result.success(Unit)
    override suspend fun sync(): Flow<SyncProgress> = emptyFlow()
}
```

Run `./gradlew :feature:home:testDebugUnitTest`. `MainDispatcherRule` uses an eager test dispatcher
by default. That is useful here, but does not exercise calls queued before loading becomes true.
Use a controlled dispatcher and suspended operations when testing overlap or edits during save.

The existing suites provide complementary coverage:

| Suite | Coverage |
|---|---|
| `StatefulComposableTest` | Synchronous transforms, returned success/failure, loading, error reset |
| `UiStateContractTest` | Queued overlap, thrown exceptions, cancellation, suspended edits |
| `StatefulComposableEventTest` | Actual Compose effect cancellation and replacement-error behavior |
| `OneTimeEventTest` | Consumption and peeking, including concurrent access |
| `ItemViewModelTest` | Raw input/parsing, current failed save/load behavior, Home/Item collector accumulation |

Tests documenting current defects are characterization tests, not evidence that those behaviors are
desirable. Inspect error content with `peekContent()` when the assertion must not consume it. Clear
ViewModel scopes in lifetime tests so their separately launched collectors are cancelled.

## Further reading

- [Architecture](architecture.md) — UI and data boundaries
- [Data](data.md) — repository Result/Flow contracts and local-first persistence
- [Navigation](navigation.md) — destination arguments and event integration
- [Guide](guide.md) — current feature integration; its walkthrough repair is queued separately
- [Core UI Module](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/core/ui/README.md)
