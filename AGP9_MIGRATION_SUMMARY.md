# Android Gradle Plugin 9.1.0 Migration Summary

## ✅ Migration Status: SUCCESSFUL

**Build Status**: ✅ Passing (528 tasks, 15s with cache)
**Migration Date**: March 13, 2026
**Previous AGP**: 8.x → **Current AGP**: 9.1.0

---

## 📦 Version Changes

| Component | Previous | Current | Status |
|-----------|----------|---------|--------|
| Android Gradle Plugin | 8.x | **9.1.0** | ✅ |
| Gradle | 8.11.1 | **9.4.0** | ✅ Auto-upgraded |
| Kotlin | 2.3.x | **2.3.10** | ✅ |
| Dagger Hilt | 2.5x | **2.59.2** | ✅ AGP 9 compatible |
| Dokka | 2.1.0 | **2.1.0** | ⚠️ Needs upgrade to 2.2.0-Beta |
| Spotless | 7.2.x | **8.3.0** | ⚠️ Task discovery issues |
| Firebase BOM | 34.0.0 | **34.10.0** | ✅ |
| Play Services Auth | 21.4.0 | **21.5.1** | ✅ |
| Compose Rules | 0.4.26 | **0.5.6** | ✅ |

---

## 🔧 Code Changes Applied

### 1. Convention Plugins (build-logic/convention/)

#### Removed kotlin-android Plugin
AGP 9 includes built-in Kotlin support, making the `kotlin-android` plugin redundant.

**Files Modified:**
- `ApplicationConventionPlugin.kt`
- `LibraryConventionPlugin.kt`
- `UiLibraryConventionPlugin.kt`

**Change:**
```kotlin
// ❌ Old (AGP 8)
with(pluginManager) {
    apply("com.android.application")
    apply("org.jetbrains.kotlin.android")  // Removed
    apply("org.jetbrains.kotlin.plugin.compose")
}

// ✅ New (AGP 9)
with(pluginManager) {
    apply("com.android.application")
    // Built-in Kotlin support - no kotlin-android needed
    apply("org.jetbrains.kotlin.plugin.compose")
}
```

#### Updated DSL Imports
AGP 9 introduces new DSL interfaces.

**Change:**
```kotlin
// ❌ Old
import com.android.build.gradle.LibraryExtension

// ✅ New
import com.android.build.api.dsl.LibraryExtension
```

### 2. App Module (app/build.gradle.kts)

#### Migrated Variant API
The old `applicationVariants` API was completely removed in AGP 9.

**Change:**
```kotlin
// ❌ Old (AGP 8) - NO LONGER WORKS
buildTypes {
    release {
        applicationVariants.all {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName =
                    "Jetpack_release_v${versionName}_${timestamp}.apk"
            }
        }
    }
}

// ✅ New (AGP 9) - Using androidComponents API
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            // Custom filename logic (TODO: Issue #579)
            output.versionName.set("${variant.outputs.first().versionName.getOrElse("1.0.0")}")
        }
    }
}
```

**Note**: Custom APK naming requires a different approach in AGP 9. See Issue #579.

### 3. Dokka Plugin (build-logic/convention/src/main/kotlin/DokkaConventionPlugin.kt)

#### AGP 9 Compatibility Workaround

**Change:**
```kotlin
// ❌ Old (AGP 8)
extensions.configure<DokkaExtension> {
    dokkaSourceSets.named("main") {
        includes.from("README.md")
    }
}

// ✅ New (AGP 9) - Deferred configuration
afterEvaluate {
    extensions.configure<DokkaExtension> {
        dokkaSourceSets.configureEach {
            if (name == "main") {
                includes.from("README.md")
            }
        }
    }
}
```

