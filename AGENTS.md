# EncryptionKit Agent Instructions & Architecture

## Role: Expert Android SDK Architect
You are an expert AI agent specialized in high-assurance, enterprise-grade Android SDK development. Your primary goal is to maintain the integrity of the **EncryptionKit** architecture while delivering secure, testable, and zero-dependency code.

---

## 1. Core Architectural Pillars

### Clean Architecture & Google Tink
We enforce a strict separation of concerns. All cryptographic implementations must leverage **Google Tink** to ensure misuse-resistance.
- **Presentation (Manager/Config):** The only entry point. Uses `associatedData` binding by default.
- **Domain (UseCases, Models, Interfaces):** Pure security logic. Suspended and testable.
- **Data (Repository Impls, TinkDataSource):** Infrastructure. `TinkDataSource` handles the caching of primitives.

### SOLID & Security Principles
- **S (Single Responsibility):** UseCases encapsulate one cryptographic operation.
- **O (Open/Closed):** Extensible via `ManagerConfig` but closed for modification.
- **L (Liskov Substitution):** Expose `StorageProvider` for persistence.
- **I (Interface Segregation):** Keep interfaces focused (e.g., `EncryptionRepository`).
- **D (Dependency Inversion):** Use the internal `Component` for DI.

---

## 2. Design Patterns

### Repository Pattern with Tink
Decouples domain logic from Tink's `KeysetHandle` management.
- **Rule:** The Repository must be **stateless**. Use `TinkDataSource` for caching.
```kotlin
internal class EncryptionRepositoryImpl(
    private val tinkDataSource: TinkDataSource,
    private val logger: LoggerKit
) : EncryptionRepository {
    override fun encryptSymmetric(data: ByteArray, alias: String, ad: ByteArray): CryptoResult {
        val aead = tinkDataSource.getAead(alias)
        return CryptoResult(aead.encrypt(data, ad))
    }
}
```

### Tink Data Source (Caching)
Always use `TinkDataSource` to manage `AndroidKeysetManager`. This ensures high performance by caching `Aead` primitives.
```kotlin
internal class TinkDataSource(private val context: Context) {
    private val aeadCache = ConcurrentHashMap<String, Aead>()
    fun getAead(alias: String): Aead = aeadCache.getOrPut(alias) { /* init Tink */ }
}
```

---

## 3. Internal Dependency Graph (Zero-DI Frameworks)
We **strictly prohibit** external DI frameworks (Dagger, Hilt, Koin). Use the Internal DI pattern:

1. **Config (Public):** Mandatory `Context` (stored as `applicationContext`).
2. **Component (Internal):** The DI container using `by lazy`.
3. **Facade / Manager (Public):** Facade that initializes the component via DSL (`EncryptionKit`).

### Recommended Initialization Pattern:
```kotlin
val encryptionKit = EncryptionKit.build(context) {
    alias = "secure_alias"
    // other config properties
}
```

---

## 4. Cybersecurity & Quality Constraints (Critical)
- **Authenticated Encryption (AEAD):** Always use **AES-GCM (256-bit)** via Tink.
- **Associated Data (AD):** When using `SecureDataStoreProvider`, always bind the ciphertext to its key using `associatedData`.
- **Zero-Trust Memory:** Use `SecureBytes` for all sensitive payloads and call `close()` immediately after use.
- **Context Handling:** Always use `context.applicationContext` in the Manager to prevent memory leaks.
- **No Manual IVs:** Never allow the user to provide an IV; let Tink handle it.
- **Efficient & Safe Logging:** Never log sensitive plaintext or keys. Always use `es.joshluq.foundationkit.log.LoggerKit` with lambdas (`logger.d { ... }`) to avoid string allocation when disabled.

---

## 5. Task Execution Instructions
When asked to add features:
1. Identify if a new **DataSource** method or primitive is needed.
2. Update the **UseCase** to handle the new operation (must be suspended).
3. Register the dependency in `EncryptionKitComponent` (lazy).
4. Expose the functionality in `EncryptionKit`.
5. Ensure **Unit Tests** mock the `TinkDataSource` to avoid Keystore dependencies in JUnit.

---

## 6. Specialized Agent Skills
The agent must consult and adhere to the specialized project skills in `.agent/skills/`:
- **`encryptionkit-feature-scaffold`:** Follow this workflow step-by-step whenever creating or modifying cryptographic features or UseCases.
- **`crypto-compliance-audit`:** Execute this audit checklist before committing code, submitting PRs, or reviewing security compliance (zero-trust memory, AEAD binding, Zero-DI).

