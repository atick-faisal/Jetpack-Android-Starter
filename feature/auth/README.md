# Module :feature:auth

**Purpose:** Email/password and Google sign-in/registration screens, wrapping `firebase:auth` behind
the `UiState`/`StatefulComposable` pattern.

## Key APIs

| API | What it does |
|---|---|
| `SignInScreen` / `SignInViewModel` | Sign-in form; saved-credential auto sign-in, Google sign-in, email/password |
| `SignUpScreen` / `SignUpViewModel` | Registration form |

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

- [Guide](../../docs/guide.md) — the canonical feature walkthrough to follow when adding a new screen
- [State Management](../../docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Navigation](../../docs/navigation.md) — how this module's entries are registered with `Navigator`
- [firebase:auth](../../firebase/auth/README.md) — the Credential Manager wrapper this module calls