**Note**: This is a temporary workaround. Upgrade to Dokka 2.2.0-Beta when stable (Issue #578).

### 4. Dependency Version Updates (gradle/libs.versions.toml)

Resolved merge conflicts by selecting the latest versions:
- Firebase BOM: 34.10.0
- Firebase Crashlytics Plugin: 3.0.6
- Firebase Performance Plugin: 2.0.2
- Play Services Auth: 21.5.1
- Spotless: 8.3.0
- Compose Rules: 0.5.6

---

## 📋 GitHub Issues Created

All pending work is tracked in GitHub issues:

### **Issue #578**: [Upgrade Dokka to 2.2.0-Beta](https://github.com/atick-faisal/Jetpack-Android-Starter/issues/578)
- **Priority**: High
- **Description**: Current Dokka 2.1.0 shows AGP 9 compatibility warnings
- **Solution**: Upgrade to Dokka 2.2.0-Beta when stable
- **Files**: `gradle/libs.versions.toml`, `build-logic/convention/src/main/kotlin/DokkaConventionPlugin.kt`

### **Issue #579**: [Implement custom APK naming pattern](https://github.com/atick-faisal/Jetpack-Android-Starter/issues/579)
- **Priority**: High
- **Description**: AGP 9 removed direct outputFile manipulation
- **Previous Behavior**: `Jetpack_release_v{version}_{timestamp}.apk`
- **Current**: Using default APK naming
- **Solution**: Implement AGP 9 variant artifacts API or task customization
- **File**: `app/build.gradle.kts`

### **Issue #580**: [Verify Spotless task discoverability](https://github.com/atick-faisal/Jetpack-Android-Starter/issues/580)
- **Priority**: Medium
- **Description**: Spotless tasks not discoverable via `./gradlew tasks`
- **Investigation Needed**: Gradle 9.4.0 init script compatibility
- **File**: `gradle/init.gradle.kts`

### **Issue #581**: [Post-migration cleanup and verification](https://github.com/atick-faisal/Jetpack-Android-Starter/issues/581)
- **Priority**: Medium
- **Description**: Master tracking issue for AGP 9 migration completion
- **Contains**: Checklist of all pending tasks and verification steps

---

## ⚠️ Known Warnings (Harmless)

These warnings appear during builds but don't prevent success:

### 1. Dokka Warnings
```
class org.jetbrains.dokka.gradle.adapters.AndroidExtensionWrapper could not get Android Extension for project
```
- **Impact**: None (builds succeed)
- **Fix**: Upgrade to Dokka 2.2.0-Beta (Issue #578)

### 2. Configuration Cache Warning
```
Configuration cache entry discarded because incompatible task was found:
'task :app:debugOssLicensesTask' of type 'com.google.android.gms.oss.licenses.plugin.LicensesTask'
```
- **Impact**: Cache is rebuilt but builds still succeed
- **Fix**: Wait for OSS Licenses plugin update

---

## 📝 TODO Comments Added

All workarounds and future improvements are marked with TODO/FIXME comments:

1. **Dokka Configuration** (`build-logic/convention/src/main/kotlin/DokkaConventionPlugin.kt`)
   ```kotlin
   // TODO: AGP 9 Migration - Dokka Configuration Workaround
   // FIXME: Upgrade to Dokka 2.2.0-Beta when stable
   // Tracking: GitHub Issue #578
   ```

2. **Custom APK Naming** (`app/build.gradle.kts`)
   ```kotlin
   // TODO: AGP 9 Migration - Custom Output Filename
   // FIXME: Implement proper AGP 9 approach for custom APK naming
   // Tracking: GitHub Issue #579
   ```

3. **Dokka Version** (`gradle/libs.versions.toml`)
   ```kotlin
   # TODO: Upgrade to Dokka 2.2.0-Beta when stable
   # FIXME: Current version has AGP 9 compatibility warnings (Issue #578)
   dokka = "2.1.0"
   ```

4. **Spotless** (`gradle/init.gradle.kts`)
   ```kotlin
   // TODO: Verify Spotless task discoverability in Gradle 9.4.0 (Issue #580)
   ```

---

## 🔍 Testing Verification

### Build Success
```bash
./gradlew assembleDebug
# BUILD SUCCESSFUL in 2m 37s
# 528 actionable tasks: 474 executed

./gradlew assembleDebug  # Second run
# BUILD SUCCESSFUL in 15s
# 528 actionable tasks: 526 up-to-date (excellent caching)
```

### Module Compilation
All modules compile successfully:
- ✅ app
- ✅ build-logic/convention
- ✅ core:android, core:network, core:preferences, core:room, core:ui
- ✅ data
- ✅ feature:auth, feature:home, feature:profile, feature:settings
- ✅ firebase:analytics, firebase:auth, firebase:firestore
- ✅ sync

---

## 📚 Migration Resources

### Official Documentation
- [AGP 9.0.1 Release Notes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Built-in Kotlin Documentation](https://kotl.in/gradle/agp-built-in-kotlin)
- [Extend AGP Guide](https://developer.android.com/build/extend-agp)

### Community Guides
- [AGP 9.1.0 Complete Developer Guide](https://medium.com/@asankitkumar8130/android-gradle-plugin-9-1-0-the-complete-developer-guide-fad021b47743)
- [AGP 9.0 Migration Guide](https://mrkivan820.medium.com/agp-9-0-migration-guide-fixing-dsl-deprecated-flags-build-changes-01589a57f75d)
- [Android Gradle Recipes](https://github.com/android/gradle-recipes/tree/agp-9.0)

### Plugin-Specific
- [Dagger Hilt 2.59 Release](https://github.com/google/dagger/releases/tag/dagger-2.59)
- [Dokka AGP 9 Support](https://github.com/Kotlin/dokka/issues/4256)

---

## 🎯 Next Steps

### Immediate (High Priority)
1. Monitor Issue #578 for Dokka 2.2.0-Beta stable release
2. Implement custom APK naming (Issue #579) when releasing to production
3. Verify CI/CD workflows work with AGP 9

### Short-term (Medium Priority)
1. Investigate Spotless task discovery issue (Issue #580)
2. Test release builds with proper keystore configuration
3. Update documentation for new contributors

### Long-term (Low Priority)
1. Implement comprehensive test suite
2. Verify LeakCanary compatibility
3. Monitor for AGP 9.2.0 release and migration guides

---

## 📞 Support

For issues related to this migration:
1. Check existing GitHub issues: #578, #579, #580, #581
2. Consult `CLAUDE.md` for AGP 9 specific notes
3. Review official AGP 9 documentation links above

---

**Migration completed successfully by Claude Code on March 13, 2026**
