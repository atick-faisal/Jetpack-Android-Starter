# Dependency Management

## Summary

This guide explains the project's dependency management strategy using Gradle Version Catalogs and
automated update tools (Renovate/Dependabot). Learn how to add dependencies, manage versions
centrally, use BOMs for version compatibility, and leverage automated dependency updates to keep the
project current.

---

This project uses Version Catalogs and automated dependency updates to maintain a clean and
up-to-date dependency management system.

## Version Catalog

Dependencies and versions are centrally managed in `gradle/libs.versions.toml`. This file serves as
a single source of truth for:

- Library dependencies
- Plugin versions
- Project configuration
- SDK versions

### Structure

The version catalog is organized into several sections:

1. **Versions**

   ```toml
   [versions]
   # Core versions
   java = "21"
   kotlin = "2.1.10"
   
   # SDK Configuration
   minSdk = "24"
   compileSdk = "35"
   targetSdk = "35"
   ```

2. **Plugins**

   ```toml
   [plugins]
   kotlin = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
   android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
   ```

3. **Libraries**

   ```toml
   [libraries]
   androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "androidxCore" }
   ```

> [!NOTE]
> Some versions in the catalog, like `java` and SDK versions, are not direct dependencies but are
> used by convention plugins and build configuration.

### Using Dependencies

In your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
}
```

For plugins:

```kotlin
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.android.library)
}
```

## Automated Dependency Updates

This project uses both Renovate and Dependabot to automate dependency updates.

### Renovate Setup

The project includes a `renovate.json` configuration:

```json
{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": [
    "config:base",
    "group:all",
    ":dependencyDashboard",
    "schedule:daily"
  ]
}
```

To enable Renovate, install the [Renovate app](https://github.com/apps/renovate) in your repository

### Dependabot Setup

The project includes a `.github/dependabot.yml` configuration:

```yaml
version: 2
updates:
  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"

  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    groups:
      kotlin-ksp-compose:
        patterns:
          - "org.jetbrains.kotlin:*"
          - "com.google.devtools.ksp"
          - "androidx.compose.compiler:compiler"
```

To enable Dependabot, ensure Dependabot is enabled in your repository settings

> [!TIP]
> You can group dependencies that you want to get updated together in dependabot config as it has
> been done for the `kotlin-ksp-group`.

## Best Practices

1. **Version Organization**:
    - Group related versions together
    - Use comments to separate sections
    - Keep SDK configurations in a dedicated section

2. **Version References**:

   ```toml
   [versions]
   compose-compiler = "1.5.3"
   
   [libraries]
   compose-compiler = { group = "androidx.compose.compiler", name = "compiler", version.ref = "compose-compiler" }
   ```

3. **Version Bundles**:
   Use BOM (Bill of Materials) when available:

   ```toml
   [libraries]
   compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "androidxComposeBom" }
   ```

4. **Custom Properties**:
   Store important project configuration in the version catalog:

   ```toml
   [versions]
   minSdk = "24"
   compileSdk = "35"
   targetSdk = "35"
   ```

> [!WARNING]
> Don't mix different versions of the same library family. Use BOMs when available to ensure
> compatibility.

## Adding New Dependencies

1. Add the version to `[versions]` section if needed
2. Add the library definition to `[libraries]` section
3. Use the new dependency in your module:

```kotlin
dependencies {
    implementation(libs.your.new.dependency)
}
```

## Further Reading

- **[Convention Plugins](plugins.md)** - Build logic and plugin configuration that uses version
  catalog
- **[Adding New Features](guide.md)** - Step-by-step guide for implementing new features
- **[Architecture Overview](architecture.md)** - Module structure and architectural patterns