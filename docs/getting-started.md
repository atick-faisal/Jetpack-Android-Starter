# Getting Started

This guide gets the project running on your machine: clone, build the debug variant, then optionally
configure Firebase and release signing.

## Prerequisites

- Android Studio Quail 2 (2026.1.2), or a later release supporting AGP 9.3. Check the
  [official compatibility table](https://developer.android.com/build/releases/about-agp#android_gradle_plugin_and_android_studio_compatibility)
  before choosing an IDE; this project pins AGP 9.3.1.
- JDK 21 for both Android Studio's Gradle JDK and command-line builds (`JAVA_HOME`). Check the
  command-line runtime with `./gradlew --version` after cloning.
- Android SDK Platform 37 (the project's `compileSdk`) and Android SDK Platform-Tools, installed
  through Android Studio's SDK Manager. Accept the SDK licenses when prompted.
- An Android device or emulator running API 24 (Android 7.0) or higher. Use an emulator image with
  Google Play services when testing Google Sign-In.

## Quick Start

1. Clone and enter the checkout:

    ```bash
    git clone --depth 1 -b main https://github.com/atick-faisal/Jetpack-Android-Starter.git
    cd Jetpack-Android-Starter
    ```

2. Open the checkout in Android Studio, select JDK 21 as the Gradle JDK, and let Gradle sync.
    Studio records your SDK location in ignored `local.properties`. For command-line-only setup,
    create that file with `sdk.dir=/absolute/path/to/Android/sdk` for your SDK installation.

3. Build the debug variant:

    ```bash
    ./gradlew assembleDebug
    ```

    The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

4. Start an emulator, or connect a device with USB debugging enabled and authorize the computer.
    Select it in Android Studio and run the `app` configuration. Alternatively, with one device
    connected, install from the terminal:

    ```bash
    ./gradlew installDebug
    ```

    Then open **Jetpack** from the device's launcher. Installation alone does not launch the app.

The tracked template `google-services.json` allows a fresh checkout to build and open the app.
Authentication and remote sync require your own Firebase project; a successful build does not
verify access to those services. Follow the setup below before testing sign-in and Firestore.

## Setting Up Firebase Features

This project includes Firebase integration for authentication, Firestore, and crash reporting.
The app also includes Analytics and Performance SDK dependencies; see the Firebase guide for the configured scope.

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

`storeFile` is resolved relative to `app/`, so the example names a file directly inside that
directory. These values are placeholders; use your own signing credentials.

**Build the release variant:**

```bash
./gradlew assembleRelease
```

> [!CAUTION]
> If `keystore.properties` is missing, `app/build.gradle.kts` does not fail the build — it silently
> signs the release variant with the **debug** key, warning only via a `println` that's easy to miss
> in build output. A debug-signed release APK can be installed, but Google Play rejects debug-signed uploads.
> Switching to a different signing certificate normally prevents an in-place update of an installed
> app; keep the same signing identity for updates. Uninstalling to change certificates removes app data.
> Never commit `keystore.properties`, the keystore file, or your real `google-services.json`.

## IDE Setup

- Android Studio reads the project's `.editorconfig`; use the project's Spotless commands for
  repository formatting (see [Build & Tooling](build-and-tooling.md)).
- After Gradle sync, open a Kotlin file with an `@Preview` composable and select **Design** or
  **Split** to view it. See [Compose Preview](https://developer.android.com/develop/ui/compose/tooling/previews)
  and the project's [component guide](components.md) for preview setup.
- Use the provided `.run/` configurations for common tasks — "Signing Report" gets the debug SHA-1
  needed for Google Sign-In

## Further reading

- [Firebase Setup Guide](firebase.md) — configure Firebase features in your project
- [Architecture](architecture.md) — how the app is structured
- [Adding a Feature](guide.md) — step-by-step feature walkthrough
- [Build & Tooling](build-and-tooling.md) — CI/CD, Spotless, Play Store publishing
- [Troubleshooting](troubleshooting.md) — build, Firebase, and release issues
