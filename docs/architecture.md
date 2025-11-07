# Architecture Overview

This project follows the
official [Android Architecture Guidelines](https://developer.android.com/topic/architecture) with
some pragmatic adaptations to keep the codebase simple and maintainable.

## Architectural Principles

The architecture is built on several key principles:

1. **Separation of Concerns**: Each component has its own responsibility
2. **Single Source of Truth**: Data is managed in a single place
3. **Unidirectional Data Flow**: Data flows in one direction, events flow in the opposite
4. **State-Based UI**: UI is a reflection of the state
5. **Pragmatic Simplicity**: Complex patterns are only added when necessary

## Core Layers

The app uses a two-layer architecture:

```mermaid
graph TD
    A[UI Layer] --> B[Data Layer]
    style A fill: #4CAF50, stroke: #333, stroke-width: 2px
    style B fill: #1976D2, stroke: #333, stroke-width: 2px
```

### UI Layer

The UI layer follows MVVM pattern and consists of:

1. **Composables**: Pure UI components built with Jetpack Compose
2. **ViewModels**: Manage UI state and business logic
3. **Screen Data**: Immutable data classes representing screen state

Example UI Layer structure:

```kotlin
data class HomeScreenData(
    val items: List<Item> = emptyList(),
    // other UI state properties
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(HomeScreenData()))
    val uiState = _uiState.asStateFlow()
}

@Composable
fun HomeScreen(
    screenData: HomeScreenData,
    onAction: (HomeAction) -> Unit
) {
}
```

### Data Layer

The data layer handles data operations and consists of:

1. **Repositories**: Single source of truth for data
2. **Data Sources**: Interface with external systems (API, database, etc.)
3. **Models**: Data representation classes

Example Data Layer structure:

```kotlin
class HomeRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: NetworkDataSource
) : HomeRepository {
    override fun getData(): Flow<List<Data>> =
        networkBoundResource(
            query = { localDataSource.getData() },
            fetch = { networkDataSource.getData() },
            saveFetchResult = { localDataSource.saveData(it) }
        )
}
```

> [!NOTE]
> Unlike the official guidelines, this project intentionally omits the domain layer to reduce
> complexity. You can add a domain layer if your app requires complex business logic or needs to
> share
> logic between multiple ViewModels.

## State Management

The project uses a consistent state management pattern:

1. **UiState Wrapper**:

    ```kotlin
    data class UiState<T : Any>(
        val data: T,
        val loading: Boolean = false,
        val error: OneTimeEvent<Throwable?> = OneTimeEvent(null)
    )
    ```

2. **State Updates**:

    ```kotlin
    // Regular state updates
    _uiState.updateState { copy(value = newValue) }

    // Async operations
    _uiState.updateStateWith {
        repository.someAsyncOperation()
    }
    ```

> [!TIP]
> **Kotlin Context Parameters**: The `updateStateWith` and `updateWith` functions use Kotlin's
> context parameters feature (enabled via `-Xcontext-parameters` compiler flag) to automatically
> access the ViewModel's scope. You don't need to pass `viewModelScope` explicitly - it's
> injected via the `context(viewModel: ViewModel)` parameter.

3. **State Display**:

    ```kotlin
	@Composable
    fun StatefulScreen(
        state: UiState<ScreenData>,
        onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
    ) {
        StatefulComposable(
            state = state,
            onShowSnackbar = onShowSnackbar
        ) { screenData ->
            // UI Content
        }
    }
    ```

## Dependency Injection

The project uses Hilt for dependency injection:

- **Modules**: Organized by feature and core functionality
- **Scoping**: Primarily uses singleton scope for repositories and data sources
- **Testing**: Enables easy dependency replacement for testing

## Data Flow

1. **User Interaction** → UI Events
2. **ViewModel** → Business Logic
3. **Repository** → Data Operations
4. **DataSource** → External Systems
5. **Back to UI** through StateFlow

```mermaid
graph LR
    A[User Action] --> B[ViewModel]
    B --> C[Repository]
    C --> D[Data Sources]
    D --> C
    C --> B
    B --> E[UI State]
    E --> F[UI]
```

## Adding a Domain Layer

If your app grows in complexity, you can add a domain layer:

```kotlin
class GetDataUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(params: Params): Result<Data> =
        repository.getData(params)
}
```

> [!TIP]
> Consider adding a domain layer when:
> - Multiple ViewModels share business logic
> - Business rules become complex
> - You need to transform data between layers

## Testing Strategy

> [!NOTE]
> Testing infrastructure is planned but not yet implemented in this template.

The architecture enables different types of tests:

1. **UI Tests**: Test Composables in isolation
2. **ViewModel Tests**: Test state management and business logic
3. **Repository Tests**: Test data operations
4. **Integration Tests**: Test multiple layers together

## Best Practices

1. **Keep Screen Data Simple**: Only include what's needed for the UI
2. **Single Responsibility**: Each class should have one clear purpose
3. **Error Handling**: Use `Result` type for operations that can fail
4. **Coroutines**: Use structured concurrency with proper scoping
5. **Immutable Data**: Use data classes for state and models

---

## Summary

This template uses a **two-layer architecture** (UI + Data) for simplicity:

- **UI Layer**: Composables + ViewModels with UiState wrapper
- **Data Layer**: Repositories + Data Sources (Network, Local, Firebase)
- **State Management**: Centralized with `updateState` and `updateStateWith` functions
- **Dependency Injection**: Hilt with feature-based modules
- **Unidirectional Data Flow**: User actions → ViewModel → Repository → Data Sources → UI

The architecture is intentionally simple but allows for growth when needed.

## Further Reading

### Concept Guides

- [Design Philosophy](philosophy.md) - Understand the design principles behind the architecture
- [State Management](state-management.md) - Deep dive into the UiState pattern
- [Adding Features](guide.md) - Step-by-step guide to implementing new features
- [Data Flow](data-flow.md) - Understand data flow patterns (offline-first, caching, sync)
- [Dependency Injection](dependency-injection.md) - Complete guide to Hilt setup and patterns

### Module Documentation

- [Core UI Module](../core/ui/README.md) - State management utilities and UI components
- [Data Layer Module](../data/README.md) - Repository patterns and implementations
- [App Module](../app/README.md) - Application architecture and MainActivity setup
