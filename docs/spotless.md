# Spotless Linting Setup

## Summary

This guide explains the project's code formatting and license header management using Spotless with
ktlint. Learn how to check and apply formatting, customize copyright headers, configure IDE
integration, and ensure your code passes CI checks. Always run `spotlessApply` before committing to
avoid CI failures.

---

This project uses Spotless for code formatting and license header management. Spotless is configured
to work with Kotlin, Groovy, Gradle KTS files, and XML files.

## Features

The Spotless configuration includes:

- Kotlin formatting using [ktlint](https://github.com/pinterest/ktlint)
- Customizable formatting using `ktlint` and the `.editorconfig` file
- [Custom Compose UI rules](https://github.com/mrmans0n/compose-rules?ref=nlopez.io) for better
  Jetpack Compose code formatting
- License header management for all supported file types
- Gradle and XML file formatting

## Using Spotless

### IntelliJ/Android Studio Run Configurations

The project includes pre-configured run configurations in the `.run` directory:

1. **Spotless Check**: Verifies if all files conform to the formatting rules
    - Run using: `./gradlew spotlessCheck`
    - Or use the "Spotless Check" run configuration in Android Studio

2. **Spotless Apply**: Automatically formats all files according to the rules
    - Run using: `./gradlew spotlessApply`
    - Or use the "Spotless Apply" run configuration in Android Studio

> [!TIP]
> Always run Spotless Apply before committing your changes to ensure consistent code formatting.

### CI/CD Integration

The project's GitHub Actions workflow automatically runs `spotlessCheck` on pull requests. Your PR
will fail if there are any formatting issues.

## Customizing Copyright Headers

The project includes default copyright headers in the `spotless` directory:

- `copyright.kt` - For Kotlin files
- `copyright.gradle` - For Gradle files
- `copyright.kts` - For Kotlin Script files
- `copyright.xml` - For XML files

To use your own copyright headers:

1. Navigate to the `spotless` directory
2. Replace the content of the copyright files with your own headers
3. Maintain the proper file format for each type

Example copyright header for Kotlin files:

```kotlin
/*
 * Copyright (c) 2024 Your Name/Organization
 * 
 * Licensed under ...
 */
```

> [!NOTE]
> Make sure to update all copyright files to maintain consistency across different file types.

## Configuration Details

The Spotless configuration is defined in `gradle/init.gradle.kts` and includes:

### Kotlin Configuration

```kotlin
kotlin {
    target("**/*.kt")
    targetExclude("**/build/**/*.kt")
    ktlint(ktlintVersion).editorConfigOverride(
        mapOf(
            "android" to "true",
        ),
    ).customRuleSets(
        listOf(
            "io.nlopez.compose.rules:ktlint:0.4.16",
        ),
    )
    licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
}
```

### Other File Types

The configuration includes similar setups for:

- Groovy (Gradle) files
- Kotlin Script (`.kts`) files
- XML files

> [!IMPORTANT]
> The build task will fail if any files don't match the formatting rules. Always run `spotlessApply`
> if `spotlessCheck` fails.

## Best Practices

1. **Pre-commit Hook**: Consider setting up a pre-commit hook to run `spotlessApply`:
   ```bash
   ./gradlew spotlessApply
   ```

2. **IDE Integration**: Use the provided run configurations in Android Studio for easy access to
   Spotless commands.

3. **Regular Checks**: Run Spotless Check periodically during development, not just before commits.

4. **Copyright Updates**: Remember to update copyright years and organization information in the
   header files when necessary.

> [!TIP]
> If you're using Android Studio, you can bind the Spotless Apply run configuration to a keyboard
> shortcut for quick formatting.

## Further Reading

- **[GitHub CI/CD Setup](github.md)** - Continuous integration workflow that runs spotlessCheck
- **[Troubleshooting](troubleshooting.md)** - Solutions for common Spotless formatting issues
- **[Convention Plugins](plugins.md)** - Build logic that configures Spotless
