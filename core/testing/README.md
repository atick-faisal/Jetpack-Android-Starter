# Module :core:testing

Shared JVM test helpers for repositories and ViewModels: `MainDispatcherRule`,
`FakeUserPreferencesDataSource`, and `FakeAuthDataSource`.

## Add the helpers

Add this to the consuming module’s `build.gradle.kts`:

```kotlin
dependencies {
    testImplementation(projects.core.testing)
}
```

The Android convention supplies the generic test stack; these shared helpers are opt-in.
This module re-exports JUnit4, Kotlin test, coroutine test, Turbine and Truth to its test consumers.

| API | Behavior |
|---|---|
| `MainDispatcherRule` | Replaces `Dispatchers.Main` for a test and resets it afterwards; defaults to `UnconfinedTestDispatcher` |
| `FakeUserPreferencesDataSource` | Stores preferences in `MutableStateFlow`; interface writes produce observable updates; `current` exposes the latest value |
| `FakeAuthDataSource` | Stores a nullable current user; sign-out clears it unless `signOutError` is set; tracks calls in `signOutCount` |

The auth fake’s sign-in/registration operations throw `UnsupportedOperationException`, including
email/password operations. This is a limitation of this fake, not a restriction on all JVM tests.
Use a suitable test double when testing other auth paths.

## Test repository success and failure

This complete example belongs under `data/src/test/kotlin/dev/atick/data/repository/settings/`.
It uses the same implementation and assertions as `SettingsRepositoryImplTest`. No Hilt test graph,
Firebase connection or dispatcher replacement is needed for these two suspend repository calls.

```kotlin
package dev.atick.data.repository.settings

import com.google.common.truth.Truth.assertThat
import dev.atick.core.preferences.model.DarkThemeConfigPreferences
import dev.atick.core.testing.data.FakeAuthDataSource
import dev.atick.core.testing.data.FakeUserPreferencesDataSource
import dev.atick.data.model.settings.DarkThemeConfig
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SettingsRepositoryExampleTest {
    private val preferences = FakeUserPreferencesDataSource()
    private val auth = FakeAuthDataSource()
    private val repository = SettingsRepositoryImpl(auth, preferences)

    @Test
    fun `theme update writes preferences`() = runTest {
        val result = repository.setDarkThemeConfig(DarkThemeConfig.LIGHT)

        assertThat(result.isSuccess).isTrue()
        assertThat(preferences.current.darkThemeConfigPreferences)
            .isEqualTo(DarkThemeConfigPreferences.LIGHT)
    }

    @Test
    fun `sign out returns the source failure`() = runTest {
        val failure = IllegalStateException("network down")
        auth.signOutError = failure

        val result = repository.signOut()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
    }
}
```

Run `./gradlew :data:testDebugUnitTest --tests 'dev.atick.data.repository.settings.SettingsRepositoryImplTest'`
for the existing reference suite. If you add the example class, select `SettingsRepositoryExampleTest`
instead. The existing suite also collects settings with Turbine and checks preference reset ordering.

For ViewModels using `viewModelScope`, install the rule:

```kotlin
@get:org.junit.Rule
val mainDispatcherRule = dev.atick.core.testing.rule.MainDispatcherRule()
```

Collect shared state before asserting emitted updates; reading `StateFlow.value` alone does not
start a `WhileSubscribed` upstream. The default eager dispatcher does not exercise queued overlap;
use a controlled dispatcher and suspended operations for concurrency tests.

## Further guidance

- [State Management — Testing](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md#testing) — ViewModel example and state/event contract suites
- [Guide — Testing](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/guide.md#testing) — constructing repository and ViewModel tests
- [Architecture — Testing](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/architecture.md#testing) — convention test setup
- [Settings repository tests](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/data/src/test/kotlin/dev/atick/data/repository/settings/SettingsRepositoryImplTest.kt) — executable success, failure, Flow and reset-order examples
