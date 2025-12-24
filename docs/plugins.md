# Convention Plugins

## Summary

This guide explains the project's convention plugins system that centralizes build configuration in
the `build-logic` directory. Learn about the five available plugins (Application, Library, UI
Library, Dagger Hilt, Firebase), how to use them in your modules, and how to create custom
convention plugins to avoid build configuration duplication.

---

This project uses Gradle Convention Plugins to centralize common build logic and simplify
module-level build files. These plugins are located in the `build-logic` directory.

## Available Plugins

### 1. Application Plugin (`dev.atick.application`)

Used for the main application module:

- Configures Android application defaults
- Sets up Compose
- Enables BuildConfig
- Configures Kotlin and Java versions
- Enables Kotlin context parameters (`-Xcontext-parameters`)
- Sets up Compose compiler metrics and reports

```kotlin
plugins {
    alias(libs.plugins.jetpack.application)
}
```

### 2. Library Plugin (`dev.atick.library`)

Base plugin for Android library modules:

- Configures Android library defaults
- Sets up Kotlin
- Configures Java versions
- Enables KotlinX Serialization

```kotlin
plugins {
    alias(libs.plugins.jetpack.library)
}
```

### 3. UI Library Plugin (`dev.atick.ui.library`)

Extended library plugin for UI modules:

- Includes all Library Plugin features
- Enables Jetpack Compose
- Adds Material3 experimental opt-ins
- Enables Kotlin context parameters (`-Xcontext-parameters`)

```kotlin
plugins {
    alias(libs.plugins.jetpack.ui.library)
}
```

### 4. Dagger Hilt Plugin (`dev.atick.dagger.hilt`)

Sets up Dagger Hilt in a module:

- Applies Hilt Android plugin
- Adds KSP for annotation processing
- Configures Hilt dependencies

```kotlin
plugins {
    alias(libs.plugins.jetpack.dagger.hilt)
}
```

### 5. Firebase Plugin (`dev.atick.firebase`)

Configures Firebase services:

- Sets up Firebase BoM
- Configures Analytics, Crashlytics, and Performance
- Applies necessary Google Services plugins

```kotlin
plugins {
    alias(libs.plugins.jetpack.firebase)
}
```

## Using the Plugins

To use these plugins in your module's `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.jetpack.dagger.hilt)
    // Add other plugins as needed
}
```

> [!NOTE]
> The plugins are already registered in the version catalog (`libs.versions.toml`), making them
> easily accessible across all modules.

## Creating Custom Plugins

To create your own convention plugin:

1. Add your plugin class in `build-logic/convention/src/main/kotlin`:

   ```kotlin
   class CustomConventionPlugin : Plugin<Project> {
       override fun apply(target: Project) {
           with(target) {
               // Your plugin configuration here
           }
       }
   }
   ```

2. Register it in `build-logic/convention/build.gradle.kts`:

   ```kotlin
   gradlePlugin {
       plugins {
           register("customPlugin") {
               id = "dev.your.plugin.id"
               implementationClass = "CustomConventionPlugin"
           }
       }
   }
   ```

3. Add it to `libs.versions.toml`:

   ```toml
   [plugins]
   custom-plugin = { id = "dev.your.plugin.id" }
   ```

## Plugin Development Tips

1. **Accessing Version Catalog**:

   ```kotlin
   val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
   val version = libs.findVersion("your.version").get()
   ```

2. **Configuring Android Extensions**:

   ```kotlin
   extensions.configure<LibraryExtension> {
       // Your configuration
   }
   ```

3. **Adding Dependencies**:

   ```kotlin
   dependencies {
       "implementation"(libs.findLibrary("library.name").get())
   }
   ```

> [!TIP]
> Keep your plugins focused and single-purpose. Create new plugins for distinct functionality rather
> than overloading existing ones.

## Best Practices

1. **Module Organization**:

    - Use `jetpack.application` for the app module
    - Use `jetpack.ui.library` for feature modules with UI
    - Use `jetpack.library` for core modules without UI

2. **Plugin Combinations**:

   ```kotlin
   plugins {
       alias(libs.plugins.jetpack.ui.library) // Base UI setup
       alias(libs.plugins.jetpack.dagger.hilt) // Add Hilt support
       alias(libs.plugins.jetpack.firebase) // Add Firebase if needed
   }
   ```

3. **Version Management**:

    - Keep versions in the version catalog
    - Reference them in plugins using `libs.findVersion()`

> [!WARNING]
> Don't repeat plugin configurations in module-level build files. If you find yourself doing this,
> consider creating a new convention plugin instead.

## Further Reading

- **[Dependency Management](dependency.md)** - Version catalog management used by convention plugins
- **[Architecture Overview](architecture.md)** - Module structure and how plugins support it
- **[Adding New Features](guide.md)** - Step-by-step guide that uses convention plugins