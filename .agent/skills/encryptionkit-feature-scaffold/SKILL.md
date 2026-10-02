---
name: encryptionkit-feature-scaffold
description: Procedural guide to scaffold and implement new cryptographic features or UseCases in EncryptionKit adhering strictly to Clean Architecture, Zero-DI, Google Tink AEAD, and memory safety.
---

# EncryptionKit Feature Scaffold

This skill guides the agent through adding a new cryptographic capability or UseCase to **EncryptionKit** following the architectural patterns defined in `AGENTS.md`.

## Architectural Checklist (Mandatory Pre-requisites)

- [ ] **Clean Architecture:** Strict separation between Data (`TinkDataSource`, Repository), Domain (Model, UseCase, Repository interface), and SDK facade (`EncryptionKit`).
- [ ] **Zero-DI:** No external frameworks (no Hilt, Dagger, Koin). Pure Kotlin container with `by lazy` in `EncryptionKitComponent`.
- [ ] **Google Tink:** Misuse-resistant AEAD (AES-GCM 256-bit). No manual IVs from callers.
- [ ] **Zero-Trust Memory:** Sensitive byte inputs/outputs encapsulated with `SecureBytes` and wiped via `.close()`.
- [ ] **Tracing:** Use `es.joshluq.foundationkit.log.LoggerKit` with lambdas (`logger.d { ... }`).
- [ ] **Android Context:** Always store `context.applicationContext` in configuration.

---

## 5-Step Implementation Procedure

### Step 1: Data Layer (Primitive & DataSource)
If the feature requires a new Google Tink primitive (e.g. `Aead`, `DeterministicAead`, `HybridEncrypt`, `Mac`):
1. Open `library/src/main/java/es/joshluq/encryptionkit/data/datasource/TinkDataSource.kt`.
2. Cache the primitive using `ConcurrentHashMap<String, Primitive>` to avoid repetitive Keystore lookups.
3. Handle initialization with `AndroidKeysetManager` securely.
4. Update `EncryptionRepository` interface and its implementation `EncryptionRepositoryImpl.kt` ensuring stateless repository calls.

### Step 2: Domain Layer (UseCases & Security Models)
1. If sensitive memory is returned, wrap or clear it with `SecureBytes` (`domain/model/SecureBytes.kt`).
2. If exceptions occur, map Tink / Security exceptions to domain `CryptoException` (`domain/model/CryptoException.kt`).
3. Create the UseCase in `domain/usecase/<FeatureName>UseCase.kt`:
   - Must be a `suspend fun invoke(...)` or return `CryptoResult`.
   - Single responsibility principle: encapsulates only one cryptographic operation.
   - Accepts dependencies exclusively via constructor (e.g. `private val repository: EncryptionRepository`).

```kotlin
class ExampleUseCase(
    private val repository: EncryptionRepository,
) {
    suspend operator fun invoke(data: SecureBytes, alias: String, associatedData: ByteArray): CryptoResult {
        return try {
            repository.encryptSymmetric(data.data, alias, associatedData)
        } finally {
            data.close() // Zero-Trust memory wiping
        }
    }
}
```

### Step 3: Dependency Injection (`EncryptionKitComponent`)
1. Open `library/src/main/java/es/joshluq/encryptionkit/di/EncryptionKitComponent.kt`.
2. Register the new UseCase as a read-only property instantiated with `by lazy`:
```kotlin
val exampleUseCase: ExampleUseCase by lazy {
    ExampleUseCase(repository)
}
```
3. Never use reflection, annotations (`@Inject`), or third-party DI service locators.

### Step 4: SDK Facade (`EncryptionKit` & Config)
1. Open `library/src/main/java/es/joshluq/encryptionkit/sdk/EncryptionKit.kt`.
2. Expose the user-friendly suspended method on `EncryptionKit`:
   - Enforce associated data binding by default where applicable.
   - Delegate directly to `component.<featureUseCase>`.
   - Log execution entry and exit using `logger.d { "Feature executed with alias: $alias" }`.

### Step 5: Unit Testing (JVM Isolation)
1. Mock `TinkDataSource` or `EncryptionRepository` using `mockk` in `library/src/test/java/es/joshluq/encryptionkit/`.
2. **Never** touch `AndroidKeystore` or `AndroidKeysetManager` in JVM JUnit tests.
3. Test cases to cover:
   - Happy path: valid encryption/decryption/computation.
   - Memory zeroing: verify `SecureBytes.isWiped()` becomes true after use.
   - Tampering / Security failure: corrupted ciphertext or mismatched `associatedData` throws appropriate `CryptoException`.
