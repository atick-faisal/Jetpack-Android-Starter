# Module :feature:home

Main feature module implementing the home screen with item list and CRUD operations.

## Features

- Item List Display
- Create/Edit Items
- Delete with Undo
- Offline Support
- Pull to Refresh
- Background Sync

## Dependencies Graph

```mermaid
graph TD
    A[feature:home] --> B[core:ui]
    A --> C[data]
    B --> D[core:android]

    subgraph "Core Dependencies"
        B
        C
        D
    end
```

## Key Components

1. **Home Screen**: Shows the list of items

	```kotlin
	@Composable
	fun HomeRoute(
	    onItemClick: (String) -> Unit,
	    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
	)
	```

2. **Item Screen**: Create/Edit item screen

	```kotlin
	@Composable
	fun ItemRoute(
	   onBackClick: () -> Unit,
	   onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
	)
	```

## Related Documentation

- **[Adding a Feature Guide](../../docs/guide.md)** - Step-by-step template for creating new features
- **[State Management](../../docs/state-management.md)** - UiState pattern and ViewModel best practices
- **[Navigation Deep Dive](../../docs/navigation.md)** - Type-safe navigation implementation
- **[Data Layer](../../data/README.md)** - Repository patterns used in this feature