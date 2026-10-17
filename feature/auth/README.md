# Module :feature:auth

**Purpose:** Email/password and Google sign-in/registration screens, wrapping `firebase:auth` behind
the `UiState`/`StatefulComposable` pattern. ViewModels call `AuthRepository` in `:data`,
which handles authentication and saves the local profile through its data sources.

## Key APIs

| API | What it does |
|---|---|
| `SignInScreen` / `SignInViewModel` | Sign-in form; saved-credential auto sign-in, Google sign-in, email/password |
| `SignUpScreen` / `SignUpViewModel` | Registration form |

Destination signature excerpt; imports and function body are omitted.

```kotlin
// feature/auth/src/main/kotlin/dev/atick/feature/auth/ui/signin/SignInScreen.kt
@Composable
internal fun SignInScreen(
    onSignUpClick: () -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    signInViewModel: SignInViewModel = hiltViewModel(),
)
```

## Related Documentation

- [Guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/guide.md) — the canonical feature walkthrough to follow when adding a new screen
- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Navigation](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/navigation.md) — how this module's entries are registered with `Navigator`
- [Credential Manager guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/firebase.md#credential-manager-sign-in-vs-register) — the Credential Manager source used by the authentication repository
