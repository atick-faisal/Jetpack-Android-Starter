# Getting Started

This guide helps you set up and run the project on your local machine. You'll learn how to clone the
repository, configure Firebase, and prepare for release builds.

---

## Summary

Get started quickly with the Jetpack Android Starter template:

1. Clone the repository and open in Android Studio
2. Build and run the debug variant out of the box
3. Optionally configure Firebase for authentication and Firestore
4. Set up signing configuration for release builds

The debug variant works immediately with minimal setup. Firebase configuration and release builds
are optional until you're ready to use those features.

---

## Quick Start

**Clone the repository** (with depth 1 to reduce clone size):

```bash
git clone --depth 1 -b main https://github.com/atick-faisal/Jetpack-Android-Starter.git
```

**Open the project** in Android Studio Hedgehog or newer

**Run the debug build variant:**

```bash
./gradlew assembleDebug
```

> [!NOTE]
> The debug variant works out of the box with the template `google-services.json` file.
> However, Firebase features like authentication and Firestore won't be functional until you set up
> your own Firebase project.

## Prerequisites

- Android Studio Hedgehog or newer
- JDK 21
- An Android device or emulator running API 24 (Android 7.0) or higher

---

## Setting Up Firebase Features

This project includes Firebase integration for authentication, Firestore, and analytics. The debug
variant works out of the box with a template `google-services.json`, but Firebase features won't be
functional until you configure your own Firebase project.

> [!NOTE]
> For complete Firebase setup instructions, see the [Firebase Setup Guide](firebase.md). The guide
> covers:
> - Creating your Firebase project
> - Configuring Authentication (Google Sign-In and Email/Password)
> - Setting up Firestore database
> - Downloading and configuring `google-services.json`
> - Setting up security rules
> - Troubleshooting common Firebase issues

---

## Release Build Setup

To create release builds, you need to set up signing:

**Create a keystore file** using Android Studio's "Generate Signed Bundle/APK" tool

**Create `keystore.properties`** in the project root:

```properties
storePassword=your-store-password
keyPassword=your-key-password
keyAlias=your-key-alias
storeFile=your-keystore-file.jks
```

**Place your keystore file** in the `app/` directory

**Build the release variant:**

```bash
./gradlew assembleRelease
```

> [!TIP]
> Use Android Studio's "Generate Signed Bundle/APK" tool to create your keystore if you don't
> have one.

---

## Next Steps

After getting the project running, continue with these guides:

**Understand the Architecture** - Read the [Architecture Overview](architecture.md) to understand
how the app is structured

**Setup CI/CD** - Follow the [GitHub CI/CD Guide](github.md) to set up automation

**Code Style** - Review the [Spotless Setup](spotless.md) for code formatting guidelines

---

## Common Issues

**Build Fails:**

- Ensure you have JDK 21 set in Android Studio
- Run `./gradlew clean` and try again
- Check if all dependencies are resolved

**Firebase Features Not Working:**

- Verify you've replaced `google-services.json`
- Check Firebase Console for proper setup
- Ensure SHA-1 is added for authentication

**Release Build Fails:**

- Verify `keystore.properties` exists and has correct values
- Confirm keystore file is in the correct location
- Check signing configuration in `app/build.gradle.kts`

> [!IMPORTANT]
> Never commit sensitive files like `keystore.properties`, your keystore file, or your real
> `google-services.json` to version control.

For more troubleshooting help, see the [Troubleshooting Guide](troubleshooting.md).

---

## IDE Setup

For the best development experience:

**Enable Compose Preview:**

- Ensure "Live Edit of Literals" is enabled
- Configure appropriate preview devices

**Run Configurations:**

- Use provided run configurations for common tasks
- Signing Report configuration helps get SHA-1 for Firebase

**Code Style:**

- Import the project's `.editorconfig`
- Enable "Format on Save" for Kotlin files
- Use the Spotless plugin for consistent formatting

---

## Further Reading

- [Firebase Setup Guide](firebase.md) - Configure Firebase features in your project
- [Architecture Overview](architecture.md) - Learn about the app's architecture
- [Design Philosophy](philosophy.md) - Understand the design principles
- [Adding New Features](guide.md) - Step-by-step feature implementation guide
- [Dependency Management](dependency.md) - Understand dependency management
- [Convention Plugins](plugins.md) - Learn about custom Gradle plugins
- [Spotless Setup](spotless.md) - Code formatting and style enforcement
- [GitHub CI/CD](github.md) - Continuous integration and deployment
- [Performance Optimization](performance.md) - Optimize app speed and efficiency
- [Tips & Tricks](tips.md) - Useful development and debugging tips
- [Publishing to Play Store](fastlane.md) - Deploy your app to Google Play
