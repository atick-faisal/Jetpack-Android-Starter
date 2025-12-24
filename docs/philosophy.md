# Design Philosophy

This project prioritizes pragmatic simplicity over theoretical purity, making conscious trade-offs
that favor maintainability and readability over absolute correctness or flexibility.

## Core Principles

### 1. Pragmatic Simplicity

We favor straightforward, understandable solutions over complex but theoretically "pure" ones. This
means:

- Using direct state management approaches
- Minimizing layers when possible
- Keeping code readable and debuggable

### 2. Centralized State Management

#### UiState Wrapper

All UI state is wrapped in a consistent structure:

```kotlin
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null)
)
```

#### StatefulComposable Pattern

Instead of implementing error and loading states individually for each screen, we handle these
centrally:

```kotlin
@Composable
fun <T : Any> StatefulComposable(
    state: UiState<T>,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    content: @Composable (T) -> Unit
) {
    // Centralized loading and error handling
    content(state.data)
}
```

> [!NOTE]
> This approach trades some flexibility for consistency and reduced boilerplate.

### 3. Direct ViewModels

We opt for a direct approach to state management in ViewModels:

```kotlin
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(AuthScreenData()))
    val uiState = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.updateState {
            copy(
                email = TextFiledData(
                    value = email,
                    errorMessage = email.validateEmail()
                )
            )
        }
    }
}
```

**Trade-offs Made:**

- ✅ Readability: State changes are explicit and easy to trace
- ✅ Debuggability: Direct state mutations are easier to debug
- ✅ Simplicity: Easier to manage multiple UI events
- ❌ Purity: Less adherence to functional programming principles

### 4. Error Handling

We use a consistent error handling approach throughout the app:

1. **Repository Layer**: Uses `Result` type

	```kotlin
	suspend fun getData(): Result<Data> = suspendRunCatching {
	   dataSource.getData()
	}
	```

2. **ViewModel Layer**: Converts to `OneTimeEvent`

	```kotlin
	viewModelScope.launch {
	   repository.getData()
	       .onSuccess { data ->
	           _uiState.updateState { copy(data = data) }
	       }
	       .onFailure { error ->
	           _uiState.updateState {
	               copy(error = OneTimeEvent(error))
	           }
	       }
	}
	```

3. **UI Layer**: Handled by `StatefulComposable`

> [!TIP]
> This standardized approach makes error handling predictable across the entire application.

### 5. State Updates

We provide extension functions for common state update patterns:

```kotlin
// Regular state updates
_uiState.updateState {
    copy(value = newValue)
}

// Async operations with loading state
_uiState.updateStateWith {
    repository.someAsyncOperation()
}
```

### 6. Feature Organization

Each feature is self-contained and follows a consistent structure:

- `feature/auth/` - Authentication feature module
  - `navigation/` - Navigation-related code
  - `ui/` - UI components and ViewModels
  - `model/` - Feature-specific models

## Conscious Trade-offs

Our design philosophy makes several conscious trade-offs:

1. **Simplicity vs. Flexibility**
	- We choose simpler solutions even if they're less flexible
	- Custom solutions are added only when really needed

2. **Convention vs. Configuration**
	- We favor strong conventions over configuration options
	- This reduces decision fatigue but may limit customization

3. **Pragmatism vs. Purity**
	- We prioritize practical solutions over theoretical purity
	- This may mean occasionally breaking "clean" architecture rules

4. **Consistency vs. Optimization**
	- We prefer consistent patterns across the codebase
	- This might mean using the same solution even when a specialized one might be marginally better

> [!IMPORTANT]
> These patterns are guidelines, not rules. The goal is to make the codebase more maintainable and
> easier to understand, not to restrict flexibility where it's truly needed.

## Benefits of This Approach

1. **Reduced Cognitive Load**
	- Developers can predict where to find things
	- Common patterns reduce decision fatigue

2. **Easier Onboarding**
	- New team members can quickly understand the codebase
	- Consistent patterns make learning curve smoother

3. **Better Maintainability**
	- Common patterns make code more predictable
	- Centralized handling reduces bugs

4. **Improved Debugging**
	- State changes are explicit and traceable
	- Error handling is consistent and predictable

## When to Break These Rules

While these patterns serve well for most cases, consider alternatives when:

1. A feature has unique error/loading UI requirements
2. Performance optimizations are crucial
3. Complex business logic demands more separation
4. Third-party integration requires different patterns

> [!TIP]
> When deviating from these patterns, document your reasoning to help other developers understand
> the context of your decisions.

---

## Summary

This template's design philosophy prioritizes:

- **Pragmatic simplicity** over theoretical purity
- **Centralized state management** with UiState and StatefulComposable
- **Direct, explicit state updates** for readability and debuggability
- **Consistent error handling** across all layers
- **Strong conventions** over flexible configuration

These trade-offs favor maintainability, readability, and easier onboarding at the cost of some flexibility.

## Further Reading

- [Architecture Overview](architecture.md) - Understand the two-layer architecture
- [State Management](state-management.md) - Deep dive into UiState and update functions
- [Adding Features](guide.md) - Step-by-step guide applying these principles
