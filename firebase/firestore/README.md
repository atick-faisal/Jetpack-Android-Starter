# Module :firebase:firestore

This module provides cloud data storage using Firebase Firestore. It handles data synchronization,
real-time updates, and offline persistence.

## Features

- Cloud Data Storage
- Real-time Updates
- Offline Persistence
- Batch Operations
- Security Rules Integration
- Data Serialization

## Dependencies Graph

```mermaid
graph TD
    A[firebase:firestore] --> B[core:android]
    A --> C[firebase.bom]
    C --> D[firebase.firestore]
```

## Usage

```kotlin
dependencies {
    implementation(project(":firebase:firestore"))
}
```

### Data Operations

```kotlin
class FirestoreDataSource @Inject constructor(
    firestore: FirebaseFirestore
) {
    private val collection = firestore
        .collection("your_collection")

    suspend fun getData(userId: String): List<Data> =
        collection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects()

    suspend fun createData(data: Data) =
        collection
            .document(data.id)
            .set(data)
            .await()
}
```

### Security Rules

The module expects proper Firestore security rules to be set up. Here's a basic example:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

> [!NOTE]
> For complete Firestore setup and production-ready security rules examples, see the [Firebase Setup Guide](../../docs/firebase.md#firestore-security-rules).

All operations are performed with proper security context and error handling.

## Setup

> [!NOTE]
> For Firebase Firestore setup instructions, including enabling Firestore in the Firebase Console and configuring security rules, see the [Firebase Setup Guide](../../docs/firebase.md).

## Related Documentation

- **[Firebase Setup Guide](../../docs/firebase.md)** - Complete Firebase Console and Firestore setup
- **[Troubleshooting Guide](../../docs/troubleshooting.md)** - Firebase Firestore issues and solutions
- **[Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)** - Official Firebase security rules documentation