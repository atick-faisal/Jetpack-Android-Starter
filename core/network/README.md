# Module :core:network

**Purpose:** Retrofit/OkHttp REST client plus connectivity monitoring. An independent option for a
plain backend — `HomeRepositoryImpl` doesn't use it; it syncs through Firestore only.

## Key APIs

| API | What it does |
|---|---|
| `JetpackRestApi` | `getPosts(): List<NetworkPost>`, `getPost(id): NetworkPost` — a demo REST endpoint (`jsonplaceholder`-style `/photos`), unrelated to the `Jetpack` domain model |
| `NetworkDataSource` / `NetworkDataSourceImpl` | Wraps `JetpackRestApi` on `@IoDispatcher`; throws on error, caught by callers via `suspendRunCatching` |
| `NetworkUtils` / `NetworkUtilsImpl` | `getCurrentState(): Flow<NetworkState>` — initial `CONNECTED`/`UNAVAILABLE`, then callback `CONNECTED`/`LOST`/`UNAVAILABLE`; `LOSING` is only logged |

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

- Each collection seeds from the active network’s `NET_CAPABILITY_INTERNET`, then registers a
  default-network callback and collapses consecutive duplicates. Availability reports `CONNECTED`;
  capability changes are only logged. This does not check `NET_CAPABILITY_VALIDATED` or guarantee
  reachable Internet access, and the snapshot/registration sequence is not atomic.
- `NetworkState.LOSING` is declared but never emitted; the callback logs it without
  sending a state.
- The app uses this module’s connectivity monitor and shared image-loader configuration even
  though Home uses Firestore. REST integration is an optional extension; see the data guide.
- The base URL is a build secret, not a constant: the Gradle Secrets plugin reads `BACKEND_URL` from
  `secrets.defaults.properties` (version-controlled default) or `local.properties` (real value,
  gitignored) into `BuildConfig.BACKEND_URL`, consumed by `RetrofitModule`.

## Related Documentation

- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/data.md) — the four data sources and where each is actually used
