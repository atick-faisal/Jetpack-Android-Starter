# Getting Started

This guide gets the project running on your machine: clone, build the debug variant, then optionally
configure Firebase and release signing.

## Quick Start

**Clone the repository** (with depth 1 to reduce clone size):

```bash
git clone --depth 1 -b main https://github.com/atick-faisal/Jetpack-Android-Starter.git
```

**Open the project** in Android Studio Hedgehog or newer, then **build the debug variant**:

```bash
./gradlew assembleDebug
```

The debug variant builds and runs immediately using the template `google-services.json` — Firebase
features like authentication and Firestore won't be functional until you configure your own project
(see below).

## Prerequisites

- Android Studio Hedgehog or newer
- JDK 21
- An Android device or emulator running API 24 (Android 7.0) or higher

## Setting Up Firebase Features

This project includes Firebase integration for authentication, Firestore, and analytics.

> [!NOTE]
> For complete setup instructions, see the [Firebase Setup Guide](firebase.md). It covers creating
> your Firebase project, configuring Authentication (Google Sign-In and Email/Password), setting up
> Firestore, and downloading `google-services.json`.

## Release Build Setup

**Create a keystore file** using Android Studio's "Generate Signed Bundle/APK" tool, then place it in
the `app/` directory. **Create `keystore.properties`** in the project root:

```properties
storePassword=your-store-password
keyPassword=your-key-password
keyAlias=your-key-alias
storeFile=your-keystore-file.jks
```

**Build the release variant:**

```bash
./gradlew assembleRelease
```

> [!CAUTION]
> If `keystore.properties` is missing, `app/build.gradle.kts` does not fail the build — it silently
> signs the release variant with the **debug** key, warning only via a `println` that's easy to miss
> in build output. The result is a release APK/AAB that installs but cannot be upgraded or published.
> Never commit `keystore.properties`, the keystore file, or your real `google-services.json`.

## IDE Setup

- Import the project's `.editorconfig` and enable "Format on Save" for Kotlin files
- Enable "Live Edit of Literals" for Compose previews
- Use the provided `.run/` configurations for common tasks — "Signing Report" gets the debug SHA-1
  needed for Google Sign-In

## Further reading

- [Firebase Setup Guide](firebase.md) — configure Firebase features in your project
- [Architecture](architecture.md) — how the app is structured
- [Adding a Feature](guide.md) — step-by-step feature walkthrough
- [Build & Tooling](build-and-tooling.md) — CI/CD, Spotless, Play Store publishing
- [Troubleshooting](troubleshooting.md) — build, Firebase, and release issues
