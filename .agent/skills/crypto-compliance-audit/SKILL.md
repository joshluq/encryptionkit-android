---
name: crypto-compliance-audit
description: Static verification checklist and audit procedure to enforce cryptographic compliance, misuse-resistance, memory wipe safety, and zero external DI in EncryptionKit.
---

# Crypto Compliance Audit

This skill provides an audit procedure to ensure any proposed or existing code in **EncryptionKit** meets high-assurance enterprise security requirements and complies with `AGENTS.md`.

## Audit Categories & Rules

### 1. Authenticated Encryption & Algorithm Compliance
- [ ] **Tink AEAD AES-GCM (256-bit):** All symmetric encryption operations must use Google Tink's AES-GCM (256-bit) AEAD template (`KeyTemplates.get("AES256_GCM")` or `AeadConfig`).
- [ ] **No Manual IVs:** Verify that no public or internal API accepts a caller-provided Initialization Vector (IV / Nonce). Tink must generate random, non-repeating IVs internally to prevent catastrophic nonce reuse.
- [ ] **No Legacy Ciphers:** No manual `Cipher.getInstance("AES/CBC/...")` or unauthenticated ECB/CTR modes.

### 2. Associated Data (AD) Binding
- [ ] **Key-Ciphertext Binding:** For persistence (`SecureDataStoreProvider`), verify that ciphertext is bound to its key or context using `associatedData`.
- [ ] **AD Mismatch Protection:** Ensure decryption fails securely when altered or missing associated data is provided, throwing `CryptoException`.

### 3. Zero-Trust Memory & Lifecycle
- [ ] **SecureBytes Usage:** Plaintext payloads and sensitive keys must be represented using `SecureBytes` or `ByteArray` that is promptly zeroed.
- [ ] **Immediate Memory Wiping:** Call `.close()` or overwrite byte arrays (`Arrays.fill(bytes, 0.toByte())`) immediately after cryptographic processing in `finally` blocks or `use { }`.
- [ ] **No Lingering String Plaintext:** Avoid storing secret payloads in immutable `String` instances on the Java heap whenever possible.

### 4. Zero-DI & Architecture Integrity
- [ ] **No Third-Party DI:** Ensure `build.gradle.kts` does not include Hilt, Dagger, or Koin dependencies.
- [ ] **Internal Component:** DI must be handled strictly inside `EncryptionKitComponent` using `by lazy`.
- [ ] **Stateless Repositories:** Verify that `EncryptionRepositoryImpl` does not hold state between calls; state/primitives are managed by `TinkDataSource`.

### 5. Android Context & Leak Prevention
- [ ] **Application Context:** Check `EncryptionKitConfig` and `EncryptionKitManager` initialization to verify `context.applicationContext` is used, preventing Activity leaks.

### 6. Logging & Data Leakage
- [ ] **LoggerKit Adoption:** All logs must use `es.joshluq.foundationkit.log.LoggerKit` with lambda expressions (e.g., `logger.d { "message" }`) to avoid string allocation when disabled.
- [ ] **No Sensitive Data in Logs:** Ciphertext previews, keys, plaintexts, or IVs must never be logged.

### 7. Unit Test Isolation
- [ ] **Keystore Decoupling:** JVM Unit tests (`src/test`) must mock `TinkDataSource` or use in-memory keysets without invoking Android's hardware Keystore (`AndroidKeysetManager`).

---

## Audit Execution Script / Steps

When running an audit on a branch or PR:
1. Search for forbidden DI annotations:
   `Select-String -Path "library/src/main/**" -Pattern "@Inject|@Singleton|@Provides"` (should return 0 matches).
2. Search for manual IV parameter definitions:
   `Select-String -Path "library/src/main/**" -Pattern "iv:|iv =|IvParameterSpec"` (should return 0 matches outside raw tests).
3. Search for insecure string logging:
   `Select-String -Path "library/src/main/**" -Pattern "Log\.[devwi]|println\("` (should use `LoggerKit`).
4. Validate test execution:
   Run `./gradlew testDebugUnitTest` to confirm all unit tests pass without Keystore dependencies.
