# Module :core:preferences

**Purpose:** Type-safe local storage for the user's profile and app settings, backed by a single
Proto-style DataStore file, exposed as `Flow` for reads and suspend functions for writes.

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
- `UserDataSerializer.readFrom` only catches `SerializationException`. A custom serializer added
  here must throw that, not something like `IllegalArgumentException` from `enumValueOf()`, or a
  corrupt file will escape the `catch` and propagate instead of falling back to `defaultValue`.

## Related Documentation

- [Data Layer](../../docs/data.md) — where this data source fits in the repository pattern
- [State Management](../../docs/state-management.md) — how `SettingsViewModel` surfaces these preferences
