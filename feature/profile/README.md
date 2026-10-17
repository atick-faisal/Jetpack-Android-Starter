# Module :feature:profile

**Purpose:** Read-only profile display and sign-out, backed by `:data`'s local-only
`ProfileRepositoryImpl` (DataStore + Firebase Auth, no Room).

## Key APIs

| API | What it does |
|---|---|
| `ProfileScreen` / `ProfileViewModel` | Displays profile info; `signOut()` signs out of Firebase Auth, then resets preferences if that succeeds |

Destination signature excerpt; imports and function body are omitted.

```kotlin
// feature/profile/src/main/kotlin/dev/atick/feature/profile/ui/ProfileScreen.kt
@Composable
internal fun ProfileScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    profileViewModel: ProfileViewModel = hiltViewModel(),
)
```

Sign-out returns `Result<Unit>` through `ProfileRepository`; it does not erase Room or remote
records. Profile is read-only here: there is no avatar upload or profile-editing UI.

## Related Documentation

- [Guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/guide.md) — the canonical feature walkthrough to follow when adding a new screen
- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Navigation](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/navigation.md) — how this module's entry is registered with `Navigator`
- [Credential Manager guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/firebase.md#credential-manager-sign-in-vs-register) — the auth source used by `ProfileRepository`
