# Minify obfuscates navigation destinations marked with @Serializable
# https://issuetracker.google.com/issues/353898971?pli=1
-keep @interface kotlinx.serialization.Serializable
# ⚠️ The issue above only requires keeping navigation-destination classes from being
# stripped/renamed, but this rule keeps every member of every @Serializable class in the app, not
# just navigation destinations. That breadth is deliberate: any new @Serializable class is
# automatically covered with no keep-list maintenance, at the cost of being much blunter than the
# bug requires. See core/network/consumer-rules.pro and firebase/auth/consumer-rules.pro for the
# narrower, per-rule style used elsewhere.
-keep @kotlinx.serialization.Serializable class * { *; }

# Crashlytics
# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.