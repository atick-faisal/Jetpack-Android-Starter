# Tips and Tricks

Get the most out of this template with these useful tips and features organized by category.

---

## UI Layer Tips

### Pre-built Components

The `core:ui/components` directory contains pre-built Material3 composables ready to use:

```kotlin
// Buttons
JetpackButton(onClick = {})
JetpackOutlinedButton(onClick = {})
JetpackTextButton(onClick = {})
JetpackIconToggleButton(checked = isChecked, onCheckedChange = {})
JetpackExtendedFab(text = "Create", icon = Icons.Default.Add, onClick = {})

// Input
JetpackTextField(value = "", onValueChange = {})

// Selection
JetpackFilterChip(selected = isSelected, onClick = {}, label = { Text("Filter") })
JetpackTag(followed = isFollowed, onClick = {}, text = "Topic")
JetpackToggleOptions(options = listOf("Light", "Dark"), selectedIndex = 0, onOptionSelected = {})

// Navigation
JetpackNavigationSuiteScaffold(...)  // Adaptive: bottom bar/rail/drawer
JetpackNavigationBar(...)            // Bottom navigation
JetpackNavigationRail(...)           // Side navigation
JetpackTab(...)                      // Individual tab
JetpackTabRow(...)                   // Tab container

// Display
JetpackLoadingWheel(contentDesc = "")
JetpackOverlayLoadingWheel(contentDesc = "")
DynamicAsyncImage(imageUrl = url, contentDescription = "")
DividerWithText(text = "OR")

// Layout
AppBackground(modifier = Modifier)
AppGradientBackground(gradientColors = gradientColors)
SwipeToDismiss(onDelete = {})

// App bars
JetpackTopAppBar(...)
```

> [!TIP]
> Always use these pre-built components instead of creating new ones. They provide consistent
> styling across your app and can be modified centrally in `core:ui`.

### Theming System

The `core:ui/theme` directory provides a complete theming system with Material3 support:

```kotlin
// Apply custom theme with dynamic color support
JetpackTheme(
    darkTheme = isDarkTheme,
    disableDynamicTheming = false  // Enable Material You dynamic colors
) {
    // Your content
}

// Use decorative backgrounds
AppGradientBackground(
    gradientColors = GradientColors()  // Automatic theme-aware gradient
) {
    // Your content
}
```

### Composable Previews

Use the provided multi-preview annotations for efficient UI development:

```kotlin
@Composable
@PreviewThemes     // Preview in both light and dark themes
@PreviewDevices    // Preview on different device sizes
fun YourComposablePreview() {
    JetpackTheme {
        YourComposable()
    }
}
```

**Preview devices include**:
- Phone (360×640dp @ 480dpi)
- Landscape (640×360dp @ 480dpi)
- Foldable (673×841dp @ 480dpi)
- Tablet (1280×800dp @ 480dpi)

**Preview themes**: Light and Dark modes

Combining both annotations generates **8 previews** (4 devices × 2 themes) automatically!

### State Management Utilities

