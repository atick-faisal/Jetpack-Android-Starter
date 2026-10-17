# Module :core:preferences

**Purpose:** Type-safe local storage for the user's profile and app settings, backed by a single
JSON DataStore file, exposed as `Flow` for reads and suspend functions for writes.

## Key APIs

| API | What it does |
|---|---|
| `UserPreferencesDataSource` / `Impl` | `getUserDataPreferences(): Flow<UserDataPreferences>`, `getUserIdOrThrow()`, `setUserProfile()`, `setDarkThemeConfig()`, `setDynamicColorPreference()`, `resetUserPreferences()` |
| `UserDataPreferences` | `id`, `userName`, `profilePictureUriString`, `darkThemeConfigPreferences`, `useDynamicColor` — the one serialized preferences object |
| `UserDataSerializer` | `Serializer<UserDataPreferences>` passed to `DataStoreFactory.create` |

```kotlin
// core/preferences/src/main/kotlin/dev/atick/core/preferences/model/UserDataPreferences.kt
@Serializable
data class UserDataPreferences(
    val id: String = String(),
    val userName: String? = null,
    val profilePictureUriString: String? = null,
    val darkThemeConfigPreferences: DarkThemeConfigPreferences = DarkThemeConfigPreferences.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = true,
)
```

## Gotchas

- One DataStore, one data class — there's no per-key preference API here, every write goes through
  `updateData { userData -> userData.copy(...) }` on the whole object.
- `UserDataSerializer.readFrom` converts `SerializationException` to `CorruptionException`. No
  corruption handler is installed, so malformed JSON propagates as an error rather than resetting
  to defaults. Other exceptions are not converted by this catch.
- Missing fields with defaults are supported; unknown keys and invalid enum values are not ignored.
  Review serializer compatibility when evolving the model.
- `resetUserPreferences()` replaces the entire object with defaults, including theme/color settings.
  Sign-out resets it after auth sign-out succeeds; this does not clear Room or Firestore.

## Related Documentation

- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/data.md) — where this data source fits in the repository pattern
- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/state-management.md) — how `SettingsViewModel` surfaces these preferences
