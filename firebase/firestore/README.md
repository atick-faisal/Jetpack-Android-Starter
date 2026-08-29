# Module :firebase:firestore

**Purpose:** The remote sync target for `:data`'s offline-first `home` feature — read/write access
to a per-user Firestore collection, nothing more.

## Key APIs

| API | What it does |
|---|---|
| `FirebaseDataSource` / `FirebaseDataSourceImpl` | `pullJetpacks(userId, lastSynced)`, `createOrUpdateJetpack`, `deleteJetpack` against `/dev.atick.jetpack/{userId}/jetpacks/{jetpackId}` |
| `FirebaseJetpack` | 7-field remote model (`id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `deleted`) enforced by the security rules below |

```kotlin
// firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/data/FirebaseDataSourceImpl.kt
override suspend fun pullJetpacks(userId: String, lastSynced: Long): List<FirebaseJetpack> {
    return withContext(ioDispatcher) {
        database.document(checkAuthentication(userId))
            .collection(FirebaseDataSource.JETPACK_COLLECTION_NAME)
            .whereGreaterThan("lastUpdated", lastSynced)
            .get().await().toObjects(FirebaseJetpack::class.java)
    }
}
```

## Gotchas

- This module is only ever read through `:data`'s `HomeRepositoryImpl` — no screen queries Firestore
  directly.
- A rule change that rejects a write fails silently as a permission-denied error at sync time, not at
  deploy time — test in the Rules Playground first.

## Related Documentation

- [Firebase Setup § Firestore Security Rules](../../docs/firebase.md#firestore-security-rules) — the full rule set and field validation
- [Data Layer](../../docs/data.md) — the two-way sync algorithm this module participates in
