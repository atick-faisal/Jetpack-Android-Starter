# Fastlane and Play Store Setup

## Summary

This guide explains how to set up Fastlane for automated Play Store deployments. Learn how to
configure Play Store authentication with service account JSON, manage metadata and changelogs,
deploy updates to different tracks (internal/alpha/beta/production), and troubleshoot common
deployment issues.

---

This project uses Fastlane to automate Play Store deployments. This guide will help you set up
Fastlane for your deployment needs.

## Prerequisites

Before you begin, make sure you have:

1. Ruby installed (version 2.5 or higher)
2. Bundler installed (`gem install bundler`)
3. A Google Play Console account with access to your app
4. A Play Store API service account (JSON key file)

> [!NOTE]
> For detailed instructions about setting up Fastlane for Android, refer to
> the [official Android setup guide](https://docs.fastlane.tools/getting-started/android/setup/).

## Initial Setup

1. Install Fastlane:
   ```bash
   gem install fastlane
   ```

2. Set up Play Store authentication:
    - Follow
      the [Fastlane Play Store Setup Guide](https://docs.fastlane.tools/actions/upload_to_play_store/)
      to create and download your `play-store.json` service account key
    - Place the `play-store.json` file in your fastlane directory

> [!WARNING]
> Never commit your `play-store.json` file to the repository. Make sure it's included in your
`.gitignore`.

## Configure Fastlane Files

### Appfile Configuration

Edit the `fastlane/Appfile` to configure your app-specific information:

```ruby
json_key_file("fastlane/play-store.json") # Path to your Play Store service account json file
package_name("dev.your.package.name") # Your app's package name
```

### Fastfile Configuration

Edit the `fastlane/Fastfile` to define your deployment lanes:

```ruby
default_platform(:android)

platform :android do
  desc "Deploy a new version to the Google Play"
  lane :deploy do
    # Upload to Play Store
    upload_to_play_store(
      track: 'internal', # or 'alpha', 'beta', 'production'
      aab: '../app/build/outputs/bundle/release/app-release.aab',
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
      release_status: "completed",
      version_code: ENV["VERSION_CODE"].to_i
    )
  end
end
```

## Metadata Management

The project includes a standard Fastlane metadata structure:

```
metadata/
└── android/
    └── en-US/
        ├── changelogs/
        ├── images/
        ├── full_description.txt
        ├── short_description.txt
        ├── title.txt
        └── video.txt
```

To pull existing metadata from Play Store:

```bash
fastlane supply init
```

> [!IMPORTANT]
> For `supply` to be able to initialize, you need to have successfully uploaded an APK to your app
> in the Google Play Console at least once.

> [!NOTE]
> This command downloads all your existing Play Store metadata and screenshots. It's useful when
> setting up a new machine or CI environment.

## Deploying Updates

1. Update your changelog:
    - Edit `fastlane/metadata/android/en-US/changelogs/default.txt`
    - Or create a version-specific changelog: `changelogs/<version_code>.txt`

2. Update app metadata (if needed):
    - Edit relevant files in `metadata/android/en-US/`
    - Update screenshots in `metadata/android/en-US/images/`

3. Run deployment:
   ```bash
   fastlane android deploy
   ```

> [!TIP]
> Our GitHub Actions workflow automatically runs the deployment when you create a new release tag.
> See the [GitHub CI/CD](github.md) guide for details.

## Troubleshooting Common Issues

1. **Authentication Errors**:
    - Verify your `play-store.json` file is correctly placed
    - Ensure the service account has appropriate permissions in Play Console
    - Check if the JSON key is properly formatted

2. **Upload Failures**:
    - Verify your app's version code is incremented
    - Ensure the APK/AAB is properly signed
    - Check if the track (internal/alpha/beta/production) exists

3. **Metadata Issues**:
    - Validate all required metadata files exist
    - Check character limits in descriptions
    - Ensure screenshot dimensions meet Play Store requirements

> [!NOTE]
> For more detailed information about Fastlane commands and options, refer to
> the [Fastlane supply action documentation](https://docs.fastlane.tools/actions/supply/).

## Further Reading

- **[GitHub CI/CD Setup](github.md)** - Automated release workflow that uses Fastlane
- **[Troubleshooting](troubleshooting.md)** - Solutions for common deployment issues
- **[FAQ](faq.md)** - Release preparation and deployment questions
