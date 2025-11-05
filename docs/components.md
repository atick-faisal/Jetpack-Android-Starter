# Component Usage Guide

This guide covers the pre-built UI components available in the `core:ui` module, their usage patterns, customization options, and best practices.

## Overview

The Jetpack Android Starter template provides a curated set of Material 3 components with consistent styling and behavior patterns. These components are located in `core/ui/src/main/kotlin/dev/atick/core/ui/components/`.

### Component Philosophy

1. **Material 3 Foundation**: All components wrap Material 3 components with opinionated defaults
2. **Consistent Theming**: Components automatically adapt to your app's theme
3. **Accessibility First**: Built-in content descriptions and semantic properties
4. **Minimal Configuration**: Sensible defaults reduce boilerplate
5. **Composable**: Flexible content slots for customization

### When to Use Pre-Built Components

**Use pre-built components when:**
- You need standard UI patterns (buttons, text fields, app bars)
- You want consistent styling across your app
- You need accessibility features out of the box
- You want to reduce boilerplate code

**Create custom components when:**
- You need highly specialized UI behavior
- Pre-built components don't match your design requirements
- You need fine-grained control over component internals

## Component Reference

### Buttons

The template provides three button variants: filled, outlined, and text buttons. All support text and icon content.

#### JetpackButton (Filled)

**Purpose**: Primary actions, high emphasis

**Basic Usage**:
```kotlin
JetpackButton(
    onClick = { /* action */ },
    text = { Text("Continue") }
)
```

**With Leading Icon**:
```kotlin
JetpackButton(
    onClick = { /* action */ },
    text = { Text("Sign In") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Default.Login,
            contentDescription = null
        )
    }
)
```

**Custom Content**:
```kotlin
JetpackButton(
    onClick = { /* action */ },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Custom Layout")
    Icon(Icons.Default.ArrowForward, contentDescription = null)
}
```

**Parameters**:
- `onClick`: Callback when button is clicked
- `text`: Text label composable (in overload)
- `leadingIcon`: Optional icon before text (in overload)
- `content`: Custom content slot (in base overload)
- `modifier`: Modifier to customize appearance
- `enabled`: Boolean to enable/disable button (default: `true`)
- `contentPadding`: Internal padding (default: `ButtonDefaults.ContentPadding`)

**Theming**: Uses `MaterialTheme.colorScheme.onBackground` for container color

#### JetpackOutlinedButton

**Purpose**: Secondary actions, medium emphasis

**Usage**: Same API as `JetpackButton`, but with outlined style

```kotlin
JetpackOutlinedButton(
    onClick = { /* action */ },
    text = { Text("Cancel") }
)
```

**Theming**:
- Uses `MaterialTheme.colorScheme.outline` for border
- Adapts border color when disabled (12% alpha)
- 1.dp border width

#### JetpackTextButton

**Purpose**: Tertiary actions, low emphasis

**Usage**: Same API as `JetpackButton`, but with no background

```kotlin
JetpackTextButton(
    onClick = { /* action */ },
    text = { Text("Skip") }
)
```

**Common Pattern - Button Row**:
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    JetpackOutlinedButton(
        onClick = onCancel,
        text = { Text("Cancel") },
        modifier = Modifier.weight(1f)
    )
    JetpackButton(
        onClick = onConfirm,
        text = { Text("Confirm") },
        modifier = Modifier.weight(1f)
    )
}
```

### Text Fields

#### JetpackTextFiled

**Purpose**: Standard text input with validation support

**Basic Usage**:
```kotlin
var email by remember { mutableStateOf("") }

JetpackTextFiled(
    value = email,
    onValueChange = { email = it },
    label = { Text("Email") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null
        )
    }
)
```

**With Validation**:
```kotlin
val emailData by viewModel.emailData.collectAsStateWithLifecycle()

