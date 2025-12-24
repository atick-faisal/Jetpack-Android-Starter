# Data Flow Guide

This guide explains how data flows through the application layers, covering different architectural
patterns for network-only, local-only, and offline-first (network + local) data sources.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Data Flow Patterns](#data-flow-patterns)
3. [Network-Only Pattern](#network-only-pattern)
4. [Local-Only Pattern](#local-only-pattern)
5. [Offline-First Pattern (Network + Local)](#offline-first-pattern-network--local)
6. [Real-Time Data Updates](#real-time-data-updates)
7. [Caching Strategies](#caching-strategies)
8. [Error Handling](#error-handling)

---

## Architecture Overview

### Two-Layer Architecture

This template intentionally uses a simplified **two-layer architecture** (UI + Data) instead of the
traditional three-layer approach. There is **no domain layer** by design to reduce complexity.

```mermaid
graph TB
    subgraph UI["UI Layer (MVVM)"]
        VM[ViewModel<br/>- Manages UI state UiState&ltScreenData&gt<br/>- Calls repositories directly<br/>- Transforms data for UI]
    end

    subgraph Data["Data Layer"]
        Repo[Repository<br/>Interface + Implementation<br/>- Coordinates data sources<br/>- Implements business logic<br/>- Returns Flow&ltT&gt for reactive data<br/>- Returns Result&ltT&gt for one-shot operations]
        Local[LocalDataSource<br/>Room]
        Network[NetworkDataSource<br/>Retrofit]
    end

    UI -->|Result&lt T&gt / Flow&lt T&gt| Data
    Repo --> Local
    Repo --> Network
```

### Unidirectional Data Flow

Data flows in **one direction** through the layers:

```mermaid
graph LR
    A[User Interaction] -->|event| B[UI Layer]
    B -->|action| C[ViewModel]
    C -->|call| D[Repository]
    D -->|query| E[Data Sources]
    E -->|data| D
    D -->|Flow/Result| C
    C -->|StateFlow| B
    B -->|recompose| F[UI Rendered]
```

**Flow Steps:**

1. **User Interaction** → UI Layer
2. **ViewModel** → Calls Repository
3. **Repository** → Coordinates Data Sources (Room/Retrofit/Firebase/DataStore)
4. **Data Sources** → External Systems (Database/Network/Storage)
5. **Data** flows back → Repository → ViewModel → UI

### Key Principles

- **Single Source of Truth**: Local database (Room) is the source of truth for observable data
- **Repositories Expose Flow**: Observable data uses `Flow<T>`, one-shot operations use `Result<T>`
- **ViewModels Call Repositories Directly**: No domain layer, repositories contain business logic
- **Offline-First**: Local data is displayed immediately, network updates happen in background
- **Error Handling**: Use `Result<T>` for error propagation, `suspendRunCatching` for repository
  operations

---

## Data Flow Patterns

The template supports three main data flow patterns. Choose based on your feature requirements:

| Pattern           | Use Case                         | Data Source              | Example                      |
|-------------------|----------------------------------|--------------------------|------------------------------|
| **Network-Only**  | Non-cacheable data, always fresh | Retrofit API             | Weather data, stock prices   |
| **Local-Only**    | User preferences, settings       | Room or DataStore        | Theme preference, auth token |
| **Offline-First** | Core app data, sync required     | Room + Retrofit/Firebase | User posts, profile data     |

---

## Network-Only Pattern

### When to Use

- Data must always be fresh (e.g., live sports scores, stock prices)
- Caching would provide stale or incorrect information
- Data is not critical for offline access

### Architecture

```mermaid
graph LR
    VM[ViewModel] --> Repo[Repository]
    Repo --> Network[NetworkDataSource<br/>Retrofit]
    Network --> API[API]
    API -->|Result&ltT&gt| Network
    Network --> Repo
    Repo --> VM
```

### Data Flow Diagram

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as ViewModel
    participant Repo as Repository
    participant Network as NetworkDataSource
    participant API
    User ->> UI: Tap "Refresh"
    UI ->> VM: loadWeather()
    VM ->> Repo: getCurrentWeather()
    Repo ->> Network: fetch()
    Network ->> API: Retrofit call
    API -->> Network: Weather data
    Network -->> Repo: Result Weather
    Repo -->> VM: Result Weather
    VM ->> VM: Update UiState
    VM -->> UI: StateFlow emits
    UI ->> UI: Recompose with new data
```

### Key Characteristics

- **No local caching** - Data fetched directly from network
- **Always fresh** - Every request goes to the server
- **No offline support** - Requires active network connection
- **Returns Result<T>** - One-shot operations for network calls

> [!NOTE]
> For complete repository implementation examples including interface definitions, data sources, and
> mapper functions, see the [Data Module README](../data/README.md#repository-patterns).

---

## Local-Only Pattern

### When to Use

- User preferences and settings
- Authentication tokens and session data
- Data that doesn't require network sync
- Small, simple key-value data

### Architecture

```mermaid
graph LR
    VM[ViewModel] --> Repo[Repository]
    Repo --> DS[DataStore / Room]
    DS --> Storage[(Local Storage)]
    Storage -->|Flow&ltT&gt| DS
    DS --> Repo
    Repo --> VM
```

### Data Flow Diagram

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as ViewModel
    participant Repo as Repository
    participant DS as DataStore
    Note over UI, DS: App Launch
    UI ->> VM: observeSettings()
    VM ->> Repo: observeSettings()
    Repo ->> DS: Flow subscription
    DS -->> Repo: Flow&ltPreferences&gt
    Repo -->> VM: Flow emits
    VM -->> UI: UiState updates
    UI ->> UI: Render settings
    Note over User, DS: User Changes Theme
    User ->> UI: Select DARK theme
    UI ->> VM: updateTheme(DARK)
    VM ->> Repo: updateTheme(DARK)
    Repo ->> DS: edit { ... }
    DS ->> DS: Persist change
    DS -->> Repo: Flow emits new prefs
    Repo -->> VM: Flow emits
    VM -->> UI: UiState updates
    UI ->> UI: Update automatically
```

### Key Characteristics

- **Fully offline** - No network dependency
- **Reactive with Flow** - UI automatically updates when data changes
- **Immediate persistence** - Changes saved to local storage instantly
- **DataStore for preferences** - Type-safe, reactive preferences storage
- **Room for complex data** - Use Room if data structure is complex

> [!TIP]
> Use DataStore for simple key-value preferences and Room for structured local data with
> relationships.

---

## Offline-First Pattern (Network + Local)

### When to Use

- Core application data (posts, messages, user profiles)
- Data needed offline
- Data that syncs with a server
- Multi-device synchronization required

### Architecture

```mermaid
graph TB
    VM[ViewModel]
    Repo[Repository<br/>Single Source of Truth]
    Local[LocalDataSource<br/>Room Database]
    Network[NetworkDataSource<br/>Retrofit/Firebase]
    DB[(Local Database<br/>SQLite)]
    API[Remote API<br/>Firestore]
    VM -->|observeData| Repo
    Repo -->|Flow&ltT&gt| VM
    Repo -->|observes| Local
    Local -->|Flow&ltEntity&gt| Repo
    Repo -. sync .-> Network
    Network -. fetch .-> API
    Local <-->|read/write| DB
    Network --> Local
```

### Key Concepts

1. **Room is the Single Source of Truth**: UI always observes local database
2. **Network Updates Background**: Fetch from network, update local database
3. **Sync Metadata**: Track sync state (lastUpdated, lastSynced, needsSync)
4. **Soft Deletes**: Mark as deleted locally, sync deletion, then remove

### Data Flow Diagram

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as ViewModel
    participant Repo as Repository
    participant Local as LocalDataSource
    participant Network as NetworkDataSource
    participant DB as Room Database
    participant API
    Note over User, API: App Launch / User Navigates
    UI ->> VM: observePosts()
    VM ->> Repo: observePosts()
    Repo ->> Repo: Trigger sync (background)
    Repo ->> Local: observePosts()
    Local ->> DB: Query
    DB -->> Local: Flow&ltList&ltPostEntity&gt&gt
    Local -->> Repo: Flow emits
    Repo ->> Repo: Map to domain
    Repo -->> VM: Flow&ltList&ltPost&gt&gt
    VM ->> VM: Update UiState
    VM -->> UI: StateFlow emits
    UI ->> UI: Display posts (offline-first!)
    Note over Repo, API: Background Sync
    par Push Local Changes
        Repo ->> Local: Get unsynced posts
        Local -->> Repo: Unsynced items
        Repo ->> Network: UPSERT/DELETE
        Network ->> API: Push changes
        Repo ->> Local: Mark as synced
    and Pull Remote Changes
        Repo ->> Local: Get lastSyncTimestamp
        Local -->> Repo: Timestamp
        Repo ->> Network: Fetch since timestamp
        Network ->> API: GET updated posts
        API -->> Network: Remote posts
        Network -->> Repo: Remote data
        Repo ->> Local: Upsert to database
        Local ->> DB: Save
    end

    DB -->> Local: Flow emits updated
    Local -->> Repo: Updated data
    Repo -->> VM: Flow emits
    VM -->> UI: UI updates (reactive!)
```

### Key Characteristics

- **Room as Single Source of Truth** - UI always observes local database
- **Immediate UI updates** - Local changes reflected instantly
- **Background synchronization** - Network sync happens asynchronously
- **Conflict resolution** - Last-write-wins (server timestamp based)
- **Bidirectional sync** - Push local changes, pull remote changes
- **Incremental sync** - Only fetch data modified since last sync

> [!IMPORTANT]
> The offline-first pattern requires careful sync metadata tracking. Always include `lastUpdated`,
`lastSynced`, `needsSync`, and `syncAction` fields in your Room entities.

For detailed offline-first repository implementation with sync metadata, see
the [Data Module README](../data/README.md#repository-patterns).

---

## Real-Time Data Updates

For real-time updates (e.g., Firebase Firestore snapshots, WebSocket), use Firebase's snapshot
listeners or similar mechanisms.

### Firebase Firestore Real-Time Flow

```mermaid
sequenceDiagram
    participant Firestore as Firebase Firestore<br/>(Cloud)
    participant Listener as Snapshot Listener
    participant Flow as callbackFlow
    participant Repo as Repository
    participant Room as Local Database<br/>(Room)
    participant VM as ViewModel
    participant UI
    Firestore ->> Listener: Data change
    Listener ->> Flow: Emit snapshot
    Flow ->> Repo: Observe changes
    Repo ->> Room: Update local data
    Room ->> Room: Persist
    Room -->> Repo: Flow emits
    Repo -->> VM: Flow&ltData&gt
    VM ->> VM: Update UiState
    VM -->> UI: StateFlow emits
    UI ->> UI: Render updates
```

### Key Pattern: Firestore → Room → UI

Even with real-time updates, maintain **Room as the single source of truth**:

1. **Firestore snapshot listener** emits changes
2. **Repository** updates local Room database
3. **UI observes Room Flow** (not Firestore directly)
4. **Result**: Consistent data access pattern across app

This approach ensures:

- Offline access to last known data
- Consistent data access APIs
- Easy testing (mock Room, not Firestore)
- Works even if Firebase connection fails

> [!NOTE]
> For Firebase Firestore integration examples, see
> the [Firebase Module README](../firebase/firestore/README.md).

---

## Caching Strategies

### 1. Time-Based Cache Invalidation

Fetch fresh data from network if cache is older than a threshold.

**Flow:**

```mermaid
graph TD
    Start[Repository called] --> Check{Check lastSyncTimestamp}
    Check --> Stale{Is cache stale?<br/>current time - lastSync > threshold}
    Stale -->|YES| Sync[Trigger background sync]
    Stale -->|NO| Use[Use cached data]
    Sync --> Emit[Emit local data immediately]
    Use --> Emit
    Emit --> End[offline-first!]
```

**Use When:**

- Data changes infrequently
- Staleness tolerance is acceptable (e.g., 5 minutes)
- Want to reduce network calls

### 2. Manual Refresh (Pull-to-Refresh)

Allow user to manually trigger sync.

**Flow:**

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as ViewModel
    participant Repo as Repository
    participant Network
    participant Room as Local Database
    User ->> UI: Pull to refresh
    UI ->> VM: refreshPosts()
    VM ->> VM: Set loading state
    VM ->> Repo: syncPosts()
    Repo ->> Network: Fetch from network
    Network -->> Repo: Fresh data
    Repo ->> Room: Update local database
    Room ->> Room: Persist
    Room -->> Repo: Flow emits updated
    Repo -->> VM: Updated data
    VM ->> VM: Clear loading state
    VM -->> UI: UiState updates
    UI ->> UI: Dismiss loading indicator
```

**Use When:**

- User wants to ensure fresh data
- Complementary to time-based caching
- Provides user control

### 3. Network-Bound Resource Pattern

Utility for coordinating network + local data with automatic caching.

**Flow:**

```mermaid
graph TD
    Start[Network-Bound Resource] --> Loading1[1. Emit Loading state]
    Loading1 --> Query[2. Query local database]
    Query --> Loading2[3. Emit Loading with local data]
    Loading2 --> ShouldFetch{4. Should fetch<br/>from network?}
    ShouldFetch -->|YES| Fetch[Fetch from network]
    Fetch --> Save[Save to local database]
    Save --> Success[Emit Success with fresh data]
    ShouldFetch -->|NO| CachedSuccess[Emit Success with cached data]
    Fetch -. Network Error .-> Error[Emit Error<br/>with stale local data]
```

**Use When:**

- Want automatic cache-then-network pattern
- Need loading states with cached data
- Want to show stale data on network errors

> [!NOTE]
> For `networkBoundResource` implementation and usage examples, see
> the [Data Module README](../data/README.md).

---

## Error Handling

All data layer operations use a **layered error handling approach** with `Result<T>` for error
propagation:

- **Repository Layer**: Uses `suspendRunCatching` to wrap all operations
- **ViewModel Layer**: Uses `updateStateWith`/`updateWith` for automatic error capture
- **UI Layer**: Uses `StatefulComposable` for automatic error display via snackbar

### Error Flow Diagram

```mermaid
graph TD
    Start[Repository Operation] --> Catch[suspendRunCatching]
    Catch --> Result{Success or Failure}
    Result --> Return[Result&ltT&gt]
    Return --> VM[ViewModel]
    VM --> Update[updateStateWith/<br/>updateWith]
    Update --> Auto[Auto-handle Result]
    Auto --> UiState[UiState<br/>data or error]
    UiState --> Stateful[StatefulComposable]
    Stateful --> Display{Display}
    Display -->|Success| Content[Show content]
    Display -->|Error| Snackbar[Show error snackbar]
```

For comprehensive error handling patterns including network-specific errors, HTTP error codes, and
detailed examples, see:

> [!NOTE]
> Complete error handling documentation is available in
> the [Data Module README](../data/README.md#error-handling).

---

## Summary

This guide covered three main data flow patterns:

- **Network-Only**: For real-time data that doesn't need offline access (weather, stock prices)
- **Local-Only**: For preferences and settings using DataStore (theme, notifications)
- **Offline-First**: For user-generated content with Room as single source of truth (posts,
  profiles)

**Key Takeaways:**

1. **Choose the Right Pattern** based on feature requirements
2. **Room is the Single Source of Truth** for offline-first - UI observes local database, network
   updates happen in background
3. **Use Proper Threading** - Inject `@IoDispatcher` and use `withContext(ioDispatcher)` for
   blocking calls
4. **Error Handling is Centralized** - Repository uses `suspendRunCatching`, ViewModel uses
   `updateStateWith`/`updateWith`, UI uses `StatefulComposable`
5. **Flow for Reactive Data** - Observe local database with Flow, UI updates automatically
6. **Result for Operations** - One-shot operations return Result<T> for error handling

All patterns use **Repositories** as the interface to ViewModels, **Data Sources** for external
system interaction, **Result** type for error handling, and **Flow** for reactive data streams.

## Further Reading

- [Data Module README](../data/README.md) - Repository patterns, implementations, and error handling
  reference
- [State Management](state-management.md) - Learn about ViewModel state patterns
- [Architecture Overview](architecture.md) - Understand the two-layer architecture
- [Adding Features](guide.md) - Step-by-step implementation guide
- [Quick Reference](quick-reference.md) - Common data flow patterns cheat sheet
