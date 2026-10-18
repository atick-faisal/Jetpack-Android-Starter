# Module :firebase:firestore

**Purpose:** The remote sync target for `:data`'s offline-first `home` feature — read/write access
to a per-user Firestore collection.

## Key APIs

| API | What it does |
|---|---|
| `FirebaseDataSource` / `FirebaseDataSourceImpl` | `pullJetpacks(userId, syncedAfterNanos)`, `createOrUpdateJetpack`, `deleteJetpack` against `/dev.atick.jetpack/{userId}/jetpacks/{jetpackId}` |
| `FirebaseJetpack` | 8-field remote model (`id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `serverUpdatedAt`, `deleted`) validated by the example rules linked below when deployed |
| `serverUpdatedAtNanos()` | Reads `serverUpdatedAt` as nanoseconds since the epoch — the pull cursor `:data` stores |

```kotlin
// firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/model/FirebaseJetpack.kt
@get:ServerTimestamp
var serverUpdatedAt: Timestamp? = null
```

## Gotchas

- Current Home screens observe Room through `HomeRepository`; that repository calls this source
  during synchronization. No current screen queries Firestore directly.
- The repository upload mapping leaves `serverUpdatedAt` as `null`: Firestore substitutes its timestamp for a null
  `@ServerTimestamp` property, and that server clock — never `lastUpdated`, which belongs to the
  writing device — is what orders `pullJetpacks`. See [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md).
- It is not a constructor parameter, so `:data` never needs `com.google.firebase.Timestamp` on its
  compile classpath. This module declares the SDK with `implementation`, not `api`, and moving the
  property into the constructor would break that boundary.
- Rule rejection fails the remote operation during sync; the worker logs errors and may retry.
  A successful local repository write does not establish remote acceptance.
- Pull uses default-source `get()` and a strict server-timestamp filter. Cached results do not
  establish server completeness; missing timestamp fields are excluded, and equal-timestamp
  interruption can skip records. Remote hard deletes supply no deletion feed. See
  [delivery limitations](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#delivery-and-conflict-limitations) and
  [upgrade requirements](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/firebase.md#existing-installs-and-documents).

## Related Documentation

- [Firebase Setup § Firestore Security Rules](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/firebase.md#firestore-security-rules) — the full rule set and field validation
- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md) — the two-way sync algorithm this module participates in