JetpackTextFiled(
    value = emailData.value,
    onValueChange = viewModel::updateEmail,
    label = { Text("Email") },
    leadingIcon = {
        Icon(Icons.Default.Email, contentDescription = null)
    },
    errorMessage = emailData.errorMessage,
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
    )
)
```

**Parameters**:
- `value`: Current text value
- `onValueChange`: Callback when text changes
- `label`: Label composable
- `leadingIcon`: Icon before text input
- `trailingIcon`: Icon after text input (optional)
- `errorMessage`: Error text to display (null for no error)
- `keyboardOptions`: Keyboard configuration (default: `KeyboardOptions.Default`)
- `modifier`: Modifier to customize appearance

**Validation Pattern**:
The recommended pattern uses `TextFiledData` from `core:ui`:

```kotlin
// In ViewModel
private val _uiState = MutableStateFlow(UiState(ScreenData()))

fun updateEmail(email: String) {
    _uiState.updateState {
        copy(
            email = TextFiledData(
                value = email,
                errorMessage = if (email.isEmailValid()) null else "Invalid email"
            )
        )
    }
}

// Data class
data class ScreenData(
    val email: TextFiledData = TextFiledData("")
)
```

**Styling**:
- 50% rounded corners
- Red border/color when error is present
- Animated error message appearance

#### JetpackPasswordFiled

**Purpose**: Password input with visibility toggle

**Usage**:
```kotlin
var password by remember { mutableStateOf("") }

JetpackPasswordFiled(
    value = password,
    onValueChange = { password = it },
    label = { Text("Password") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null
        )
    },
    errorMessage = if (password.length < 8) "Too short" else null
)
```

**Features**:
- Automatic visibility toggle button
- Password masking by default
- Uses `PasswordVisualTransformation`
- Saves visibility state across configuration changes

**Common Pattern - Login Form**:
```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    JetpackTextFiled(
        value = email,
        onValueChange = viewModel::updateEmail,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, null) },
        errorMessage = emailData.errorMessage
    )

    JetpackPasswordFiled(
        value = password,
        onValueChange = viewModel::updatePassword,
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        errorMessage = passwordData.errorMessage
    )

    JetpackButton(
        onClick = viewModel::signIn,
        text = { Text("Sign In") },
        modifier = Modifier.fillMaxWidth(),
        enabled = emailData.isValid && passwordData.isValid
    )
}
```

### Top App Bars

#### JetpackTopAppBar (Navigation + Action)

**Purpose**: Screen title with navigation and action icons

**Usage**:
```kotlin
JetpackTopAppBar(
    titleRes = R.string.screen_title,
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconContentDescription = "Navigate back",
    actionIcon = Icons.Default.Settings,
    actionIconContentDescription = "Open settings",
    onNavigationClick = { navController.navigateUp() },
    onActionClick = { /* open settings */ }
)
```

**Parameters**:
- `titleRes`: String resource for title
- `navigationIcon`: Leading icon (typically back arrow)
- `navigationIconContentDescription`: Accessibility label for navigation
- `actionIcon`: Trailing action icon
- `actionIconContentDescription`: Accessibility label for action
- `onNavigationClick`: Navigation callback
- `onActionClick`: Action callback
- `colors`: TopAppBar colors (default: Material 3 defaults)
- `modifier`: Modifier to customize appearance

#### JetpackTopAppBar (Action Only)

**Purpose**: Screen title with action icon only

**Usage**:
```kotlin
JetpackTopAppBar(
    titleRes = R.string.home,
    actionIcon = Icons.Default.Settings,
    actionIconContentDescription = "Settings",
    onActionClick = { /* open settings */ }
)
```

#### JetpackTopAppBarWithAvatar

**Purpose**: Screen title with user avatar

**Usage**:
```kotlin
JetpackTopAppBarWithAvatar(
    titleRes = R.string.home,
    avatarUri = userProfilePictureUri,
    avatarContentDescription = "Profile picture",
    onAvatarClick = { navController.navigateToProfile() }
)
```

**Features**:
- Loads avatar with Coil's `AsyncImage`
- Fallback to default avatar drawable
- Circular clip automatically applied

#### JetpackActionBar

**Purpose**: Screen with back navigation and text action button

**Usage**:
```kotlin
JetpackActionBar(
    titleRes = R.string.edit_profile,
    actionRes = R.string.save,
    onNavigateBackClick = { navController.navigateUp() },
    onActionClick = viewModel::saveProfile
)
```

**Unique Feature**: Uses `JetpackButton` instead of icon for the action

**Common Pattern - Scaffold with Top App Bar**:
```kotlin
Scaffold(
    topBar = {
        JetpackTopAppBar(
            titleRes = R.string.screen_title,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            navigationIconContentDescription = "Back",
            actionIcon = Icons.Default.MoreVert,
            actionIconContentDescription = "More options",
            onNavigationClick = { navController.navigateUp() },
            onActionClick = { /* show menu */ }
        )
    }
) { paddingValues ->
    // Screen content
    Content(modifier = Modifier.padding(paddingValues))
}
```

### Loading Indicators

#### JetpackLoadingWheel

**Purpose**: Animated loading indicator

**Usage**:
```kotlin
JetpackLoadingWheel(
    contentDesc = "Loading data",
    modifier = Modifier.size(48.dp)
)
```

**Features**:
- Custom animated wheel design
- 12 rotating lines with color transitions
- Automatic rotation animation
- Material 3 color scheme integration

**Parameters**:
- `contentDesc`: Accessibility content description
- `modifier`: Modifier (default includes 48.dp size)

#### JetpackOverlayLoadingWheel

**Purpose**: Loading indicator with semi-transparent background

**Usage**:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // Your content
    Content()

    // Show loading overlay
    if (isLoading) {
        JetpackOverlayLoadingWheel(
            contentDesc = "Loading",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
```

