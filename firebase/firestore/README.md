# Module :firebase:firestore

**Purpose:** The remote sync target for `:data`'s offline-first `home` feature — read/write access
to a per-user Firestore collection, nothing more.

## Key APIs

| API | What it does |
|---|---|
| `FirebaseDataSource` / `FirebaseDataSourceImpl` | `pullJetpacks(userId, syncedAfterNanos)`, `createOrUpdateJetpack`, `deleteJetpack` against `/dev.atick.jetpack/{userId}/jetpacks/{jetpackId}` |
| `FirebaseJetpack` | 8-field remote model (`id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `serverUpdatedAt`, `deleted`) enforced by the security rules below |
| `serverUpdatedAtNanos()` | Reads `serverUpdatedAt` as nanoseconds since the epoch — the pull cursor `:data` stores |

```kotlin
// firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/model/FirebaseJetpack.kt
@get:ServerTimestamp
var serverUpdatedAt: Timestamp? = null
```

## Gotchas

- This module is only ever read through `:data`'s `HomeRepositoryImpl` — no screen queries Firestore
  directly.
- `serverUpdatedAt` is always written as `null`: Firestore substitutes its own timestamp for a null
  `@ServerTimestamp` property, and that server clock — never `lastUpdated`, which belongs to the
  writing device — is what orders `pullJetpacks`. See [Data Layer](../../docs/data.md).
- It is not a constructor parameter, so `:data` never needs `com.google.firebase.Timestamp` on its
  compile classpath. This module declares the SDK with `implementation`, not `api`, and moving the
  property into the constructor would break that boundary.
- A rule change that rejects a write fails silently as a permission-denied error at sync time, not at
  deploy time — test in the Rules Playground first.

## Related Documentation

- [Firebase Setup § Firestore Security Rules](../../docs/firebase.md#firestore-security-rules) — the full rule set and field validation
- [Data Layer](../../docs/data.md) — the two-way sync algorithm this module participates in