The template provides helper functions for managing UI state in ViewModels:

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(FeatureScreenData()))
    val uiState = _uiState.asStateFlow()

    // Synchronous state updates (no loading indicator)
    fun updateName(name: String) {
        _uiState.updateState {
            copy(name = name)
        }
    }

    // Async operations that return new data (automatic loading + error handling)
    fun loadData() {
        _uiState.updateStateWith {
            repository.getData()  // Returns Result<FeatureScreenData>
        }
    }

    // Async operations that perform actions (automatic loading + error handling)
    fun saveData() {
        _uiState.updateWith {
            repository.saveData(this)  // Returns Result<Unit>
        }
    }
}
```

**Helper functions**:
- `updateState { }` - Synchronous updates (text input, toggles, local state)
- `updateStateWith { }` - Async operations returning new data (load, refresh, search)
- `updateWith { }` - Async operations performing actions (save, delete, send)

> [!NOTE]
> These functions use Kotlin's context parameters. The ViewModel scope is automatically available -
> you never need to pass it explicitly.

### Automatic Error Handling

Use `StatefulComposable` to automatically handle loading states and errors:

```kotlin
@Composable
fun FeatureRoute(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = uiState,
        onShowSnackbar = onShowSnackbar
    ) { screenData ->
        FeatureScreen(
            screenData = screenData,
            onAction = viewModel::handleAction
        )
    }
}
```

**What StatefulComposable does**:
- Displays your content based on current data
- Shows overlay loading indicator when `loading = true`
- Automatically displays errors via snackbar
- Includes "Report" button that logs errors to Firebase Crashlytics

> [!TIP]
> This pattern enforces clean separation: Route composable manages state collection,
> Screen composable is pure UI with no ViewModel dependency.

---

## Data Layer Tips

### String Validation Extensions

The `core:android` module provides validation utilities in `StringExtensions.kt`:

```kotlin
// Email validation
email.isEmailValid()  // Uses Android's Patterns.EMAIL_ADDRESS

// Password validation (8-20 chars, at least 1 digit, 1 lowercase letter)
password.isPasswordValid()

// Full name validation (at least 2 parts, letters only)
name.isValidFullName()
```

**Usage example**:

```kotlin
// In a ViewModel - validate user input
fun validateEmail(email: String): Boolean {
    return email.isEmailValid()
}

// In a Composable - show error state
var email by remember { mutableStateOf("") }

OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    isError = email.isNotEmpty() && !email.isEmailValid(),
    label = { Text("Email") }
)
```

> [!NOTE]
> All validation functions are null-safe - they return `false` for null or empty strings.

---

## IDE Productivity Tips

### Secrets Management

The template includes the `gradle-secrets-plugin` for secure credentials management:

```properties
# local.properties (git-ignored)
API_KEY=your_actual_api_key
API_SECRET=your_actual_secret
```

```properties
# secrets.defaults.properties (version controlled)
API_KEY=dummy_api_key
API_SECRET=dummy_secret
```

Access secrets as `BuildConfig` fields:

```kotlin
val apiKey = BuildConfig.apiKey
```

> [!TIP]
> Use `secrets.defaults.properties` to provide dummy values for CI/CD environments while keeping
> sensitive data in `local.properties`.

### Documentation Generation

The template comes with Dokka configured for API documentation generation:

```bash
# Generate API documentation
./gradlew dokkaGeneratePublicationHtml

# The generated docs will be available at:
# build/dokka/html/index.html
```

MkDocs is also configured for guide documentation:

```bash
# Install MkDocs (once)
pip install mkdocs mkdocs-material

# Serve documentation locally
mkdocs serve

# View at http://localhost:8000
```

> [!NOTE]
> If you're using GitHub, documentation is automatically generated and published through the
> `.github/workflows/docs.yml` workflow.

### Multilingual Support

Automatic locale configuration is enabled in `app/build.gradle.kts`:

```kotlin
androidResources {
    generateLocaleConfig = true
}
```

Locale preferences are saved and applied automatically via `AndroidManifest.xml`:

```xml
<service
    android:name="androidx.appcompat.app.AppLocalesMetadataHolderService"
    android:enabled="false"
    android:exported="false">
    <meta-data
        android:name="autoStoreLocales"
        android:value="true" />
</service>
```

This automatically:
- Generates `LocaleConfig` from your string resource files
- Saves user's language preference
- Applies saved locale on app restart

Read more: [Android App Languages Guide](https://developer.android.com/guide/topics/resources/app-languages)

---

## Performance Tips

### Image Loading Optimization

Use `DynamicAsyncImage` for all remote images - it's optimized with Coil:

```kotlin
DynamicAsyncImage(
    imageUrl = user.avatarUrl,
    contentDescription = "User avatar",
    modifier = Modifier.size(48.dp)
)
```

**Built-in optimizations** (from `CoilModule.kt`):
- Automatic disk caching
- Memory caching
- Crossfade animations
- Placeholder and error handling
- Respect dark theme (no-ops color filter on dark backgrounds)

> [!TIP]
> For more image loading optimization techniques, see the [Performance Guide](performance.md#image-loading-optimization).

### Loading State Best Practices

Leverage the centralized loading patterns:

```kotlin
// Overlay loading (blocks interaction)
if (state.loading) {
    JetpackOverlayLoadingWheel(contentDesc = "Loading data")
}