**Features**:
- Rounded corners (60.dp)
- Elevated surface (8.dp shadow)
- 83% opacity background
- Fixed 60.dp size

**Common Pattern - Loading State**:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    when {
        uiState.loading -> {
            JetpackOverlayLoadingWheel(
                contentDesc = "Loading content",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        uiState.data.isEmpty() -> {
            EmptyState(modifier = Modifier.align(Alignment.Center))
        }
        else -> {
            ContentList(items = uiState.data)
        }
    }
}
```

### Other Components

The template includes additional components for specific use cases:

- **Chip**: Material 3 chips (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Chip.kt`)
- **Tag**: Labeled tags (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Tag.kt`)
- **IconButton**: Themed icon buttons (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/IconButton.kt`)
- **ToggleButton**: Toggle button component (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/ToggleButton.kt`)
- **Fab**: Floating action buttons (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Fab.kt`)
- **Navigation**: Navigation components (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Navigation.kt`)
- **Tabs**: Tab components (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Tabs.kt`)
- **Divider**: Themed dividers (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Divider.kt`)
- **SwipeToDismiss**: Swipe-to-dismiss wrapper (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/SwipeToDismiss.kt`)
- **DynamicAsyncImage**: Image loading with Coil (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/DynamicAsyncImage.kt`)
- **Background**: App background component (see `core/ui/src/main/kotlin/dev/atick/core/ui/components/Background.kt`)

Refer to the source code and KDoc comments for detailed usage of these components.

## Theming Components

### Color Scheme

All components use Material 3's `MaterialTheme.colorScheme`. The default color scheme is defined in `core/ui/src/main/kotlin/dev/atick/core/ui/theme/Color.kt`.

**Component Color Usage**:
- **JetpackButton**: `colorScheme.onBackground` (container)
- **JetpackOutlinedButton**: `colorScheme.outline` (border)
- **Loading Indicators**: `colorScheme.onBackground` and `colorScheme.inversePrimary`
- **Text Fields**: `colorScheme.error` (when error is present)

### Customizing Colors

**Option 1: Modify theme colors** (affects all components):

```kotlin
// In core/ui/theme/Color.kt
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onBackground = Color(0xFF1C1B1F),
    // ... other colors
)
```

**Option 2: Override colors for specific component instances**:

```kotlin
JetpackButton(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color.Red // Override default
    ),
    content = { Text("Delete") }
)
```

### Typography

Components use Material 3 typography defined in `core/ui/src/main/kotlin/dev/atick/core/ui/theme/Type.kt`.

**Customizing Typography**:

```kotlin
// In core/ui/theme/Type.kt
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    // ... other text styles
)
```

### Dynamic Color

The app supports Material You dynamic colors. Enable in `SettingsScreen`:

```kotlin
// In feature/settings module
settingsViewModel.updateDynamicColorPreference(true)
```

Dynamic colors automatically adapt to the user's wallpaper (Android 12+).

## Accessibility Best Practices

### Content Descriptions

**Always provide content descriptions for icons**:

```kotlin
Icon(
    imageVector = Icons.Default.Home,
    contentDescription = "Navigate to home" // ✅ Good
)

