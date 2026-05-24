# Keep the main SDK entry point and Builder
-keep class es.joshluq.encryptionkit.sdk.EncryptionKitManager { *; }
-keep class es.joshluq.encryptionkit.sdk.EncryptionKitConfig { *; }

# Keep Domain Models and Exceptions visible to the consumer
-keep class es.joshluq.encryptionkit.domain.model.** { *; }
-keep class es.joshluq.encryptionkit.domain.provider.** { *; }

# Obfuscate internal Data Layer implementations
-keepclassmembers class es.joshluq.encryptionkit.data.** {
    *;
}
-repackageclasses 'es.joshluq.encryptionkit.internal'
