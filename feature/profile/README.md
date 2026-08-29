# Module :feature:profile

**Purpose:** Read-only profile display and sign-out, backed by `:data`'s local-only
`ProfileRepositoryImpl` (DataStore + Firebase Auth, no Room).

## Key APIs

| API | What it does |
|---|---|
| `ProfileScreen` / `ProfileViewModel` | Displays profile info; `signOut()` clears preferences and Firebase Auth session |

```kotlin
// feature/profile/src/main/kotlin/dev/atick/feature/profile/ui/ProfileScreen.kt
@Composable
internal fun ProfileScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    profileViewModel: ProfileViewModel = hiltViewModel(),
)
```

## Related Documentation

- [Guide](../../docs/guide.md) — the canonical feature walkthrough to follow when adding a new screen
- [State Management](../../docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Navigation](../../docs/navigation.md) — how this module's entry is registered with `Navigator`
- [firebase:auth](../../firebase/auth/README.md) — the sign-out flow this feature calls