// Inline loading (doesn't block)
if (isRefreshing) {
    JetpackLoadingWheel(contentDesc = "Refreshing")
}
```

> [!NOTE]
> `StatefulComposable` automatically handles overlay loading based on `UiState.loading`.
> Use inline loading for pull-to-refresh or partial screen updates.

---

## Best Practices

### Component Usage

1. **Use Pre-built Components**
   - Leverage components in `core:ui/components`
   - Maintain consistent styling across the app
   - Centralize design system changes

2. **Follow the State Management Pattern**
   - Route composable → manages ViewModel + state collection
   - Screen composable → pure UI with data + event callbacks
   - Use `StatefulComposable` for automatic loading/error handling

3. **Validation**
   - Use built-in validation extensions (`isEmailValid`, `isPasswordValid`)
   - Show validation errors inline with `isError` parameter
   - Validate on input change, not just on submit

### Documentation

1. **Keep KDoc Updated**
   - Document all public APIs with KDoc comments
   - Use Dokka to generate API documentation
   - Leverage automated docs workflow on GitHub

2. **Use Module READMEs**
   - Each module has a README explaining its purpose
   - Module READMEs appear in Dokka output
   - Keep them focused on module architecture

3. **Secrets Management**
   - Store sensitive data in `local.properties` (git-ignored)
   - Provide defaults in `secrets.defaults.properties`
   - Use `BuildConfig` fields for compile-time configuration

> [!TIP]
> Explore the `core` modules thoroughly - they contain many utilities that can save you time and
> ensure consistency across your app.

---

## Git Workflow Tips

### Commit Message Convention

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) standard:

```bash
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Commit Types**:
- `feat` - New feature for the user
- `fix` - Bug fix
- `docs` - Documentation changes
- `refactor` - Code change that neither fixes a bug nor adds a feature
- `test` - Adding or updating tests
- `chore` - Maintenance tasks, dependency updates
- `style` - Code style changes (formatting, missing semi-colons, etc.)
- `perf` - Performance improvements

**Examples**:

```bash
# Feature addition
feat(auth): add password reset functionality

# Bug fix
fix(home): resolve crash on empty data

# Documentation update
docs: update repository pattern guide

# Refactoring
refactor(ui): simplify state management utilities
```

> [!TIP]
> Following Conventional Commits enables automatic changelog generation and semantic versioning.

### Pre-commit Workflow

Always run these commands before committing:

```bash
# 1. Format code with Spotless
./gradlew spotlessApply

# 2. Build to check for compilation errors
./gradlew build

# 3. Stage and commit with conventional commit message
git add .
git commit -m "docs: update tips documentation"
```

> [!IMPORTANT]
> The CI workflow runs `spotlessCheck` on pull requests. Your PR will fail if code is not properly formatted.

### Git Ignore Configuration

The `.gitignore` is pre-configured to exclude:
- Build artifacts (`build/`, `*.apk`)
- Local configuration (`local.properties`, `keystore.properties`)
- IDE files (`.idea/`, `*.iml`)
- Firebase config (`google-services.json`)
- Keystore files (`*.jks`, `*.keystore`)
- AI tool configs (`.claude/`, `.gemini/`)

> [!WARNING]
> Never commit `keystore.properties`, keystore files, or production `google-services.json` to version control.

---

## Android Studio Productivity

### IDE Run Configurations

The project includes pre-configured run configurations in the `.run` directory:

