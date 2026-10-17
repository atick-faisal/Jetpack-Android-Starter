# CredentialProviderFactory reads provider class names from manifest service metadata and
# instantiates them reflectively. When CredentialManager is present, this rule keeps every
# class and member in the playservices package, including CredentialProviderPlayServicesImpl.
# The credentials-play-services-auth library also supplies a narrower rule for that provider.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
