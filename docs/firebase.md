# Firebase Setup

## Summary

This guide walks you through setting up Firebase for this project, including Authentication (Google
Sign-In and Email/Password), Firestore database, and Analytics. Learn how to configure the Firebase
Console, download and protect `google-services.json`, set up security rules, and troubleshoot common
Firebase issues.

---

This project uses Firebase for authentication, Firestore database, and analytics. Follow this guide
to set up Firebase for your project.

## Prerequisites

- A Google account
- Access to [Firebase Console](https://console.firebase.google.com)
- Android Studio

## Firebase Console Setup

1. Create a new project in Firebase Console:
    - Go to [Firebase Console](https://console.firebase.google.com)
    - Click "Add project"
    - Enter your project name
    - Follow the setup wizard

2. Add Android app to your Firebase project:
    - Click on Android icon in project overview
    - Register app using your package name (`dev.your.package.name`)
    - Get the SHA-1 using the provided Signing Report configuration:
        - Open Android Studio
        - Run the "Signing Report" configuration from the run configurations dropdown
        - Copy the SHA-1 hash
    - Enter the SHA-1 in Firebase console

3. Enable Required Services:

   a. **Authentication**:
    - Go to "Authentication" in Firebase Console
    - Click "Get Started"
    - Enable "Google" sign-in method
    - Enable "Email/Password" sign-in method
    - Add your support email

   b. **Firestore**:
    - Go to "Firestore Database"
    - Click "Create Database"
    - Choose your location
    - Start in production mode

## Local Project Setup

1. Download Configuration File:
    - Download `google-services.json` from Firebase Console
    - Before replacing the template file, stop Git from tracking it:
      ```bash
      git update-index --skip-worktree app/google-services.json
      ```
    - Replace the template `google-services.json` in the `app` directory with your downloaded file

2. Verify Firebase Setup:
    - Build and run the app
    - Try signing in with Google
    - Check Firebase Console to verify authentication is working

> [!WARNING]
> Never commit your actual `google-services.json` to version control. The template file is provided
> only to ensure successful builds.

## Troubleshooting

Common issues and solutions:

1. **Google Sign-In Not Working**:
    - Verify SHA-1 is correctly added in Firebase Console
    - Check if `google-services.json` is up to date
    - Ensure the OAuth consent screen is configured

2. **Firestore Access Issues**:
    - Check your Firestore rules
    - Verify your device has internet connection
    - Check if the user is properly authenticated

3. **Build Issues**:
    - Clean and rebuild the project
    - Verify `google-services.json` is in the correct location
    - Check if all Firebase dependencies are resolved

> [!TIP]
> If you need to get the SHA-1 hash again later, you can always run the "Signing Report"
> configuration from Android Studio.

## Firebase Configuration Files

The project handles Firebase configuration files in several ways:

1. **Development**:
    - Use your local `google-services.json`
    - Keep it private using `git update-index --skip-worktree`

2. **CI/CD**:
    - GitHub Actions workflow uses encoded secrets
    - See [GitHub CI/CD Setup](github.md) for details

> [!NOTE]
> The template `google-services.json` allows the project to build but won't enable Firebase
> services. You must replace it with your own configuration file.

## Firestore Security Rules

Set up security rules in Firebase Console to protect your data:

1. Go to Firestore Database
2. Click on "Rules" tab
3. Replace the default rules with the following structure:

   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Helper function to check if user is authenticated
       function isAuthenticated() {
         return request.auth != null;
       }
   
       // Helper function to check if the user is accessing their own data
       function isUserOwner(userId) {
         return isAuthenticated() && request.auth.uid == userId;
       }
   
       // Helper function to validate Jetpack data structure
       function isValidJetpack(jetpack) {
         return jetpack.size() == 7
           && 'id' in jetpack && jetpack.id is string
           // ... other validations
       }
   
       // Match the specific path structure: dev.atick.jetpack/{userId}/jetpacks/{jetpackId}
       match /dev.atick.jetpack/{userId}/jetpacks/{jetpackId} {
         allow read: if isUserOwner(userId);
         
         allow create: if isUserOwner(userId) 
           && isValidJetpack(request.resource.data);
         
         allow update: if isUserOwner(userId) 
           && isValidJetpack(request.resource.data)
           && request.resource.data.id == resource.data.id;
         
         allow delete: if isUserOwner(userId);
       }
       
       // Deny access to all other documents by default
       match /{document=**} {
         allow read, write: if false;
       }
     }
   }
   ```

These rules implement several security features:

- User authentication check
- Data ownership validation
- Document structure validation
- Explicit deny-by-default for unmatched paths

> [!IMPORTANT]
> Always test your security rules thoroughly using the Firebase Console Rules Playground before
> deploying to production.

## Additional Setup

1. **Test Your Configuration**:

   ```kotlin
   // Add this in an activity or fragment
   FirebaseAuth.getInstance().currentUser?.let {
     Timber.d("Firebase Auth is working: ${it.email}")
   }
   ```

2. **Reset Git Tracking** (if needed):

   ```bash
   git update-index --no-skip-worktree app/google-services.json
   ```

3. **Multiple Environments**:
    - Create separate Firebase projects for development and production
    - Manage different `google-services.json` files for each build variant

> [!IMPORTANT]
> Remember to add your `google-services.json` to `.gitignore` if you're setting up the project from
> scratch.

## Further Reading

- **[Troubleshooting](troubleshooting.md)** - Solutions for common Firebase setup and runtime issues
- **[GitHub CI/CD Setup](github.md)** - CI/CD workflow that uses Firebase secrets
- **[FAQ](faq.md)** - Firebase setup questions and Google Sign-In troubleshooting