**Spotless Check** (`Spotless Check.run.xml`)
- Verifies if all files conform to formatting rules
- Run from toolbar or with `./gradlew spotlessCheck`

**Spotless Apply** (`Spotless Apply.run.xml`)
- Automatically formats all files
- Run from toolbar or with `./gradlew spotlessApply`
- **Always run before committing**

**Generate Docs** (`Generate Docs.run.xml`)
- Generates Dokka HTML documentation
- Run from toolbar or with `./gradlew dokkaGeneratePublicationHtml`

**Signing Report** (`Signing Report.run.xml`)
- Displays SHA-1 fingerprint for Firebase setup
- Run from toolbar or with `./gradlew signingReport`

> [!TIP]
> Bind frequently-used run configurations to keyboard shortcuts for faster access. Go to Settings → Keymap → External Tools.

### EditorConfig Integration

The project includes `.editorconfig` for consistent code style:

```editorconfig
[*.{kt,kts}]
ij_kotlin_allow_trailing_comma=true
ij_kotlin_allow_trailing_comma_on_call_site=true
ktlint_function_naming_ignore_when_annotated_with=Composable, Test
```

**Features**:
- Trailing commas enabled for cleaner diffs
- Composable functions exempt from standard naming rules
- Automatic ktlint configuration for Spotless

> [!NOTE]
> Android Studio automatically applies EditorConfig settings. No additional setup required.

---

## Debugging Tips

### LeakCanary Memory Leak Detection

LeakCanary is automatically included in debug builds:

```kotlin
// app/build.gradle.kts
debugImplementation(libs.leakcanary.android)
```

**What it does**:
- Automatically detects memory leaks in debug builds
- Shows notification when leaks are found
- Provides detailed leak trace in the app

**Usage**:
1. Run debug build on device/emulator
2. Use the app normally
3. LeakCanary notifies you if it detects leaks
4. Tap notification to view leak trace
5. Fix the leak based on the trace information

> [!TIP]
> LeakCanary only runs in debug builds - zero overhead in release builds.

### Compose Preview Optimization

Use multi-preview annotations for efficient preview generation:

```kotlin
@Composable
@PreviewThemes     // Generates 2 previews (light + dark)
@PreviewDevices    // Generates 4 previews (phone, landscape, foldable, tablet)
fun YourComposablePreview() {
    JetpackTheme {
        YourComposable()
    }
}
```

**Combining both annotations generates 8 previews** (4 devices × 2 themes) automatically!

> [!TIP]
> In Android Studio, use "Pin" to keep specific previews visible while editing for instant visual feedback.

### Build Performance

**Enable Gradle parallel execution** in `gradle.properties`:

```properties
org.gradle.parallel=true
org.gradle.caching=true
```

**Use build cache**:

```bash
# Clean build with cache
./gradlew clean build --build-cache

# Check what tasks are cached
./gradlew build --scan
```

> [!NOTE]
> These settings are already configured in the template's `gradle.properties`.

---

### Module Documentation

- [Core UI Module](../core/ui/README.md) - UI components, state management utilities, and theming
- [Core Android Module](../core/android/README.md) - Android utilities, extensions, and DI qualifiers
- [Core Network Module](../core/network/README.md) - Network utilities and HTTP client configuration
- [Data Layer](../data/README.md) - Repository patterns and data source management
- [Sync Module](../sync/README.md) - Background synchronization with WorkManager

### Concept Guides

- [State Management Guide](state-management.md) - Deep dive into state patterns and best practices
- [Components Guide](components.md) - Comprehensive guide to all available components
- [Performance Optimization](performance.md) - Performance best practices and optimization techniques
- [Quick Reference](quick-reference.md) - Cheat sheet for common patterns and utilities
- [Navigation Deep Dive](navigation.md) - Type-safe navigation patterns and implementation
- [Architecture Overview](architecture.md) - Two-layer architecture and design decisions
- [Dependency Injection](dependency-injection.md) - Hilt setup and patterns
