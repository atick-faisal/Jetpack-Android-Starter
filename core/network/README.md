# Module :core:network

**Purpose:** Retrofit/OkHttp REST client plus connectivity monitoring. An independent option for a
plain backend — `HomeRepositoryImpl` doesn't use it; it syncs through Firestore only.

## Key APIs

| API | What it does |
|---|---|
| `JetpackRestApi` | `getPosts(): List<NetworkPost>`, `getPost(id): NetworkPost` — a demo REST endpoint (`jsonplaceholder`-style `/photos`), unrelated to the `Jetpack` domain model |
| `NetworkDataSource` / `NetworkDataSourceImpl` | Wraps `JetpackRestApi` on `@IoDispatcher`; throws on error, caught by callers via `suspendRunCatching` |
| `NetworkUtils` / `NetworkUtilsImpl` | `getCurrentState(): Flow<NetworkState>` — `CONNECTED`/`LOSING`/`LOST`/`UNAVAILABLE` from a `ConnectivityManager.NetworkCallback` |

```kotlin
// core/network/src/main/kotlin/dev/atick/core/network/api/JetpackRestApi.kt
interface JetpackRestApi {
    @GET("/photos")
    suspend fun getPosts(): List<NetworkPost>

    @GET("/photos/{id}")
    suspend fun getPost(@Path("id") id: Int): NetworkPost
}
```

## Gotchas

- `getCurrentState()` never emits an initial value — a fresh collector sees nothing until the first
  connectivity change, so an `isOffline` banner derived from it won't show on launch even if the
  device is actually offline.
- Reach for this module only if a feature talks to a plain REST backend instead of Firebase; don't
  mix it with Firestore sync for the same feature.
- The base URL is a build secret, not a constant: the Gradle Secrets plugin reads `BACKEND_URL` from
  `secrets.defaults.properties` (version-controlled default) or `local.properties` (real value,
  gitignored) into `BuildConfig.BACKEND_URL`, consumed by `RetrofitModule`.

## Related Documentation

- [Data Layer](../../docs/data.md) — the four data sources and where each is actually used