Icon(
    imageVector = Icons.Default.Home,
    contentDescription = null // ✅ Only for decorative icons
)
```

**Use semantic properties**:

```kotlin
JetpackLoadingWheel(
    contentDesc = "Loading your data" // ✅ Describes what's loading
)
```

### Touch Targets

Ensure interactive elements meet minimum size requirements (48.dp):

```kotlin
IconButton(
    onClick = { },
    modifier = Modifier.size(48.dp) // ✅ Meets minimum
) {
    Icon(Icons.Default.Close, contentDescription = "Close")
}
```

### Contrast

Components automatically use theme colors with sufficient contrast. Verify custom colors meet WCAG AA standards:

- Normal text: 4.5:1 contrast ratio
- Large text: 3:1 contrast ratio

## Custom Component Creation

When pre-built components don't meet your needs, create custom components following these patterns:

### 1. Create Component File

Create in `core/ui/src/main/kotlin/dev/atick/core/ui/components/YourComponent.kt`:

```kotlin
/*
 * Copyright 2025 Your Name
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * ...
 */

package dev.atick.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Custom component description.
 *
 * @param param1 Parameter description
 * @param modifier Modifier to customize appearance
 */
@Composable
fun CustomComponent(
    param1: String,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### 2. Follow Material 3 Patterns

Use Material 3 components as building blocks:

```kotlin
@Composable
fun CustomCard(
    title: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
```

### 3. Support Theming

Always use `MaterialTheme` properties instead of hardcoded values:

```kotlin
// ❌ Bad
Text(text = "Title", color = Color(0xFF000000))

// ✅ Good
Text(
    text = "Title",
    color = MaterialTheme.colorScheme.onSurface
)
```

### 4. Add Previews

Use `@Preview` annotations for design iteration:

```kotlin
@PreviewDevices
@PreviewThemes
@Composable
private fun CustomComponentPreview() {
    JetpackTheme {
        CustomComponent(param1 = "Preview")
    }
}
```

**Preview Annotations**:
- `@PreviewDevices`: Shows component on multiple device sizes
- `@PreviewThemes`: Shows component in light and dark themes

### 5. Document with KDoc

Provide comprehensive KDoc for all public components:

```kotlin
/**
 * A custom card component for displaying user information.
 *
 * Example usage:
 * ```
 * UserCard(
 *     name = "John Doe",
 *     email = "john@example.com"
 * )
 * ```
 *
 * @param name The user's display name
 * @param email The user's email address
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun UserCard(/* ... */) { /* ... */ }
```

## Component Organization

### File Structure

```
core/ui/src/main/kotlin/dev/atick/core/ui/
├── components/
│   ├── Button.kt         # All button variants
│   ├── TextField.kt      # All text field variants
│   ├── TopAppBar.kt      # All app bar variants
│   ├── LoadingWheel.kt   # Loading indicators
│   └── ...
├── theme/
│   ├── Color.kt          # Color definitions
│   ├── Type.kt           # Typography
│   └── Theme.kt          # Theme setup
└── utils/
    └── ...               # UI utilities
```

### Naming Conventions

- **Component files**: PascalCase, describe component type (e.g., `Button.kt`)
- **Component functions**: Prefixed with `Jetpack` (e.g., `JetpackButton`)
- **Variants**: Use descriptive suffixes (e.g., `JetpackOutlinedButton`)
- **Private helpers**: Standard Kotlin naming (e.g., `JetpackButtonContent`)

### When to Split Files

Create a new file when:
- Component has multiple complex variants (3+ functions)
- File exceeds ~300 lines
- Component is unrelated to others in the file

Keep in one file when:
- Variants are closely related (e.g., filled/outlined/text buttons)
- Total complexity is low
- Components share private helpers

## Testing Components

### Unit Testing Composables

Use Compose testing library:

```kotlin
class ButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun button_clickTriggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            JetpackButton(
                onClick = { clicked = true },
                text = { Text("Click Me") }
            )
        }

        composeTestRule
            .onNodeWithText("Click Me")
            .performClick()

        assertTrue(clicked)
    }
}
```

### Screenshot Testing

Capture component screenshots for visual regression testing:

```kotlin
@Test
fun button_screenshotTest() {
    composeTestRule.setContent {
        JetpackTheme {
            JetpackButton(
                onClick = { },
                text = { Text("Screenshot") }
            )
        }
    }

    composeTestRule
        .onNodeWithText("Screenshot")
        .captureToImage()
        .assertAgainstGolden("button_screenshot")
}
```

## Performance Considerations

### Composition Optimization

**Use `remember` for expensive computations**:

```kotlin
@Composable
fun ExpensiveComponent(data: List<Item>) {
    val processedData = remember(data) {
        data.map { /* expensive transformation */ }
    }
    // Use processedData
}
```

**Avoid creating new lambdas in composition**:

```kotlin
// ❌ Bad - Creates new lambda on each recomposition
JetpackButton(
    onClick = { viewModel.doSomething(item) },
    text = { Text("Action") }
)

// ✅ Good - Stable reference
val onClick = remember(item) {
    { viewModel.doSomething(item) }
}
JetpackButton(
    onClick = onClick,
    text = { Text("Action") }
)
```

### LazyList Optimization

When using components in lists:

```kotlin
LazyColumn {
    items(
        items = itemList,
        key = { it.id } // ✅ Stable key for better performance
    ) { item ->
        ItemCard(item = item)
    }
}
```

### Image Loading

Use `DynamicAsyncImage` for efficient image loading:

```kotlin
DynamicAsyncImage(
    imageUrl = item.imageUrl,
    contentDescription = item.title,
    modifier = Modifier.size(100.dp)
)
```

Features:
- Automatic memory and disk caching (Coil)
- Crossfade animations
- Placeholder and error handling

## Migration from XML Views

If migrating from XML to Compose, use these mappings:

| XML View | Jetpack Component |
|----------|-------------------|
| `<Button>` | `JetpackButton` |
| `<EditText>` | `JetpackTextFiled` |
| `<Toolbar>` | `JetpackTopAppBar` |
| `<ProgressBar>` | `JetpackLoadingWheel` |
| `<ImageView>` | `DynamicAsyncImage` |

**Example Migration**:

```xml
<!-- Before (XML) -->
<Button
    android:id="@+id/submitButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/submit"
    android:onClick="onSubmitClick" />
```

```kotlin
// After (Compose)
JetpackButton(
    onClick = viewModel::onSubmitClick,
    text = { Text(stringResource(R.string.submit)) },
    modifier = Modifier.fillMaxWidth()
)
```

---

## Summary

The component library provides:

- **Consistent UI** across your app with minimal effort
- **Accessibility** features built-in
- **Material 3** theming and dynamic colors
- **Flexibility** through content slots and modifiers
- **Performance** optimizations for common patterns

Follow the patterns in this guide to build beautiful, accessible, and performant UIs with minimal boilerplate.

## Further Reading

- [Architecture Overview](architecture.md) - Understand where components fit in the architecture
- [State Management](state-management.md) - Learn how to manage component state
- [Adding Features](guide.md) - Step-by-step guide to building features with components
- [Material 3 Guidelines](https://m3.material.io/) - Official Material Design 3 documentation
- API Reference: See [`core/ui`](../core/ui/README.md) for module architecture
