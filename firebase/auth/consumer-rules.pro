# The playservices Google-Identity provider is discovered via ServiceLoader at runtime, not
# referenced directly from app code, so R8 full mode sees no live reference and strips it -- the
# symptom is a provider-not-found failure at runtime with no compile-time warning. This keeps it
# only when CredentialManager itself is present on the classpath.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}