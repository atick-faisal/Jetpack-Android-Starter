# GitHub CI/CD Setup

## Summary

This guide explains the project's three GitHub Actions workflows: Build (runs on PRs with Spotless
checks), Documentation (deploys to GitHub Pages), and Release (builds signed APKs and deploys to
Play Store). Learn how to set up required secrets, create releases with tags, and troubleshoot
failed workflows.

---

This project uses GitHub Actions for continuous integration and deployment. We have three main
workflows:

## Build Workflow

The build workflow runs on every pull request and when libraries are updated. It performs the
following tasks:

- Validates Gradle wrapper
- Sets up JDK 21
- Runs code style checks using Spotless
- Builds the project

> [!NOTE]
> The build workflow runs automatically and requires no additional setup.

## Documentation Workflow

The documentation workflow runs whenever changes are pushed to the main branch. It:

- Sets up Python and required dependencies
- Generates API documentation using Dokka
- Deploys documentation to GitHub Pages

> [!NOTE]
> The documentation workflow runs automatically and requires no additional setup. Make sure your
> repository has GitHub Pages enabled in the settings.

## Release Workflow

The release workflow is triggered when you push a tag with the format `v*.*.*` (e.g., v1.0.0). It:

- Builds a signed release APK
- Creates a GitHub release with the APK
- Deploys to Google Play Store using Fastlane

### Prerequisites

Before you can use the release workflow, you need to set up the following GitHub secrets:

1. `GOOGLE_SERVICES`: Base64 encoded content of your `google-services.json` file
   ```bash
   base64 -i google-services.json | tr -d '\n'
   ```

2. `KEYSTORE`: Base64 encoded content of your keystore file
   ```bash
   base64 -i your-keystore.jks | tr -d '\n'
   ```

3. `KEYSTORE_PROPERTIES`: Base64 encoded content of your keystore.properties file containing:
   ```properties
   storeFile=key.jks
   storePassword=your-store-password
   keyAlias=your-key-alias
   keyPassword=your-key-password
   ```

4. `PLAY_STORE_JSON`: Base64 encoded content of your `play-store.json` file
   ```bash
   base64 -i play-store.json | tr -d '\n'
   ```

> [!WARNING]
> Never commit your keystore file, keystore properties, google-services.json or play-store.json
> directly to the repository.

### Creating a Release

To create a new release:

1. Update the `CHANGELOG.md` file with your release notes
2. Create and push a new tag:
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```

The workflow will automatically:

- Generate a signed APK
- Create a GitHub release with the changelog
- Upload the APK to the release
- Deploy to Play Store via Fastlane

> [!TIP]
> Make sure your Fastlane setup is complete before using the release workflow. See
> the [Fastlane Setup](fastlane.md) guide for details.

### Debugging Failed Releases

If the release workflow fails, check:

1. GitHub Secrets are properly set
2. Tag format follows `v*.*.*` pattern
3. `CHANGELOG.md` exists and is properly formatted
4. Fastlane configuration is correct
5. Keystore and signing configuration match Play Store requirements

> [!NOTE]
> The workflow timeout is set to 45 minutes. For large projects, you might need to adjust this in
> the workflow file.

## Further Reading

- **[Fastlane Setup](fastlane.md)** - Play Store deployment automation used by release workflow
- **[Firebase Setup](firebase.md)** - Firebase configuration needed for release builds
- **[Spotless](spotless.md)** - Code formatting checked by build workflow
